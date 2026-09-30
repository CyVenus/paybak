package app.paybak.paybak.feature.pickers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addGuest
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbChipLeading
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSheetRow
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

private val EmailOrPhone = Regex("""^\S+@\S+\.\S+$|^\+?[\d\s-]{7,}$""")

/**
 * The `pickPeople` route (`addExpenseSplitWith`; add-expense §5). Multi: search, the picked people
 * as removable chips, You, Add a new friend and the Friends list (guests tagged); Back and Done
 * both answer with the picks. Single (Record payment's parties, Lent to): a tap answers. A search
 * with no match offers to add the text as a guest.
 */
@Composable
fun PeoplePickerScreen(route: Route.PickPeople) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val people = rememberPeopleDirectory()
    val multi = route.mode == PickMode.Multi
    var picked by rememberSaveable { mutableStateOf(route.selected) }
    var query by rememberSaveable { mutableStateOf("") }
    var known by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val tag = if (multi) "splitWith" else "pickPerson"
    // Who was already picked comes first, in the order they were picked (add-expense §5.2).
    val friends =
        people.friends
            .filter { route.allowsGuests || !it.isGuest }
            .sortedBy { friend ->
                route.selected.indexOf(friend.id).let { if (it < 0) Int.MAX_VALUE else it }
            }
    // Coming back from Add friend, tick whoever was just added.
    LaunchedEffect(friends.map { it.id }) {
        val before = known ?: return@LaunchedEffect
        picked = picked + friends.map { it.id }.filter { it !in before }
        known = null
    }
    fun finish() = navigator.complete(route.request.id, RouteResult.People(picked))
    fun choose(id: String) {
        if (multi) picked = if (id in picked) picked - id else picked + id
        else navigator.complete(route.request.id, RouteResult.Person(id))
    }
    fun addGuest() {
        val text = query.trim()
        val id =
            if (EmailOrPhone.matches(text)) ledger.addGuest(text.substringBefore('@'), text)
            else ledger.addGuest(text, null)
        query = ""
        choose(id)
    }
    BackHandler(enabled = multi, onBack = ::finish)

    PbPinnedHeaderScreen(
        testTag = "screen.pickPeople",
        header = {
            PbPushHeader(
                route.title
                    ?: if (multi) stringResource(R.string.add_split_with)
                    else stringResource(R.string.add_choose_someone),
                onBack = { if (multi) finish() else navigator.back() },
                action =
                    if (multi) PbHeaderAction.Text(stringResource(R.string.add_done), ::finish)
                    else null,
                testTag = tag,
                actionTag = "done",
            )
        },
    ) {
        PbTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.add_people_search),
            leadingIcon = PbIcon.Search,
            onClear = { query = "" },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Search,
                ),
            fieldModifier = Modifier.testTag("$tag.search"),
        )
        val chips = picked.filter { it != ME }
        if (multi && chips.isNotEmpty()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
            ) {
                chips.forEach { id ->
                    PbCategoryChip(
                        people.first(id),
                        Modifier.testTag("$tag.chip.$id"),
                        onClick = { choose(id) },
                        leading = PbChipLeading.Avatar(people.avatar(id)),
                        onRemove = { choose(id) },
                    )
                }
            }
        }
        val matches = friends.filter {
            matchesSearch(it.name, it.username, it.contact, query = query)
        }
        if (query.isBlank()) {
            if (route.includesYou) {
                PbCard {
                    PbPersonRow(
                        stringResource(R.string.add_you),
                        people.avatar(ME),
                        Modifier.testTag("$tag.you"),
                        subtitle = people.full(ME),
                        trailing = if (multi) PbPersonTrailing.Select(ME in picked) else null,
                        onCard = true,
                        onClick = { choose(ME) },
                        showDivider = false,
                    )
                }
            }
            PbSheetRow(
                stringResource(R.string.add_new_friend),
                subtitle = null,
                icon = PbIcon.UserAdd,
                onClick = {
                    known = friends.map { it.id }
                    navigator.open(Route.AddFriend)
                },
                modifier = Modifier.testTag("$tag.addFriend"),
            )
        } else if (matches.isEmpty()) {
            val invite = EmailOrPhone.matches(query.trim())
            PbSheetRow(
                if (invite) stringResource(R.string.add_invite, query.trim())
                else stringResource(R.string.add_as_guest, query.trim()),
                subtitle = null,
                icon = PbIcon.UserAdd,
                onClick = ::addGuest,
                modifier = Modifier.testTag("$tag.addGuest"),
                showChevron = false,
            )
        }
        if (matches.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                PbSectionHeader(stringResource(R.string.add_friends))
                FriendList(matches, picked, multi, people, tag, ::choose)
            }
        }
    }
}

@Composable
private fun FriendList(
    friends: List<Person>,
    picked: List<String>,
    multi: Boolean,
    people: PeopleDirectory,
    tag: String,
    onChoose: (String) -> Unit,
) {
    PbCard {
        friends.forEachIndexed { index, friend ->
            PbPersonRow(
                friend.name,
                people.avatar(friend.id),
                Modifier.testTag("$tag.friend.${friend.id}"),
                tag = stringResource(R.string.add_guest).takeIf { friend.isGuest },
                trailing = if (multi) PbPersonTrailing.Select(friend.id in picked) else null,
                onCard = true,
                onClick = { onChoose(friend.id) },
                showDivider = index < friends.lastIndex,
            )
        }
    }
}
