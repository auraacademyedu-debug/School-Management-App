package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.ui.graphics.vector.ImageVector

enum class SidebarCategory(val label: String) {
    MAIN("Main"),
    STUDENTS("Students"),
    FEES("Fees"),
    ACADEMICS("Academics"),
    FINANCE("Finance"),
    STAFF_PAYROLL("Staff / Payroll"),
    COMMUNICATION("Communication"),
    ADMIN_ACCOUNT("Admin & Account")
}

enum class NavDestination(
    val route: String,
    val title: String,
    val category: SidebarCategory,
    val icon: ImageVector
) {
    // Main
    DASHBOARD("dashboard", "Dashboard", SidebarCategory.MAIN, Icons.Default.Dashboard),

    // Students
    ADMISSION_ENQUIRY("admission_enquiry", "Admission Enquiry", SidebarCategory.STUDENTS, Icons.Default.SupportAgent),
    NEW_ADMISSION("new_admission", "New Admission", SidebarCategory.STUDENTS, Icons.Default.PersonAdd),
    ALL_STUDENTS("all_students", "All Students", SidebarCategory.STUDENTS, Icons.Default.Groups),
    PASSED_OUT_ARCHIVE("passed_out_archive", "Passed Out Archive", SidebarCategory.STUDENTS, Icons.Default.Archive),
    PROMOTION("promotion", "Promotion", SidebarCategory.STUDENTS, Icons.Default.Upgrade),
    SESSION_REPORT("session_report", "Session Report", SidebarCategory.STUDENTS, Icons.Default.Analytics),

    // Fees
    FEE_STRUCTURE("fee_structure", "Fee Structure", SidebarCategory.FEES, Icons.Default.PriceChange),
    FEE_COLLECTION("fee_collection", "Fee Collection", SidebarCategory.FEES, Icons.Default.CurrencyRupee),
    QUICK_FEE_COLLECT("quick_fee_collect", "Quick Fee Collect", SidebarCategory.FEES, Icons.Default.FlashOn),
    WHATSAPP_REMINDER("whatsapp_reminder", "WhatsApp Reminder", SidebarCategory.FEES, Icons.AutoMirrored.Filled.Chat),
    FEE_DELETION_LOG("fee_deletion_log", "Fee Deletion Log", SidebarCategory.FEES, Icons.Default.DeleteSweep),

    // Academics
    ATTENDANCE("attendance", "Attendance", SidebarCategory.ACADEMICS, Icons.Default.HowToReg),
    ATTENDANCE_REPORT("attendance_report", "Attendance Report", SidebarCategory.ACADEMICS, Icons.AutoMirrored.Filled.FactCheck),
    EXAMS("exams", "Exams", SidebarCategory.ACADEMICS, Icons.Default.Quiz),
    MARKS_ENTRY("marks_entry", "Marks Entry", SidebarCategory.ACADEMICS, Icons.Default.EditNote),
    CLASS_RESULT("class_result", "Class Result", SidebarCategory.ACADEMICS, Icons.Default.EmojiEvents),
    SUBJECTS("subjects", "Subjects", SidebarCategory.ACADEMICS, Icons.AutoMirrored.Filled.MenuBook),
    CLASS_SUBJECT_MAP("class_subject_map", "Class-Subject Map", SidebarCategory.ACADEMICS, Icons.Default.Link),
    EXAM_SCHEDULE("exam_schedule", "Exam Schedule", SidebarCategory.ACADEMICS, Icons.Default.CalendarMonth),

    // Finance
    EXPENSES("expenses", "Expenses", SidebarCategory.FINANCE, Icons.Default.AccountBalanceWallet),
    ALL_REPORTS("all_reports", "All Reports", SidebarCategory.FINANCE, Icons.Default.PieChart),
    INCOME_VS_EXPENSE("income_vs_expense", "Income vs Expense", SidebarCategory.FINANCE, Icons.AutoMirrored.Filled.TrendingUp),

    // Staff / Payroll
    STAFF_MANAGEMENT("staff_management", "Staff Management", SidebarCategory.STAFF_PAYROLL, Icons.Default.Badge),
    SALARY_MANAGEMENT("salary_management", "Salary Management", SidebarCategory.STAFF_PAYROLL, Icons.Default.Payments),
    SALARY_REPORT("salary_report", "Salary Report", SidebarCategory.STAFF_PAYROLL, Icons.AutoMirrored.Filled.ReceiptLong),

    // Communication
    NOTICES("notices", "Notices", SidebarCategory.COMMUNICATION, Icons.Default.Campaign),

    // Admin & Account
    ADMIN_SETTINGS("admin_settings", "Admin Settings", SidebarCategory.ADMIN_ACCOUNT, Icons.Default.AdminPanelSettings),
    SETTINGS("settings", "Settings", SidebarCategory.ADMIN_ACCOUNT, Icons.Default.Settings),
    PASSWORD_CHANGE("password_change", "Password Change", SidebarCategory.ADMIN_ACCOUNT, Icons.Default.LockReset),
    LOGOUT("logout", "Logout", SidebarCategory.ADMIN_ACCOUNT, Icons.AutoMirrored.Filled.Logout);

    companion object {
        val groupedByCategory: Map<SidebarCategory, List<NavDestination>> =
            entries.groupBy { it.category }
    }
}
