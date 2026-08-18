package com.telegramdrive.uploader

import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.feature.telegram.TelegramAuthViewModel
import com.telegramdrive.uploader.feature.telegram.TelegramDestinationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramAuthTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeTelegramRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTelegramRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAuthViewModelInitialState() = runTest {
        val viewModel = TelegramAuthViewModel(fakeRepository)
        assertEquals(TelegramConnectionState.DISCONNECTED, viewModel.connectionState.value)
        assertNull(viewModel.currentUser.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isProcessing.value)
    }

    @Test
    fun testAuthFlowTransitions() = runTest {
        val viewModel = TelegramAuthViewModel(fakeRepository)

        // 1. Connect clicked -> Should call connect
        viewModel.connect()
        testScheduler.advanceUntilIdle()
        assertEquals(TelegramConnectionState.WAITING_FOR_PHONE, viewModel.connectionState.value)

        // 2. Submit Phone Number
        viewModel.phoneNumberInput.value = "+123456789"
        viewModel.sendPhoneNumber()
        testScheduler.advanceUntilIdle()
        assertEquals(TelegramConnectionState.WAITING_FOR_CODE, viewModel.connectionState.value)

        // 3. Submit Code
        viewModel.codeInput.value = "12345"
        viewModel.sendCode()
        testScheduler.advanceUntilIdle()
        
        // Assert state becomes AUTHORIZED
        assertEquals(TelegramConnectionState.AUTHORIZED, viewModel.connectionState.value)
        assertNotNull(viewModel.currentUser.value)
        assertEquals("John", viewModel.currentUser.value?.firstName)
    }

    @Test
    fun testTwoFactorAuthTransition() = runTest {
        val viewModel = TelegramAuthViewModel(fakeRepository)

        // Transition to waiting for phone -> code
        fakeRepository.connectionState.value = TelegramConnectionState.WAITING_FOR_CODE
        
        // Submit code requiring 2FA
        viewModel.codeInput.value = "2222"
        viewModel.sendCode()
        testScheduler.advanceUntilIdle()

        assertEquals(TelegramConnectionState.WAITING_FOR_PASSWORD, viewModel.connectionState.value)

        // Submit correct password
        viewModel.passwordInput.value = "password"
        viewModel.sendPassword()
        testScheduler.advanceUntilIdle()

        assertEquals(TelegramConnectionState.AUTHORIZED, viewModel.connectionState.value)
        assertNotNull(viewModel.currentUser.value)
    }

    @Test
    fun testAuthFlowInvalidInputErrors() = runTest {
        val viewModel = TelegramAuthViewModel(fakeRepository)

        // Transition to phone screen
        fakeRepository.connectionState.value = TelegramConnectionState.WAITING_FOR_PHONE
        viewModel.phoneNumberInput.value = "" // blank
        viewModel.sendPhoneNumber()
        testScheduler.advanceUntilIdle()
        
        assertEquals(TelegramError.InvalidPhoneNumber, viewModel.error.value)

        // Clear error
        viewModel.clearError()
        assertNull(viewModel.error.value)
    }

    @Test
    fun testLogoutClearsSession() = runTest {
        val viewModel = TelegramAuthViewModel(fakeRepository)
        
        // Login first
        fakeRepository.connectionState.value = TelegramConnectionState.AUTHORIZED
        fakeRepository.currentUser.value = TelegramUser(1, "John", "Doe", "johndoe", "+123", null)

        viewModel.logout()
        testScheduler.advanceUntilIdle()

        assertEquals(TelegramConnectionState.DISCONNECTED, viewModel.connectionState.value)
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun testDestinationSelectionAndSearching() = runTest {
        val viewModel = TelegramDestinationViewModel(fakeRepository)
        
        // Initial state
        assertNull(viewModel.selectedDestination.value)
        assertEquals("", viewModel.searchQuery.value)

        // Search Query Changes
        viewModel.onSearchQueryChanged("channel")
        testScheduler.advanceTimeBy(500)
        testScheduler.runCurrent()
        
        val list = viewModel.destinations.first()
        // There should be only 1 destination matching "channel"
        assertEquals("List was: $list", 1, list.size)
        assertEquals("My Channel", list[0].title)

        // Select Destination
        val target = list[0]
        viewModel.selectDestination(target)
        assertEquals(target, viewModel.selectedDestination.value)

        // Clear selection
        viewModel.clearSelection()
        assertNull(viewModel.selectedDestination.value)
    }

    // Comprehensive Fake repository for robust, offline VM test suite
    class FakeTelegramRepository : TelegramRepository {
        override val connectionState = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
        override val currentUser = MutableStateFlow<TelegramUser?>(null)
        override val error = MutableStateFlow<TelegramError?>(null)
        override val isConfigured = true

        private val destinationsList = listOf(
            TelegramDestination(1L, "Saved Messages", "me", TelegramDestinationType.USER, null, true),
            TelegramDestination(2L, "My Channel", "my_channel", TelegramDestinationType.CHANNEL, null, true),
            TelegramDestination(3L, "Work Group", null, TelegramDestinationType.GROUP, null, true)
        )

        override suspend fun connect() {
            connectionState.value = TelegramConnectionState.WAITING_FOR_PHONE
        }

        override suspend fun sendPhoneNumber(phoneNumber: String) {
            if (phoneNumber.isBlank()) {
                error.value = TelegramError.InvalidPhoneNumber
            } else {
                connectionState.value = TelegramConnectionState.WAITING_FOR_CODE
            }
        }

        override suspend fun sendCode(code: String) {
            if (code == "2222") {
                connectionState.value = TelegramConnectionState.WAITING_FOR_PASSWORD
            } else if (code == "12345") {
                currentUser.value = TelegramUser(123456L, "John", "Doe", "johndoe", "+123456789", null)
                connectionState.value = TelegramConnectionState.AUTHORIZED
            } else {
                error.value = TelegramError.InvalidCode
            }
        }

        override suspend fun sendPassword(password: String) {
            if (password == "password") {
                currentUser.value = TelegramUser(123456L, "John", "Doe", "johndoe", "+123456789", null)
                connectionState.value = TelegramConnectionState.AUTHORIZED
            } else {
                error.value = TelegramError.InvalidPassword
            }
        }

        override suspend fun logout() {
            currentUser.value = null
            connectionState.value = TelegramConnectionState.DISCONNECTED
        }

        override fun clearError() {
            error.value = null
        }

        override fun getDestinations(query: String): Flow<List<TelegramDestination>> {
            val filtered = if (query.isBlank()) {
                destinationsList
            } else {
                destinationsList.filter { it.title.contains(query, ignoreCase = true) }
            }
            return flowOf(filtered)
        }
    }
}
