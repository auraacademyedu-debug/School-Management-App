package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ALL_SCHOOL_CLASSES
import com.example.data.Enquiry
import com.example.data.SchoolConfig
import com.example.ui.DashboardMetrics
import com.example.ui.components.TopSummaryMetricsSection
import com.example.ui.components.formatAmount
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SidebarNavy

@Composable
fun DashboardAndEnquiryWorkspace(
    isDashboardOverview: Boolean,
    isWideScreen: Boolean,
    selectedSession: String,
    metrics: DashboardMetrics,
    config: SchoolConfig,
    enquiries: List<Enquiry>,
    onNavigate: (NavDestination) -> Unit,
    onSaveEnquiry: (
        studentName: String,
        fatherName: String,
        mobile: String,
        classInterested: String,
        address: String,
        remarks: String,
        issueFormNow: Boolean
    ) -> Unit,
    onUpdateEnquiry: (Enquiry, Boolean) -> Unit,
    onDeleteEnquiry: (Enquiry) -> Unit,
    onTakeAdmission: (Enquiry) -> Unit,
    onSavePrices: (Long, Map<String, Long>) -> Unit
) {
    var printPreviewEnquiry by remember { mutableStateOf<Enquiry?>(null) }
    var editingEnquiry by remember { mutableStateOf<Enquiry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Stats Bar (Always prominent on Dashboard & Enquiry views)
        TopSummaryMetricsSection(
            metrics = metrics,
            isWideScreen = isWideScreen,
            onCardClick = onNavigate
        )

        // Hero Banner on Main Dashboard Overview
        if (isDashboardOverview) {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_campus_hero),
                        contentDescription = "School Campus Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SidebarNavy.copy(alpha = 0.90f),
                                        SidebarNavy.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${config.schoolName} • $selectedSession",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Fee Revenue: ₹${formatAmount(metrics.totalFeeCollected)}  |  Expenses: ₹${formatAmount(metrics.totalExpenses)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onNavigate(NavDestination.NEW_ADMISSION) },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("hero_new_admission_btn")
                            ) {
                                Text("New Admission", color = SidebarNavy, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onNavigate(NavDestination.QUICK_FEE_COLLECT) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.testTag("hero_quick_fee_btn")
                            ) {
                                Text("Quick Fee Collect")
                            }
                        }
                    }
                }
            }
        }

        // 2. Admission Enquiry Module (Two Main Sections: New Enquiry Form + Enquiry List & Actions)
        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                NewEnquiryFormCard(
                    config = config,
                    onSaveEnquiry = onSaveEnquiry,
                    modifier = Modifier.weight(0.42f)
                )
                EnquiryListAndActionsCard(
                    enquiries = enquiries.filter { it.session == selectedSession },
                    onPrintForm = { enq ->
                        if (!enq.formIssued) {
                            onUpdateEnquiry(enq.copy(formIssued = true, status = "Form Issued"), true)
                        }
                        printPreviewEnquiry = enq.copy(formIssued = true)
                    },
                    onTakeAdmission = onTakeAdmission,
                    onEdit = { editingEnquiry = it },
                    onDelete = onDeleteEnquiry,
                    modifier = Modifier.weight(0.58f)
                )
            }
        } else {
            NewEnquiryFormCard(
                config = config,
                onSaveEnquiry = onSaveEnquiry,
                modifier = Modifier.fillMaxWidth()
            )
            EnquiryListAndActionsCard(
                enquiries = enquiries.filter { it.session == selectedSession },
                onPrintForm = { enq ->
                    if (!enq.formIssued) {
                        onUpdateEnquiry(enq.copy(formIssued = true, status = "Form Issued"), true)
                    }
                    printPreviewEnquiry = enq.copy(formIssued = true)
                },
                onTakeAdmission = onTakeAdmission,
                onEdit = { editingEnquiry = it },
                onDelete = onDeleteEnquiry,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Admission Form Price (Class-wise) Module
        AdmissionFormPricingSection(
            config = config,
            onSavePrices = onSavePrices
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Print Form Receipt Modal
    printPreviewEnquiry?.let { enq ->
        PrintAdmissionFormDialog(
            enquiry = enq,
            schoolConfig = config,
            onDismiss = { printPreviewEnquiry = null }
        )
    }

    // Edit Enquiry Dialog
    editingEnquiry?.let { enq ->
        EditEnquiryDialog(
            enquiry = enq,
            config = config,
            onDismiss = { editingEnquiry = null },
            onConfirm = { updated, newlyIssued ->
                editingEnquiry = null
                onUpdateEnquiry(updated, newlyIssued)
            }
        )
    }
}

@Composable
fun NewEnquiryFormCard(
    config: SchoolConfig,
    onSaveEnquiry: (
        studentName: String,
        fatherName: String,
        mobile: String,
        classInterested: String,
        address: String,
        remarks: String,
        issueFormNow: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var studentName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Nursery") }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var issueFormNow by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val resolvedFormPrice = config.resolveFormPriceForClass(selectedClass)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = RoyalBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "New Admission Enquiry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Register prospective student & issue admission form",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it; validationError = null },
                label = { Text("Student Name *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enquiry_student_name_input")
            )

            OutlinedTextField(
                value = fatherName,
                onValueChange = { fatherName = it; validationError = null },
                label = { Text("Father's Name *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enquiry_father_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it.filter { ch -> ch.isDigit() || ch == '+' }.take(15); validationError = null },
                    label = { Text("Mobile Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("enquiry_mobile_input")
                )

                // Class Interested In Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedClass,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class Interested In") },
                        trailingIcon = {
                            IconButton(onClick = { classDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { classDropdownExpanded = true }
                            .testTag("enquiry_class_dropdown")
                    )
                    DropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        ALL_SCHOOL_CLASSES.forEach { cls ->
                            DropdownMenuItem(
                                text = {
                                    Text("$cls  (Form: ₹${config.resolveFormPriceForClass(cls)})")
                                },
                                onClick = {
                                    selectedClass = cls
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enquiry_address_input")
            )

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks / Follow-up Notes") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enquiry_remarks_input")
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { issueFormNow = !issueFormNow }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = issueFormNow,
                            onCheckedChange = { issueFormNow = it },
                            modifier = Modifier.testTag("enquiry_issue_form_checkbox")
                        )
                        Text(
                            text = "Issue Admission Form Now",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Fee: ₹$resolvedFormPrice",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (studentName.isBlank() || fatherName.isBlank() || mobileNumber.length < 6) {
                        validationError = "Please enter Student Name, Father's Name, and a valid Mobile Number."
                    } else {
                        onSaveEnquiry(
                            studentName,
                            fatherName,
                            mobileNumber,
                            selectedClass,
                            address.ifBlank { "Local" },
                            remarks.ifBlank { "General Enquiry" },
                            issueFormNow
                        )
                        studentName = ""
                        fatherName = ""
                        mobileNumber = ""
                        address = ""
                        remarks = ""
                        validationError = null
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_enquiry_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Enquiry")
            }
        }
    }
}

@Composable
fun EnquiryListAndActionsCard(
    enquiries: List<Enquiry>,
    onPrintForm: (Enquiry) -> Unit,
    onTakeAdmission: (Enquiry) -> Unit,
    onEdit: (Enquiry) -> Unit,
    onDelete: (Enquiry) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val statusFilters = listOf("All", "Pending", "Form Issued", "Admitted")

    val filteredEnquiries = remember(enquiries, searchQuery, selectedStatusFilter) {
        enquiries.filter { enq ->
            val matchesSearch = searchQuery.isBlank() ||
                enq.studentName.contains(searchQuery, ignoreCase = true) ||
                enq.fatherName.contains(searchQuery, ignoreCase = true) ||
                enq.mobileNumber.contains(searchQuery, ignoreCase = true) ||
                enq.classInterested.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "All" -> true
                "Form Issued" -> enq.formIssued || enq.status == "Form Issued"
                else -> enq.status == selectedStatusFilter
            }
            matchesSearch && matchesStatus
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enquiry List & Form Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredEnquiries.size} matching student enquiries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, father, mobile, or class...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Enquiries") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enquiry_search_input")
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                statusFilters.forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status) },
                        modifier = Modifier.testTag("enquiry_filter_${status.lowercase().replace(" ", "_")}")
                    )
                }
            }

            HorizontalDivider()

            if (filteredEnquiries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No student enquiries found",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use the 'New Admission Enquiry' form to log prospective students.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredEnquiries.forEach { enquiry ->
                        EnquiryRowCard(
                            enquiry = enquiry,
                            onPrintForm = { onPrintForm(enquiry) },
                            onTakeAdmission = { onTakeAdmission(enquiry) },
                            onEdit = { onEdit(enquiry) },
                            onDelete = { onDelete(enquiry) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnquiryRowCard(
    enquiry: Enquiry,
    onPrintForm: () -> Unit,
    onTakeAdmission: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = when (enquiry.status) {
        "Admitted" -> EmeraldSuccess
        "Form Issued" -> RoyalBlue
        else -> AmberGold
    }
    val badgeBg = when (enquiry.status) {
        "Admitted" -> EmeraldContainer
        "Form Issued" -> RoyalBlueContainer
        else -> AmberGoldContainer
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .testTag("enquiry_item_${enquiry.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = enquiry.studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Father: ${enquiry.fatherName} • Mob: ${enquiry.mobileNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target: ${enquiry.classInterested} • Addr: ${enquiry.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (enquiry.remarks.isNotBlank()) {
                        Text(
                            text = "Note: ${enquiry.remarks}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = enquiry.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (enquiry.formIssued) "Form Issued • ₹${enquiry.formPrice}" else "Form Pending (₹${enquiry.formPrice})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enquiry.formIssued) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPrintForm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("print_form_btn_${enquiry.id}")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print Form", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Form", style = MaterialTheme.typography.labelMedium)
                    }

                    if (enquiry.status != "Admitted") {
                        Button(
                            onClick = onTakeAdmission,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.testTag("take_admission_btn_${enquiry.id}")
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = "Take Admission", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Admission", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_enquiry_btn_${enquiry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Enquiry",
                            tint = RoyalBlue
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_enquiry_btn_${enquiry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Enquiry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdmissionFormPricingSection(
    config: SchoolConfig,
    onSavePrices: (Long, Map<String, Long>) -> Unit
) {
    var defaultPriceText by remember(config.defaultFormPrice) {
        mutableStateOf(config.defaultFormPrice.toString())
    }

    val classPriceInputs = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(config.classFormPrices) {
        classPriceInputs.clear()
        ALL_SCHOOL_CLASSES.forEach { cls ->
            val existingVal = config.classFormPrices[cls]
            classPriceInputs[cls] = if (existingVal != null && existingVal > 0L) existingVal.toString() else ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admission_form_pricing_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PriceChange,
                    contentDescription = null,
                    tint = AmberGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Admission Form Price (Class-wise)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set a default form fee and override custom form prices from Nursery to Class 12",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Default Price Input
            OutlinedTextField(
                value = defaultPriceText,
                onValueChange = { defaultPriceText = it.filter { ch -> ch.isDigit() }.take(6) },
                label = { Text("Default Form Price (₹) for classes without custom price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("default_form_price_input")
            )

            // Dynamic Grid of Classes (Nursery, LKG, UKG, Class 1 .. Class 12)
            Text(
                text = "Class-Specific Form Fees (Leave blank to use Default ₹${defaultPriceText.ifBlank { "0" }})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            val chunkedClasses = ALL_SCHOOL_CLASSES.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chunkedClasses.forEach { rowClasses ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowClasses.forEach { cls ->
                            OutlinedTextField(
                                value = classPriceInputs[cls] ?: "",
                                onValueChange = { newVal ->
                                    classPriceInputs[cls] = newVal.filter { ch -> ch.isDigit() }.take(6)
                                },
                                label = { Text(cls) },
                                placeholder = { Text("₹${defaultPriceText.ifBlank { "250" }}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_price_input_${cls.lowercase().replace(" ", "_")}")
                            )
                        }
                        repeat(3 - rowClasses.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val defaultPrice = defaultPriceText.toLongOrNull() ?: 250L
                    val customMap = mutableMapOf<String, Long>()
                    ALL_SCHOOL_CLASSES.forEach { cls ->
                        val v = classPriceInputs[cls]?.toLongOrNull()
                        if (v != null && v > 0L) {
                            customMap[cls] = v
                        }
                    }
                    onSavePrices(defaultPrice, customMap)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SidebarNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_prices_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmberGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Prices", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PrintAdmissionFormDialog(
    enquiry: Enquiry,
    schoolConfig: SchoolConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptText = remember(enquiry, schoolConfig) {
        """
        ${schoolConfig.schoolName.uppercase()}
        Academic Session: ${enquiry.session} | Helpline: ${schoolConfig.helplineNumber}
        -----------------------------------------
        ADMISSION ENQUIRY & FORM RECEIPT
        Applicant Name : ${enquiry.studentName}
        Father's Name  : ${enquiry.fatherName}
        Mobile Number  : ${enquiry.mobileNumber}
        Class Applied  : ${enquiry.classInterested}
        Address        : ${enquiry.address}
        Form Status    : ISSUED
        Form Fee Paid  : ₹${enquiry.formPrice}
        Remarks        : ${enquiry.remarks}
        -----------------------------------------
        Authorized Admission Desk Signature
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admission Form Receipt") },
        text = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = receiptText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Admission Form - ${enquiry.studentName}")
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Print / Share Admission Form"))
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print / Share Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EditEnquiryDialog(
    enquiry: Enquiry,
    config: SchoolConfig,
    onDismiss: () -> Unit,
    onConfirm: (Enquiry, Boolean) -> Unit
) {
    var studentName by remember { mutableStateOf(enquiry.studentName) }
    var fatherName by remember { mutableStateOf(enquiry.fatherName) }
    var mobileNumber by remember { mutableStateOf(enquiry.mobileNumber) }
    var address by remember { mutableStateOf(enquiry.address) }
    var remarks by remember { mutableStateOf(enquiry.remarks) }
    var formIssued by remember { mutableStateOf(enquiry.formIssued) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Enquiry — ${enquiry.classInterested}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = formIssued, onCheckedChange = { formIssued = it })
                    Text("Form Issued (₹${config.resolveFormPriceForClass(enquiry.classInterested)})")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newlyIssued = !enquiry.formIssued && formIssued
                    val newStatus = if (enquiry.status == "Admitted") {
                        "Admitted"
                    } else if (formIssued) {
                        "Form Issued"
                    } else {
                        "Pending"
                    }
                    onConfirm(
                        enquiry.copy(
                            studentName = studentName,
                            fatherName = fatherName,
                            mobileNumber = mobileNumber,
                            address = address,
                            remarks = remarks,
                            formIssued = formIssued,
                            formPrice = config.resolveFormPriceForClass(enquiry.classInterested),
                            status = newStatus
                        ),
                        newlyIssued
                    )
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
