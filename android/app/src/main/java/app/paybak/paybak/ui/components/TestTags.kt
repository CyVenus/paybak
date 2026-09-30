package app.paybak.paybak.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Tags a component's part for UI tests: "[parent].[part]", e.g. "record.parties.from" (flow.md
 * `<screen>.<element>` convention). Components with several tappable parts take the screen's tag
 * and derive their parts' tags from it; nothing is tagged when [parent] is null.
 */
internal fun Modifier.partTag(parent: String?, part: String? = null): Modifier =
    when {
        parent == null -> this
        part == null -> testTag(parent)
        else -> testTag("$parent.$part")
    }
