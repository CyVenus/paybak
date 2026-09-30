package app.paybak.paybak.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.feature.home.HomeScreen
import app.paybak.paybak.feature.launch.GetStartedScreen
import app.paybak.paybak.feature.launch.SplashScreen
import app.paybak.paybak.feature.launch.WelcomeScreen
import app.paybak.paybak.feature.setup.AllSetScreen
import app.paybak.paybak.feature.setup.SetupScreen
import app.paybak.paybak.feature.signin.SignInScreen
import app.paybak.paybak.feature.signin.VerifyScreen
import app.paybak.paybak.navigation.Destination.AllSet
import app.paybak.paybak.navigation.Destination.GetStarted
import app.paybak.paybak.navigation.Destination.Home
import app.paybak.paybak.navigation.Destination.Setup
import app.paybak.paybak.navigation.Destination.SignIn
import app.paybak.paybak.navigation.Destination.Splash
import app.paybak.paybak.navigation.Destination.Verify
import app.paybak.paybak.navigation.Destination.Welcome
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion

/**
 * The root flow: shows the top of [navigator]'s stack and wires every screen's navigation to the
 * flow.md table. Splash goes to Home when onboarding is complete, otherwise to Welcome.
 */
@Composable
fun AppFlow(navigator: AppNavigator, profileStore: ProfileStore) {
    val profile by profileStore.profile.collectAsState()
    BackHandler(enabled = navigator.handlesBack, onBack = navigator::back)

    AnimatedContent(
        targetState = navigator.current,
        modifier = Modifier.fillMaxSize().background(PbColors.Bg.Primary),
        transitionSpec = { transitionFor(navigator.lastTransition) },
        contentKey = { it.screenKey },
        label = "AppFlow",
    ) { destination ->
        when (destination) {
            Splash ->
                SplashScreen(
                    onFinished = {
                        val next =
                            if (profile.onboardingComplete) Home(HomeState.FirstDay) else Welcome(1)
                        navigator.resetTo(next, NavTransition.Dissolve)
                    }
                )

            is Welcome ->
                WelcomeScreen(
                    step = destination.step,
                    onStepChange = { navigator.replace(Welcome(it)) },
                    onSkip = { navigator.push(GetStarted, NavTransition.Crossfade) },
                    onGetStarted = { navigator.push(GetStarted) },
                )

            GetStarted -> {
                // No real Apple or Google sign-in yet: record the choice and go straight to setup.
                val continueWith = { method: SignInMethod ->
                    profileStore.update { it.copy(signInMethod = method, contact = "") }
                    navigator.push(Setup(1))
                }
                GetStartedScreen(
                    onContinueWithApple = { continueWith(SignInMethod.Apple) },
                    onContinueWithGoogle = { continueWith(SignInMethod.Google) },
                    onContinueWithEmailOrPhone = { navigator.push(SignIn) },
                )
            }

            SignIn ->
                SignInScreen(
                    onCodeSent = { navigator.push(Verify(wrongCode = false)) },
                    onBack = navigator::back,
                )

            is Verify ->
                VerifyScreen(
                    wrongCode = destination.wrongCode,
                    onWrongCodeChange = { navigator.replace(Verify(it)) },
                    onVerified = { navigator.push(Setup(1)) },
                    onBack = navigator::back,
                )

            is Setup ->
                SetupScreen(
                    step = destination.step,
                    onContinue = {
                        if (destination.step < Destination.SETUP_STEPS) {
                            navigator.replace(Setup(destination.step + 1))
                        } else {
                            navigator.push(AllSet)
                        }
                    },
                    onBack = navigator::back,
                )

            AllSet -> {
                LaunchedEffect(Unit) { profileStore.update { it.copy(onboardingComplete = true) } }
                AllSetScreen(
                    onGoHome = {
                        navigator.resetTo(Home(HomeState.FirstDay), NavTransition.Dissolve)
                    }
                )
            }

            is Home ->
                HomeScreen(
                    state = destination.state,
                    addSheetOpen = destination.addSheetOpen,
                    onAddSheetOpenChange = {
                        navigator.replace(destination.copy(addSheetOpen = it))
                    },
                )
        }
    }
}

private fun AnimatedContentTransitionScope<Destination>.transitionFor(
    transition: NavTransition
): ContentTransform {
    val slide = tween<IntOffset>(PbMotion.PUSH_MILLIS, easing = PbMotion.EaseInOut)
    return when (transition) {
        NavTransition.Push ->
            slideInHorizontally(slide) { it } togetherWith slideOutHorizontally(slide) { -it / 3 }

        NavTransition.Pop ->
            (slideInHorizontally(slide) { -it / 3 } togetherWith slideOutHorizontally(slide) { it })
                .apply { targetContentZIndex = -1f }

        NavTransition.Dissolve ->
            fadeIn(tween(PbMotion.DISSOLVE_MILLIS, easing = PbMotion.EaseOut)) togetherWith
                fadeOut(tween(PbMotion.DISSOLVE_MILLIS, easing = PbMotion.EaseOut))

        NavTransition.Crossfade ->
            fadeIn(tween(PbMotion.SKIP_MILLIS, easing = PbMotion.EaseInOut)) togetherWith
                fadeOut(tween(PbMotion.SKIP_MILLIS, easing = PbMotion.EaseInOut))
    }
}
