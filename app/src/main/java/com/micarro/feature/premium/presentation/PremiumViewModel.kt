package com.micarro.feature.premium.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.feature.premium.data.PremiumStore
import com.micarro.feature.premium.domain.PremiumStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PayMethod {
    CARD,
    NEQUI
}

data class CheckoutUi(
    val visible: Boolean = false,
    val method: PayMethod = PayMethod.CARD,
    val paying: Boolean = false,
    val confirmed: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val store: PremiumStore
) : ViewModel() {

    val status: StateFlow<PremiumStatus> = store.status

    private val _checkout = MutableStateFlow(CheckoutUi())
    val checkout: StateFlow<CheckoutUi> = _checkout.asStateFlow()

    init {
        store.refresh()
    }

    fun startTrial() {
        store.startTrial()
    }

    fun refresh() {
        store.refresh()
    }

    fun openCheckout() {
        _checkout.value = CheckoutUi(visible = true)
    }

    fun dismissCheckout() {
        if (_checkout.value.paying) return
        _checkout.update { it.copy(visible = false) }
    }

    fun selectMethod(method: PayMethod) {
        _checkout.update { it.copy(method = method) }
    }

    fun confirmPayment() {
        if (_checkout.value.paying) return
        viewModelScope.launch {
            _checkout.update { it.copy(paying = true) }
            delay(PAYMENT_DELAY_MS)
            store.purchase()
            _checkout.value = CheckoutUi(confirmed = true)
        }
    }

    fun dismissConfirmation() {
        _checkout.value = CheckoutUi()
    }

    private companion object {
        const val PAYMENT_DELAY_MS = 1_200L
    }
}
