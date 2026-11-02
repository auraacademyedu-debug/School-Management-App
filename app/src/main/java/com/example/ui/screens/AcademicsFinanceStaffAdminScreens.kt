package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ALL_SCHOOL_CLASSES
import com.example.data.FeeTransaction
import com.example.data.SchoolConfig
import com.example.data.SchoolRecord
import com.example.data.StaffMember
import com.example.data.Student
import com.example.ui.DashboardMetrics
import com.example.ui.components.formatAmount
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SidebarNavy
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AcademicsModuleScreen(
    destination: NavDestination,
    students: List<Student>,
    records: List<SchoolRecord>,
    onMarkAttendance: (Student, Boolean) -> Unit,
    onUpdateStudentPercentage: (Student, Long) -> Unit,
    onAddRecord: (String, String, String, String, Long) -> Unit,
    onDeleteRecord: (SchoolRecord) -> Unit
) {
    var selectedClass by remember { mutableStateOf("Class 1") }
    var titleInput by remember { mutableStateOf("") }
    var subtitleInput by remember { mutableStateOf("Class 1") }
    var detailsInput by remember { mutableStateOf("") }
    var scoreInput by remember { mutableStateOf("100") }

    val activeStudents = students.filter { it.status == "Active" && it.className == selectedClass }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (destination) {
            NavDestination.ATTENDANCE, NavDestination.ATTENDANCE_REPORT -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(destination.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ALL_SCHOOL_CLASSES.forEach { cls ->
                                FilterChip(selected = selectedClass == cls, onClick = { selectedClass = cls }, label = { Text(cls) })
                            }
                        }
                        HorizontalDivider()
                        if (activeStudents.isEmpty()) {
                            Text("No active students enrolled in $selectedClass.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            activeStudents.forEach { stu ->
                                val pct = if (stu.attendanceTotal > 0) ((stu.attendancePresent * 100) / stu.attendanceTotal) else 0L
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${stu.studentName} (Roll #${stu.rollNumber})", fontWeight = FontWeight.Bold)
                                        Text("Present: ${stu.attendancePresent} / ${stu.attendanceTotal} days ($pct%)", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (destination == NavDestination.ATTENDANCE) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { onMarkAttendance(stu, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                            ) { Text("P") }
                                            OutlinedButton(onClick = { onMarkAttendance(stu, false) }) {
                                                Text("A", color = RoseDanger)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            NavDestination.MARKS_ENTRY, NavDestination.CLASS_RESULT -> {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(destination.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ALL_SCHOOL_CLASSES.forEach { cls ->
                                FilterChip(selected = selectedClass == cls, onClick = { selectedClass = cls }, label = { Text(cls) })
                            }
                        }
                        if (activeStudents.isEmpty()) {
                            Text("No active students enrolled in $selectedClass.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            activeStudents.sortedByDescending { it.examPercentage }.forEach { stu ->
                                var marksDraft by remember(stu.id, stu.examPercentage) { mutableStateOf(stu.examPercentage.toString()) }
                                val grade = when {
                                    stu.examPercentage >= 90 -> "A+"
                                    stu.examPercentage >= 75 -> "A"
                                    stu.examPercentage >= 60 -> "B"
                                    stu.examPercentage >= 40 -> "C"
                                    else -> "Pending"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${stu.studentName} (Roll #${stu.rollNumber})", fontWeight = FontWeight.Bold)
                                        Text("Aggregate: ${stu.examPercentage}%  •  Grade: $grade", style = MaterialTheme.typography.bodySmall, color = RoyalBlue)
                                    }
                                    if (destination == NavDestination.MARKS_ENTRY) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = marksDraft,
                                                onValueChange = { marksDraft = it.filter { ch -> ch.isDigit() }.take(3) },
                                                label = { Text("%") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.width(82.dp)
                                            )
                                            Button(onClick = {
                                                val v = (marksDraft.toLongOrNull() ?: 0L).coerceIn(0L, 100L)
                                                onUpdateStudentPercentage(stu, v)
                                            }) {
                                                Text("Save")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // EXAMS, EXAM_SCHEDULE, SUBJECTS, CLASS_SUBJECT_MAP
                val recordType = if (destination == NavDestination.EXAMS || destination == NavDestination.EXAM_SCHEDULE) "Exam" else "Subject"
                val filteredRecords = records.filter { it.recordType == recordType }

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = RoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage ${destination.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text(if (recordType == "Exam") "Exam Title (e.g. Half Yearly Exam)" else "Subject Name (e.g. Mathematics)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = subtitleInput,
                            onValueChange = { subtitleInput = it },
                            label = { Text("Mapped Class (e.g. Class 1 - Class 5)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = detailsInput,
                            onValueChange = { detailsInput = it },
                            label = { Text(if (recordType == "Exam") "Exam Schedule Date & Shift" else "Subject Code / Assigned Teacher") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = scoreInput,
                            onValueChange = { scoreInput = it.filter { ch -> ch.isDigit() }.take(4) },
                            label = { Text("Max Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank()) {
                                    onAddRecord(recordType, titleInput, subtitleInput, detailsInput.ifBlank { "Standard" }, scoreInput.toLongOrNull() ?: 100L)
                                    titleInput = ""
                                    detailsInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add $recordType Entry")
                        }
                    }
                }

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Configured $recordType List", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (filteredRecords.isEmpty()) {
                            Text("No $recordType items configured yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            filteredRecords.forEach { rec ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${rec.title} • ${rec.subtitle}", fontWeight = FontWeight.SemiBold)
                                        Text("${rec.details} | Max Marks: ${rec.amountOrScore}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { onDeleteRecord(rec) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RoseDanger)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceModuleScreen(
    destination: NavDestination,
    metrics: DashboardMetrics,
    transactions: List<FeeTransaction>,
    records: List<SchoolRecord>,
    onAddExpense: (String, String, String, Long) -> Unit,
    onDeleteExpense: (SchoolRecord) -> Unit
) {
    var expenseTitle by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("Campus Maintenance") }
    var expenseNote by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }

    val expenses = records.filter { it.recordType == "Expense" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (destination == NavDestination.EXPENSES) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = RoseDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record School Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Expense Head (e.g. Electricity Bill, Lab Supplies)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                    )
                    OutlinedTextField(
                        value = expenseCategory,
                        onValueChange = { expenseCategory = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it.filter { ch -> ch.isDigit() }.take(7) },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                    )
                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { expenseNote = it },
                        label = { Text("Voucher / Remarks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val amt = expenseAmount.toLongOrNull() ?: 0L
                            if (expenseTitle.isNotBlank() && amt > 0L) {
                                onAddExpense(expenseTitle, expenseCategory, expenseNote.ifBlank { "Approved Voucher" }, amt)
                                expenseTitle = ""
                                expenseAmount = ""
                                expenseNote = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                        modifier = Modifier.fillMaxWidth().testTag("save_expense_btn")
                    ) {
                        Text("Save Expense Voucher")
                    }
                }
            }
        }

        // Income vs Expense & Summary Analytics Card
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Income vs Expense Financial Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val totalIncome = metrics.totalFeeCollected
                val totalExpense = metrics.totalExpenses
                val netSurplus = totalIncome - totalExpense
                val totalPool = (totalIncome + totalExpense).coerceAtLeast(1L)
                val incomeRatio = totalIncome.toFloat() / totalPool.toFloat()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Income", style = MaterialTheme.typography.labelMedium)
                        Text("₹${formatAmount(totalIncome)}", style = MaterialTheme.typography.titleLarge, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Expenses", style = MaterialTheme.typography.labelMedium)
                        Text("₹${formatAmount(totalExpense)}", style = MaterialTheme.typography.titleLarge, color = RoseDanger, fontWeight = FontWeight.Bold)
                    }
                }
                LinearProgressIndicator(
                    progress = { incomeRatio },
                    color = EmeraldSuccess,
                    trackColor = RoseContainer,
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
                )
                Text(
                    text = "Net Institutional Balance: ₹${formatAmount(netSurplus)}  (Active Receipts: ${transactions.count { !it.isDeleted }})",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (netSurplus >= 0) EmeraldSuccess else RoseDanger
                )
            }
        }

        // Expense Ledger
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Expense Vouchers Ledger", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (expenses.isEmpty()) {
                    Text("No expenses recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    expenses.forEach { exp ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.title, fontWeight = FontWeight.SemiBold)
                                Text("${exp.subtitle} • ${exp.details}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹${formatAmount(exp.amountOrScore)}", fontWeight = FontWeight.Bold, color = RoseDanger)
                                IconButton(onClick = { onDeleteExpense(exp) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Expense", tint = RoseDanger)
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun StaffAndPayrollScreen(
    destination: NavDestination,
    staffList: List<StaffMember>,
    onAddStaff: (String, String, String, String, Long) -> Unit,
    onPaySalary: (StaffMember, String) -> Unit,
    onDeleteStaff: (StaffMember) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("PGT Teacher") }
    var dept by remember { mutableStateOf("Academics") }
    var mobile by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("28000") }
    var payMonth by remember { mutableStateOf("Nov 2026") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (destination == NavDestination.STAFF_MANAGEMENT) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Staff Member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Designation") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it.filter { ch -> ch.isDigit() || ch == '+' }.take(15) },
                            label = { Text("Mobile *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { salary = it.filter { ch -> ch.isDigit() }.take(7) },
                            label = { Text("Monthly Salary (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && mobile.length >= 6) {
                                onAddStaff(name, role, dept, mobile, salary.toLongOrNull() ?: 20000L)
                                name = ""
                                mobile = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_btn")
                    ) {
                        Text("Save Staff Member")
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${destination.title} Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Monthly Bill: ₹${formatAmount(staffList.sumOf { it.monthlySalary })}",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalBlue
                    )
                }

                if (destination == NavDestination.SALARY_MANAGEMENT) {
                    OutlinedTextField(
                        value = payMonth,
                        onValueChange = { payMonth = it },
                        label = { Text("Disbursement Month") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (staffList.isEmpty()) {
                    Text("No staff members added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    staffList.forEach { staff ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${staff.name} • ${staff.role}", fontWeight = FontWeight.Bold)
                                Text("${staff.department} | Salary: ₹${formatAmount(staff.monthlySalary)}/mo | Last Paid: ${staff.lastPaidMonth}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onPaySalary(staff, payMonth) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                ) {
                                    Text("Pay Salary")
                                }
                                IconButton(onClick = { onDeleteStaff(staff) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Staff", tint = RoseDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunicationNoticesScreen(
    notices: List<SchoolRecord>,
    onPublishNotice: (String, String, String) -> Unit,
    onDeleteNotice: (SchoolRecord) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("All Parents & Staff") }
    var body by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Campus Notice / Circular", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Notice Headline *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = audience, onValueChange = { audience = it }, label = { Text("Target Audience") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Announcement Message *") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            onPublishNotice(title, audience, body)
                            title = ""
                            body = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SidebarNavy),
                    modifier = Modifier.fillMaxWidth().testTag("publish_notice_btn")
                ) {
                    Text("Publish Notice", color = Color.White)
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Notice Board Archive", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (notices.isEmpty()) {
                    Text("No notices published yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    notices.forEach { n ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(n.title, fontWeight = FontWeight.Bold)
                                    Text("Audience: ${n.subtitle}", style = MaterialTheme.typography.labelSmall, color = RoyalBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(n.details, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onDeleteNotice(n) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Notice", tint = RoseDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAndAccountScreen(
    destination: NavDestination,
    config: SchoolConfig,
    darkThemeEnabled: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSaveSettings: (String, String, String, String, String, String, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var schoolName by remember(config.schoolName) { mutableStateOf(config.schoolName) }
    var helpline by remember(config.helplineNumber) { mutableStateOf(config.helplineNumber) }
    var website by remember(config.websiteUrl) { mutableStateOf(config.websiteUrl) }
    var ytMain by remember(config.youtubeMainUrl) { mutableStateOf(config.youtubeMainUrl) }
    var ytJr by remember(config.youtubeJuniorUrl) { mutableStateOf(config.youtubeJuniorUrl) }
    var fb by remember(config.facebookUrl) { mutableStateOf(config.facebookUrl) }
    var ig by remember(config.instagramUrl) { mutableStateOf(config.instagramUrl) }

    val user = Firebase.auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (destination == NavDestination.PASSWORD_CHANGE) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Account Security & Google Identity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Signed-in Administrator: ${user?.email ?: "Google Account"}")
                    Text(
                        text = "Your EduPulse administrator session is federated via Google Sign-In (Credential Manager) with hardware-backed passkey & OAuth protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            val email = user?.email
                            if (!email.isNullOrBlank()) {
                                Firebase.auth.sendPasswordResetEmail(email)
                                onShowMessage("Security / recovery link dispatched to $email")
                            } else {
                                onShowMessage("Managed directly via your Google Account settings.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Send Account Recovery Verification Email")
                    }
                }
            }
        } else {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("School Profile, Helpline & Social Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme Workspace", fontWeight = FontWeight.Medium)
                        Switch(checked = darkThemeEnabled, onCheckedChange = onToggleDarkTheme)
                    }

                    HorizontalDivider()

                    OutlinedTextField(value = schoolName, onValueChange = { schoolName = it }, label = { Text("School Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = helpline, onValueChange = { helpline = it }, label = { Text("Footer Helpline Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Official Website URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ytMain, onValueChange = { ytMain = it }, label = { Text("YouTube Channel 1 (Main)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ytJr, onValueChange = { ytJr = it }, label = { Text("YouTube Channel 2 (Kids/Junior)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = fb, onValueChange = { fb = it }, label = { Text("Facebook Page URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ig, onValueChange = { ig = it }, label = { Text("Instagram Handle URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = { onSaveSettings(schoolName, helpline, website, ytMain, ytJr, fb, ig) },
                        colors = ButtonDefaults.buttonColors(containerColor = SidebarNavy),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_admin_settings_btn")
                    ) {
                        Text("Save School Profile & Footer Links", color = Color.White)
                    }
                }
            }
        }
    }
}
