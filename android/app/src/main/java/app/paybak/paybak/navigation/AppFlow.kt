package app.paybak.paybak.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.data.ledger.isPro
import app.paybak.paybak.feature.launch.GetStartedScreen
import app.paybak.paybak.feature.launch.SplashScreen
import app.paybak.paybak.feature.launch.WelcomeScreen
import app.paybak.paybak.feature.setup.AllSetScreen
import app.paybak.paybak.feature.setup.SetupScreen
import app.paybak.paybak.feature.signin.SignInScreen
import app.paybak.paybak.feature.signin.VerifyScreen
import app.paybak.paybak.navigation.Destination.AllSet
import app.paybak.paybak.navigation.Destination.GetStarted
import app.paybak.paybak.navigation.Destination.Main
import app.paybak.paybak.navigation.Destination.Setup
import app.paybak.paybak.navigation.Destination.SignIn
import app.paybak.paybak.navigation.Destination.Splash
import app.paybak.paybak.navigation.Destination.Verify
import app.paybak.paybak.navigation.Destination.Welcome
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion

/**
 * The root flow: shows the top of [navigator]'s stack and wires every screen's navigation to the
 * flow.md table. Splash goes to the app ([Main]) when onboarding is complete, otherwise to Welcome.
 *
 * @param mainStart Where the app opens (Home, or a debug start screen's stack).
 * @param links Internal links from notifications, opened once the app shows.
 * @param ledger The ledger store, first needed when the app shows.
 */
@Composable
fun AppFlow(
    navigator: AppNavigator,
    profileStore: ProfileStore,
    mainStart: MainState,
    links: DeepLinkInbox,
    ledger: () -> LedgerRepository,
) {
    val profile by profileStore.profile.collectAsState()
    BackHandler(enabled = navigator.handlesBack, onBack = navigator::back)

    AnimatedContent(
        targetState = navigator.current,
        modifier = Modifier.fillMaxSize().background(PbColors.Bg.Primary),
        transitionSpec = { transitionFor(navigator.lastTransition) },
        contentKey = { it.screenKey },
        label = "AppFlow",
    ) { destination ->
        // Only the screen on top navigates, so a quick second tap on the outgoing one does nothing.
        fun navigate(navigation: AppNavigator.() -> Unit) = navigator.from(destination, navigation)

        when (destination) {
            Splash ->
                SplashScreen(
                    onFinished = {
                        navigate {
                            val next = if (profile.onboardingComplete) Main else Welcome(1)
                            resetTo(next, NavTransition.Dissolve)
                        }
                    }
                )

            is Welcome ->
                WelcomeScreen(
                    step = destination.step,
                    onStepChange = { step -> navigate { replace(Welcome(step)) } },
                    onSkip = { navigate { push(GetStarted, NavTransition.Crossfade) } },
                    onGetStarted = { navigate { push(GetStarted) } },
                )

            GetStarted -> {
                // No real Apple or Google sign-in yet: record the choice and go straight to setup.
                val continueWith = { method: SignInMethod ->
                    navigate {
                        profileStore.update { it.copy(signInMethod = method, contact = "") }
                        push(Setup(1))
                    }
                }
                GetStartedScreen(
                    onContinueWithApple = { continueWith(SignInMethod.Apple) },
                    onContinueWithGoogle = { continueWith(SignInMethod.Google) },
                    onContinueWithEmailOrPhone = { navigate { push(SignIn) } },
                )
            }

            SignIn ->
                SignInScreen(
                    initialContact = profile.contact,
                    onSendCode = { contact ->
                        navigate {
                            profileStore.update {
                                it.copy(signInMethod = contact.method, contact = contact.value)
                            }
                            push(Verify())
                        }
                    },
                    onBack = { navigate { back() } },
                )

            is Verify ->
                VerifyScreen(
                    contact = profile.contact,
                    rejectedCode = destination.rejectedCode,
                    onRejectedCodeChange = { code -> navigate { replace(Verify(code)) } },
                    onVerified = { navigate { push(Setup(1)) } },
                    onBack = { navigate { back() } },
                )

            is Setup ->
                SetupScreen(
                    step = destination.step,
                    profileStore = profileStore,
                    onNext = {
                        navigate {
                            if (destination.step < Destination.SETUP_STEPS) {
                                replace(Setup(destination.step + 1))
                            } else {
                                push(AllSet)
                            }
                        }
                    },
                    onBack = { navigate { back() } },
                )

            AllSet -> {
                LaunchedEffect(Unit) { profileStore.update { it.copy(onboardingComplete = true) } }
                AllSetScreen(
                    firstName = profile.firstName,
                    onGoHome = {
                        navigate { resetTo(Main, NavTransition.Dissolve) }
                    },
                )
            }

            Main -> MainRoot(mainStart, links, remember { ledger() })
        }
    }
}

private fun transitionFor(transition: NavTransition): ContentTransform =
    when (transition) {
        NavTransition.Push -> pushTransition()

        NavTransition.Pop -> popTransition()

        NavTransition.Dissolve ->
            fadeIn(tween(PbMotion.DISSOLVE_MILLIS, easing = PbMotion.EaseOut)) togetherWith
                fadeOut(tween(PbMotion.DISSOLVE_MILLIS, easing = PbMotion.EaseOut))

        NavTransition.Crossfade ->
            fadeIn(tween(PbMotion.SKIP_MILLIS, easing = PbMotion.EaseInOut)) togetherWith
                fadeOut(tween(PbMotion.SKIP_MILLIS, easing = PbMotion.EaseInOut))
    }

/** The app after onboarding, with its navigator saved across recreation and process death. */
@Composable
private fun MainRoot(start: MainState, links: DeepLinkInbox, ledger: LedgerRepository) {
    val isPro = { ledger.isPro }
    val navigator =
        rememberSaveable(
            saver =
                Saver(
                    save = { it.state.encode() },
                    restore = { MainNavigator(MainState.decode(it) ?: start, isPro) },
                )
        ) {
            MainNavigator(start, isPro)
        }
    LaunchedEffect(navigator) {
        links.pending.collect { link ->
            DeepLink.parse(link)?.let(navigator::open)
            links.consume(link)
        }
    }
    CompositionLocalProvider(LocalLedger provides ledger, LocalMainNavigator provides navigator) {
        MainHost(navigator)
    }
}
