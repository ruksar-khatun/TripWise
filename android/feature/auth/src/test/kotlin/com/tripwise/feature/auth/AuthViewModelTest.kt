package com.tripwise.feature.auth

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.SessionState
import com.tripwise.core.testing.fakes.FakeAuthRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `no errors before first submit; client validation blocks the call`() = runTest {
        val auth = FakeAuthRepository()
        val vm = SignUpViewModel(auth)
        vm.onAction(AuthAction.EmailChanged("bad"))
        assertTrue(vm.uiState.value.problems.isEmpty())

        vm.onAction(AuthAction.Submit)
        assertEquals(
            mapOf(AuthField.NAME to FieldProblem.NAME_REQUIRED, AuthField.EMAIL to FieldProblem.EMAIL_INVALID, AuthField.PASSWORD to FieldProblem.PASSWORD_REQUIRED),
            vm.uiState.value.problems,
        )
        assertTrue(auth.calls.isEmpty())

        // After the first attempt, errors update live as the user types.
        vm.onAction(AuthAction.PasswordChanged("short"))
        assertEquals(FieldProblem.PASSWORD_SHORT, vm.uiState.value.problems[AuthField.PASSWORD])
    }

    @Test fun `successful sign in shows loading then signs the session in`() = runTest {
        val auth = FakeAuthRepository().apply { gate = CompletableDeferred() }
        val vm = SignInViewModel(auth)
        vm.onAction(AuthAction.EmailChanged("a@b.co"))
        vm.onAction(AuthAction.PasswordChanged("whatever"))
        vm.onAction(AuthAction.Submit)
        assertTrue(vm.uiState.value.submitting)
        vm.onAction(AuthAction.Submit) // double tap ignored
        auth.gate!!.complete(Unit)
        assertFalse(vm.uiState.value.submitting)
        assertEquals(listOf("signIn:a@b.co"), auth.calls)
        assertTrue(auth.session.value is SessionState.SignedIn)
    }

    @Test fun `wrong credentials show a form error; server field errors map to fields`() = runTest {
        val auth = FakeAuthRepository().apply { nextResult = Outcome.Failure(AppError.InvalidCredentials) }
        val signIn = SignInViewModel(auth)
        signIn.onAction(AuthAction.EmailChanged("a@b.co"))
        signIn.onAction(AuthAction.PasswordChanged("whatever"))
        signIn.onAction(AuthAction.Submit)
        assertEquals(AppError.InvalidCredentials, signIn.uiState.value.formError)

        auth.nextResult = Outcome.Failure(AppError.Validation(mapOf("body.email" to "An account with this email already exists")))
        val signUp = SignUpViewModel(auth)
        signUp.onAction(AuthAction.NameChanged("Ann"))
        signUp.onAction(AuthAction.EmailChanged("a@b.co"))
        signUp.onAction(AuthAction.PasswordChanged("longenough"))
        signUp.onAction(AuthAction.Submit)
        assertEquals("An account with this email already exists", signUp.uiState.value.serverFieldErrors[AuthField.EMAIL])
        signUp.onAction(AuthAction.EmailChanged("b@b.co"))
        assertTrue(signUp.uiState.value.serverFieldErrors.isEmpty())
    }
}
