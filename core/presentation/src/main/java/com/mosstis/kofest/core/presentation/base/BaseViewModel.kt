package com.mosstis.kofest.core.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosstis.kofest.core.presentation.contract.UiAction
import com.mosstis.kofest.core.presentation.contract.UiEffect
import com.mosstis.kofest.core.presentation.contract.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<
    S : UiState,
    A : UiAction,
    E : UiEffect,
>(
    initialState: S,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<E>(
        capacity = Channel.BUFFERED,
    )
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    protected val currentState: S
        get() = _uiState.value

    fun onAction(action: A) {
        handleAction(action)
    }

    protected abstract fun handleAction(action: A)

    protected fun updateState(
        reducer: S.() -> S,
    ) {
        _uiState.update { state ->
            state.reducer()
        }
    }

    protected fun setState(state: S) {
        _uiState.value = state
    }

    protected fun sendEffect(effect: E) {
        _uiEffect.trySend(effect)
    }

    protected fun launchCatching(
        onError: (Throwable) -> Unit,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        return viewModelScope.launch {
            try {
                block()
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }
}
