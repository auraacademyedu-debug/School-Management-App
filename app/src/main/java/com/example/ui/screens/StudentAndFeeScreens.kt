package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ACADEMIC_SESSIONS
import com.example.data.ALL_SCHOOL_CLASSES
import com.example.data.Enquiry
import com.example.data.FeeTransaction
import com.example.data.SchoolConfig
import com.example.data.Student
import com.example.ui.components.formatAmount
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SidebarNavy

@Composable
fun NewAdmissionScreen(
    config: SchoolConfig,
    prefilledEnquiry: Enquiry?,
    onAdmitStudent: (
        studentName: String,
        fatherName: String,
        mobile: String,
        className: String,
        section: String,
        rollNumber: String,
        address: String,
        annualFeeTotal: Long,
        initialPaid: Long,
        paymentMode: String,
        linkedEnquiry: Enquiry?
    ) -> Unit
) {
    var studentName by remember(prefilledEnquiry) { mutableStateOf(prefilledEnquiry?.studentName ?: "") }
    var fatherName by remember(prefilledEnquiry) { mutableStateOf(prefilledEnquiry?.fatherName ?: "") }
    var mobileNumber by remember(prefilledEnquiry) { mutableStateOf(prefilledEnquiry?.mobileNumber ?: "") }
    var className by remember(prefilledEnquiry) { mutableStateOf(prefilledEnquiry?.classInterested ?: "Class 1") }
    var section by remember { mutableStateOf("A") }
    var rollNumber by remember { mutableStateOf("1") }
    var address by remember(prefilledEnquiry) { mutableStateOf(prefilledEnquiry?.address ?: "") }
    var annualFeeText by remember(className, config) {
        mutableStateOf(config.resolveAnnualTuitionForClass(className).toString())
    }
    var initialPaidText by remember { mutableStateOf("5000") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var classMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (prefilledEnquiry != null) "Convert Enquiry to New Admission" else "New Student Admission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enroll student into active session & record admission fee",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it; errorMsg = null },
                    label = { Text("Student Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adm_student_name_input")
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it; errorMsg = null },
                    label = { Text("Father / Guardian Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adm_father_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it.filter { ch -> ch.isDigit() || ch == '+' }.take(15) },
                        label = { Text("Mobile Number *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("adm_mobile_input")
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = className,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class") },
                            trailingIcon = {
                                IconButton(onClick = { classMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = classMenuExpanded, onDismissRequest = { classMenuExpanded = false }) {
                            ALL_SCHOOL_CLASSES.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        className = cls
                                        annualFeeText = config.resolveAnnualTuitionForClass(cls).toString()
                                        classMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it.take(5) },
                        label = { Text("Section") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Roll Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = annualFeeText,
                        onValueChange = { annualFeeText = it.filter { ch -> ch.isDigit() }.take(7) },
                        label = { Text("Total Annual Fee (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = initialPaidText,
                        onValueChange = { initialPaidText = it.filter { ch -> ch.isDigit() }.take(7) },
                        label = { Text("Admission Fee Paid Now (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf("Cash", "UPI", "Bank Transfer", "Card").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        if (studentName.isBlank() || fatherName.isBlank() || mobileNumber.length < 6) {
                            errorMsg = "Please fill Student Name, Father's Name, and Mobile Number."
                        } else {
                            onAdmitStudent(
                                studentName,
                                fatherName,
                                mobileNumber,
                                className,
                                section,
                                rollNumber,
                                address.ifBlank { "Local" },
                                annualFeeText.toLongOrNull() ?: 24000L,
                                initialPaidText.toLongOrNull() ?: 0L,
                                paymentMode,
                                prefilledEnquiry
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_admission_button")
                ) {
                    Text("Complete Admission & Generate Roll No", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StudentDirectoryScreen(
    title: String,
    students: List<Student>,
    archiveMode: Boolean,
    onCollectQuickFee: (Student) -> Unit,
    onArchiveToggle: (Student) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var classFilter by remember { mutableStateOf("All") }

    val list = remember(students, search, classFilter, archiveMode) {
        students.filter { stu ->
            val statusMatch = if (archiveMode) stu.status != "Active" else stu.status == "Active"
            val clsMatch = classFilter == "All" || stu.className == classFilter
            val qMatch = search.isBlank() ||
                stu.studentName.contains(search, true) ||
                stu.admissionNo.contains(search, true) ||
                stu.mobileNumber.contains(search, true)
            statusMatch && clsMatch && qMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search by student name, admission no, or mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_directory_search")
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (listOf("All") + ALL_SCHOOL_CLASSES).forEach { cls ->
                        FilterChip(
                            selected = classFilter == cls,
                            onClick = { classFilter = cls },
                            label = { Text(cls) }
                        )
                    }
                }
            }
        }

        if (list.isEmpty()) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (archiveMode) "No passed-out or archived students yet." else "No active students enrolled for this filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            list.forEach { stu ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${stu.studentName} (${stu.admissionNo})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${stu.className} - Sec ${stu.section} | Roll #${stu.rollNumber} | Father: ${stu.fatherName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Mob: ${stu.mobileNumber} | Session: ${stu.session}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = if (stu.feeBalance == 0L) EmeraldContainer else RoseContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (stu.feeBalance == 0L) "Fee Cleared" else "Due ₹${formatAmount(stu.feeBalance)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (stu.feeBalance == 0L) EmeraldSuccess else RoseDanger,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!archiveMode && stu.feeBalance > 0L) {
                                Button(
                                    onClick = { onCollectQuickFee(stu) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                                ) {
                                    Text("Collect Fee", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            OutlinedButton(
                                onClick = { onArchiveToggle(stu) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (archiveMode) "Restore Active" else "Mark Passed Out",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionAndSessionReportScreen(
    isPromotionMode: Boolean,
    students: List<Student>,
    enquiries: List<Enquiry>,
    selectedSession: String,
    onPromoteBatch: (List<Student>, String, String) -> Unit
) {
    var fromClass by remember { mutableStateOf("Class 1") }
    var toClass by remember { mutableStateOf("Class 2") }
    var targetSession by remember { mutableStateOf("2028-29") }

    val matchingStudents = students.filter { it.status == "Active" && it.className == fromClass }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isPromotionMode) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Batch Student Promotion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Select current class and promote all active students to next academic class or Passed Out archive.", style = MaterialTheme.typography.bodySmall)

                    Text("From Class:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ALL_SCHOOL_CLASSES.forEach { cls ->
                            FilterChip(selected = fromClass == cls, onClick = { fromClass = cls }, label = { Text(cls) })
                        }
                    }

                    Text("Promote To:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (ALL_SCHOOL_CLASSES + "Passed Out").forEach { cls ->
                            FilterChip(selected = toClass == cls, onClick = { toClass = cls }, label = { Text(cls) })
                        }
                    }

                    Text("Target Academic Session:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ACADEMIC_SESSIONS.forEach { ses ->
                            FilterChip(selected = targetSession == ses, onClick = { targetSession = ses }, label = { Text(ses) })
                        }
                    }

                    Button(
                        onClick = { onPromoteBatch(matchingStudents, toClass, targetSession) },
                        enabled = matchingStudents.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth().testTag("promote_batch_button")
                    ) {
                        Text("Promote ${matchingStudents.size} Student(s) from $fromClass to $toClass")
                    }
                }
            }
        } else {
            // Session Report Breakdown
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Class-wise Session Report ($selectedSession)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    ALL_SCHOOL_CLASSES.forEach { cls ->
                        val clsEnq = enquiries.count { it.session == selectedSession && it.classInterested == cls }
                        val clsStu = students.filter { it.session == selectedSession && it.className == cls && it.status == "Active" }
                        val clsPaid = clsStu.sumOf { it.annualFeePaid }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cls, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("Enquiries: $clsEnq", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("Enrolled: ${clsStu.size}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("Paid: ₹${formatAmount(clsPaid)}", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun FeeManagementModuleScreen(
    mode: String, // structure | collection | quick | whatsapp | deletion_log
    config: SchoolConfig,
    students: List<Student>,
    transactions: List<FeeTransaction>,
    onSaveTuitionStructure: (Map<String, Long>) -> Unit,
    onCollectFee: (Student, String, Long, String) -> Unit,
    onDeleteTransaction: (FeeTransaction, String) -> Unit
) {
    val context = LocalContext.current
    var selectedStudent by remember(students) { mutableStateOf(students.firstOrNull { it.status == "Active" }) }
    var feeType by remember { mutableStateOf("Tuition Fee") }
    var amountText by remember { mutableStateOf("2500") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var deleteDialogTx by remember { mutableStateOf<FeeTransaction?>(null) }
    var deletionReason by remember { mutableStateOf("") }

    val tuitionInputs = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(config.classTuitionFees) {
        ALL_SCHOOL_CLASSES.forEach { cls ->
            val monthly = config.classTuitionFees[cls] ?: (config.resolveAnnualTuitionForClass(cls) / 12L)
            tuitionInputs[cls] = monthly.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (mode) {
            "structure" -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Class-wise Monthly Tuition Fee Structure", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ALL_SCHOOL_CLASSES.chunked(3).forEach { rowCls ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                rowCls.forEach { cls ->
                                    OutlinedTextField(
                                        value = tuitionInputs[cls] ?: "",
                                        onValueChange = { tuitionInputs[cls] = it.filter { ch -> ch.isDigit() }.take(6) },
                                        label = { Text("$cls / mo") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                val map = ALL_SCHOOL_CLASSES.associateWith { cls -> tuitionInputs[cls]?.toLongOrNull() ?: 2000L }
                                onSaveTuitionStructure(map)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SidebarNavy),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_tuition_structure_btn")
                        ) {
                            Text("Save Tuition Fee Structure", color = Color.White)
                        }
                    }
                }
            }

            "collection", "quick" -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mode == "quick") Icons.Default.FlashOn else Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = AmberGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (mode == "quick") "Quick Fee Collect Desk" else "Student Fee Collection & Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val activeStudents = students.filter { it.status == "Active" }
                        if (activeStudents.isEmpty()) {
                            Text("Admit a student first to collect tuition or exam fees.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Select Student:", style = MaterialTheme.typography.labelMedium)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeStudents.forEach { stu ->
                                    FilterChip(
                                        selected = selectedStudent?.id == stu.id,
                                        onClick = { selectedStudent = stu },
                                        label = { Text("${stu.studentName} (${stu.className})") }
                                    )
                                }
                            }

                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Tuition Fee", "Admission Fee", "Exam Fee", "Transport Fee").forEach { ft ->
                                    FilterChip(selected = feeType == ft, onClick = { feeType = ft }, label = { Text(ft) })
                                }
                            }

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it.filter { ch -> ch.isDigit() }.take(7) },
                                label = { Text("Collection Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("fee_collect_amount_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Cash", "UPI", "Bank Transfer", "Card").forEach { pm ->
                                    FilterChip(selected = paymentMode == pm, onClick = { paymentMode = pm }, label = { Text(pm) })
                                }
                            }

                            Button(
                                onClick = {
                                    val stu = selectedStudent ?: return@Button
                                    val amt = amountText.toLongOrNull() ?: 0L
                                    if (amt > 0L) onCollectFee(stu, feeType, amt, paymentMode)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("collect_fee_submit_btn")
                            ) {
                                Text("Record Fee Receipt")
                            }
                        }
                    }
                }

                // Recent Active Fee Receipts
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Recent Fee Receipts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val activeReceipts = transactions.filter { !it.isDeleted }
                        if (activeReceipts.isEmpty()) {
                            Text("No fee receipts recorded yet.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            activeReceipts.forEach { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${tx.studentName} • ${tx.feeType}", fontWeight = FontWeight.Bold)
                                        Text("${tx.className} | Mode: ${tx.paymentMode} | Session: ${tx.session}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("₹${formatAmount(tx.amount)}", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { deleteDialogTx = tx }) {
                                            Icon(Icons.Default.DeleteSweep, contentDescription = "Cancel Receipt", tint = RoseDanger)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "whatsapp" -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp Fee Reminder Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        val defaulters = students.filter { it.status == "Active" && it.feeBalance > 0L }
                        if (defaulters.isEmpty()) {
                            Text("All enrolled students have cleared their dues!", color = EmeraldSuccess)
                        } else {
                            defaulters.forEach { stu ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${stu.studentName} (${stu.className})", fontWeight = FontWeight.Bold)
                                        Text("Pending Balance: ₹${formatAmount(stu.feeBalance)} • Mob: ${stu.mobileNumber}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Button(
                                        onClick = {
                                            val phone = stu.mobileNumber.filter { it.isDigit() }
                                            val msg = "Dear ${stu.fatherName}, gentle reminder from ${config.schoolName}: Fee balance of ₹${stu.feeBalance} is due for ${stu.studentName} (${stu.className})."
                                            val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(msg)}")
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            } catch (_: Exception) {
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                    ) {
                                        Text("Send WhatsApp")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "deletion_log" -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Fee Deletion Audit Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val deletedList = transactions.filter { it.isDeleted }
                        if (deletedList.isEmpty()) {
                            Text("No cancelled or deleted fee receipts in audit history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            deletedList.forEach { tx ->
                                Surface(
                                    color = RoseContainer.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("${tx.studentName} — ₹${formatAmount(tx.amount)} (${tx.feeType})", fontWeight = FontWeight.Bold, color = RoseDanger)
                                        Text("Reason: ${tx.deletionReason}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteDialogTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { deleteDialogTx = null },
            title = { Text("Cancel Receipt — ₹${tx.amount}") },
            text = {
                OutlinedTextField(
                    value = deletionReason,
                    onValueChange = { deletionReason = it },
                    label = { Text("Audit Reason for Cancellation") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx, deletionReason.ifBlank { "Duplicate entry" })
                        deletionReason = ""
                        deleteDialogTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogTx = null }) { Text("Back") }
            }
        )
    }
}
