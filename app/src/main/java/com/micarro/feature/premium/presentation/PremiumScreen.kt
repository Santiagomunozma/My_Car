package com.micarro.feature.premium.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.R
import com.micarro.feature.premium.domain.PremiumRules
import com.micarro.feature.premium.domain.PremiumTier
import com.micarro.ui.components.AquaBackground
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroSheet
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val checkout by viewModel.checkout.collectAsStateWithLifecycle()
    val price = PremiumRules.formatPrice()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        AquaBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (status.tier == PremiumTier.PREMIUM) {
                    MiCarroCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.premium_active),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                stringResource(R.string.premium_active_body, price),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlanColumn(
                            title = stringResource(R.string.premium_free_title),
                            items = listOf(
                                false to stringResource(R.string.premium_benefit_ads),
                                false to stringResource(R.string.premium_benefit_assistant),
                                false to stringResource(R.string.premium_benefit_photos),
                                false to stringResource(R.string.premium_benefit_export),
                                false to stringResource(R.string.premium_benefit_backup),
                                true to stringResource(R.string.premium_benefit_csv)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        PlanColumn(
                            title = stringResource(R.string.premium_paid_title),
                            items = listOf(
                                true to stringResource(R.string.premium_benefit_no_ads),
                                true to stringResource(R.string.premium_benefit_assistant),
                                true to stringResource(R.string.premium_benefit_photos),
                                true to stringResource(R.string.premium_benefit_export),
                                true to stringResource(R.string.premium_benefit_backup)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = price,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(R.string.premium_one_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (status.tier == PremiumTier.TRIAL) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.premium_trial_days,
                                status.trialDaysLeft,
                                status.trialDaysLeft
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (status.canStartTrial) {
                        PrimaryButton(
                            text = stringResource(R.string.premium_start_trial),
                            onClick = viewModel::startTrial
                        )
                        SecondaryButton(
                            text = stringResource(R.string.premium_pay, price),
                            onClick = viewModel::openCheckout
                        )
                    } else {
                        PrimaryButton(
                            text = stringResource(R.string.premium_pay, price),
                            onClick = viewModel::openCheckout
                        )
                    }
                    Text(
                        text = stringResource(R.string.premium_simulated_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (checkout.visible) {
        MiCarroSheet(
            onDismiss = viewModel::dismissCheckout,
            title = stringResource(R.string.premium_checkout_title)
        ) {
            Text(
                text = stringResource(R.string.premium_checkout_body, price),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = checkout.method == PayMethod.CARD,
                    onClick = { viewModel.selectMethod(PayMethod.CARD) },
                    enabled = !checkout.paying,
                    label = { Text(stringResource(R.string.premium_method_card)) }
                )
                FilterChip(
                    selected = checkout.method == PayMethod.NEQUI,
                    onClick = { viewModel.selectMethod(PayMethod.NEQUI) },
                    enabled = !checkout.paying,
                    label = { Text(stringResource(R.string.premium_method_nequi)) }
                )
            }
            PrimaryButton(
                text = stringResource(R.string.premium_pay, price),
                onClick = viewModel::confirmPayment,
                loading = checkout.paying,
                enabled = !checkout.paying,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.premium_simulated_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (checkout.confirmed) {
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmation,
            title = { Text(stringResource(R.string.premium_paid_done)) },
            text = { Text(stringResource(R.string.premium_paid_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissConfirmation) {
                    Text(stringResource(R.string.action_accept))
                }
            }
        )
    }
}

@Composable
private fun PlanColumn(
    title: String,
    items: List<Pair<Boolean, String>>,
    modifier: Modifier = Modifier
) {
    MiCarroCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            items.forEach { (included, label) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (included) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (included) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
