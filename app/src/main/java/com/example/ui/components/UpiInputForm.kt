package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpiInputForm(
    vpa: String,
    onVpaChanged: (String) -> Unit,
    vpaError: String?,
    payeeName: String,
    onPayeeNameChanged: (String) -> Unit,
    amount: String,
    onAmountChanged: (String) -> Unit,
    amountError: String?,
    onQuickAmountSelected: (String) -> Unit,
    note: String,
    onNoteChanged: (String) -> Unit,
    isBengali: Boolean,
    onSaveProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val quickAmounts = listOf("50", "100", "120", "250", "500", "1000")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("upi_input_form_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "পেমেন্টের তথ্য" else "Payment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (vpa.isNotBlank()) {
                    OutlinedButton(
                        onClick = onSaveProfileClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_as_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "সংরক্ষণ" else "Save Preset",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // 1. UPI ID (VPA) Field
            Column {
                Text(
                    text = if (isBengali) "UPI আইডি" else "UPI ID (VPA)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = vpa,
                    onValueChange = onVpaChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vpa_input"),
                    placeholder = {
                        Text(
                            text = if (isBengali) "যেমন: yourname@okaxis" else "e.g. yourname@okaxis",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (vpa.isNotEmpty()) {
                            IconButton(onClick = { onVpaChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    isError = vpaError != null,
                    supportingText = {
                        if (vpaError != null) {
                            Text(
                                text = vpaError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = if (isBengali) "GPay, PhonePe, Paytm বা যেকোনো UPI আইডি" else "Supported handles: @okaxis, @okhdfcbank, @ybl, @paytm, @upi etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // 2. Payee Name Field
            Column {
                Text(
                    text = if (isBengali) "নাম (গ্রাহকের স্ক্রিনে দেখাবে)" else "Payee Name (Shown to customer)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = payeeName,
                    onValueChange = onPayeeNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payee_name_input"),
                    placeholder = {
                        Text(
                            text = if (isBengali) "যেমন: Poster Studio" else "e.g. Poster Studio",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (payeeName.isNotEmpty()) {
                            IconButton(onClick = { onPayeeNameChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // 3. Amount Field
            Column {
                Text(
                    text = if (isBengali) "টাকার অঙ্ক (ঐচ্ছিক)" else "Amount in ₹ (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    placeholder = {
                        Text(
                            text = if (isBengali) "ফাঁকা রাখলে গ্রাহক নিজে লিখবেন" else "Leave blank for customer to enter",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CurrencyRupee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (amount.isNotEmpty()) {
                            IconButton(onClick = { onAmountChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(
                                text = amountError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick Amount Chips
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.forEach { amt ->
                        val isSelected = amount == amt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onQuickAmountSelected(amt) },
                            label = {
                                Text(
                                    text = "₹$amt",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("chip_amt_$amt")
                        )
                    }
                }
            }

            // 4. Note Field
            Column {
                Text(
                    text = if (isBengali) "নোট (ঐচ্ছিক)" else "Note / Remarks (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input"),
                    placeholder = {
                        Text(
                            text = if (isBengali) "যেমন: পোস্টার অর্ডার" else "e.g. Poster Order",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (note.isNotEmpty()) {
                            IconButton(onClick = { onNoteChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
