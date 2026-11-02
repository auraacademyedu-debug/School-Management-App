package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.SchoolConfig
import com.example.data.SchoolRepository
import com.example.ui.auth.signOutUser
import com.example.ui.components.SchoolFooterContactBar
import com.example.ui.components.SchoolSidebarContent
import com.example.ui.components.TopHeaderAndSessionBar
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.AcademicsModuleScreen
import com.example.ui.screens.AdminAndAccountScreen
import com.example.ui.screens.CommunicationNoticesScreen
import com.example.ui.screens.DashboardAndEnquiryWorkspace
import com.example.ui.screens.FeeManagementModuleScreen
import com.example.ui.screens.FinanceModuleScreen
import com.example.ui.screens.NewAdmissionScreen
import com.example.ui.screens.PromotionAndSessionReportScreen
import com.example.ui.screens.StaffAndPayrollScreen
import com.example.ui.screens.StudentDirectoryScreen
import com.example.ui.theme.EduPulseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun MainSchoolWorkspace(
    currentUserId: String,
    userEmail: String,
    onSignedOut: () -> Unit
) {
    val schoolViewModel: SchoolViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SchoolViewModel(SchoolRepository(db), currentUserId)
            }
        }
    )

    val darkThemeEnabled by schoolViewModel.darkThemeEnabled.collectAsStateWithLifecycle()
    val currentDest by schoolViewModel.currentDestination.collectAsStateWithLifecycle()
    val selectedSession by schoolViewModel.selectedSession.collectAsStateWithLifecycle()
    val snackbarMessage by schoolViewModel.snackbarMessage.collectAsStateWithLifecycle()
    val prefilledEnquiry by schoolViewModel.prefilledAdmissionEnquiry.collectAsStateWithLifecycle()

    val configUi by schoolViewModel.configState.collectAsStateWithLifecycle()
    val enquiriesUi by schoolViewModel.enquiriesState.collectAsStateWithLifecycle()
    val studentsUi by schoolViewModel.studentsState.collectAsStateWithLifecycle()
    val transactionsUi by schoolViewModel.transactionsState.collectAsStateWithLifecycle()
    val staffUi by schoolViewModel.staffState.collectAsStateWithLifecycle()
    val recordsUi by schoolViewModel.recordsState.collectAsStateWithLifecycle()
    val metrics by schoolViewModel.dashboardMetrics.collectAsStateWithLifecycle()

    val config = (configUi as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
    val enquiries = (enquiriesUi as? UiState.Success)?.data ?: emptyList()
    val students = (studentsUi as? UiState.Success)?.data ?: emptyList()
    val transactions = (transactionsUi as? UiState.Success)?.data ?: emptyList()
    val staffList = (staffUi as? UiState.Success)?.data ?: emptyList()
    val records = (recordsUi as? UiState.Success)?.data ?: emptyList()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            schoolViewModel.clearSnackbar()
        }
    }

    // BackHandler for non-dashboard destinations
    if (currentDest != NavDestination.DASHBOARD) {
        BackHandler {
            schoolViewModel.navigateTo(NavDestination.DASHBOARD)
        }
    }

    EduPulseTheme(darkTheme = darkThemeEnabled) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 840.dp
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            val mainPaneContent: @Composable (Boolean, () -> Unit) -> Unit = { showHamburger, onOpenDrawer ->
                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopHeaderAndSessionBar(
                            title = currentDest.title,
                            categoryLabel = currentDest.category.label,
                            selectedSession = selectedSession,
                            onSessionSelected = { schoolViewModel.selectSession(it) },
                            showMenuButton = showHamburger,
                            onMenuClick = onOpenDrawer
                        )
                    },
                    bottomBar = {
                        SchoolFooterContactBar(config = config)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        when (currentDest) {
                            NavDestination.DASHBOARD,
                            NavDestination.ADMISSION_ENQUIRY -> {
                                DashboardAndEnquiryWorkspace(
                                    isDashboardOverview = currentDest == NavDestination.DASHBOARD,
                                    isWideScreen = isWideScreen,
                                    selectedSession = selectedSession,
                                    metrics = metrics,
                                    config = config,
                                    enquiries = enquiries,
                                    onNavigate = { schoolViewModel.navigateTo(it) },
                                    onSaveEnquiry = { name, father, mob, cls, addr, rem, issueNow ->
                                        schoolViewModel.saveEnquiry(name, father, mob, cls, addr, rem, issueNow)
                                    },
                                    onUpdateEnquiry = { enq, newlyIssued ->
                                        schoolViewModel.updateExistingEnquiry(enq, newlyIssued)
                                    },
                                    onDeleteEnquiry = { schoolViewModel.deleteEnquiry(it) },
                                    onTakeAdmission = { schoolViewModel.startTakeAdmissionFromEnquiry(it) },
                                    onSavePrices = { defPrice, customMap ->
                                        schoolViewModel.saveFormPricing(defPrice, customMap)
                                    }
                                )
                            }

                            NavDestination.NEW_ADMISSION -> {
                                NewAdmissionScreen(
                                    config = config,
                                    prefilledEnquiry = prefilledEnquiry,
                                    onAdmitStudent = { name, father, mob, cls, sec, roll, addr, totalFee, paidNow, mode, linked ->
                                        schoolViewModel.admitStudent(
                                            studentName = name,
                                            fatherName = father,
                                            mobileNumber = mob,
                                            className = cls,
                                            section = sec,
                                            rollNumber = roll,
                                            address = addr,
                                            annualFeeTotal = totalFee,
                                            initialPaid = paidNow,
                                            paymentMode = mode,
                                            linkedEnquiry = linked
                                        )
                                    }
                                )
                            }

                            NavDestination.ALL_STUDENTS -> {
                                StudentDirectoryScreen(
                                    title = "All Active Students ($selectedSession)",
                                    students = students,
                                    archiveMode = false,
                                    onCollectQuickFee = { schoolViewModel.navigateTo(NavDestination.QUICK_FEE_COLLECT) },
                                    onArchiveToggle = { stu ->
                                        schoolViewModel.updateStudent(stu.copy(status = "Passed Out"), "Moved ${stu.studentName} to Passed Out Archive")
                                    }
                                )
                            }

                            NavDestination.PASSED_OUT_ARCHIVE -> {
                                StudentDirectoryScreen(
                                    title = "Passed Out & Transferred Archive",
                                    students = students,
                                    archiveMode = true,
                                    onCollectQuickFee = {},
                                    onArchiveToggle = { stu ->
                                        schoolViewModel.updateStudent(stu.copy(status = "Active"), "Restored ${stu.studentName} to Active Students")
                                    }
                                )
                            }

                            NavDestination.PROMOTION -> {
                                PromotionAndSessionReportScreen(
                                    isPromotionMode = true,
                                    students = students,
                                    enquiries = enquiries,
                                    selectedSession = selectedSession,
                                    onPromoteBatch = { batch, targetCls, targetSes ->
                                        schoolViewModel.promoteClassStudents(batch, targetCls, targetSes)
                                    }
                                )
                            }

                            NavDestination.SESSION_REPORT -> {
                                PromotionAndSessionReportScreen(
                                    isPromotionMode = false,
                                    students = students,
                                    enquiries = enquiries,
                                    selectedSession = selectedSession,
                                    onPromoteBatch = { _, _, _ -> }
                                )
                            }

                            NavDestination.FEE_STRUCTURE -> {
                                FeeManagementModuleScreen(
                                    mode = "structure",
                                    config = config,
                                    students = students,
                                    transactions = transactions,
                                    onSaveTuitionStructure = { schoolViewModel.saveTuitionStructure(it) },
                                    onCollectFee = { _, _, _, _ -> },
                                    onDeleteTransaction = { _, _ -> }
                                )
                            }

                            NavDestination.FEE_COLLECTION -> {
                                FeeManagementModuleScreen(
                                    mode = "collection",
                                    config = config,
                                    students = students,
                                    transactions = transactions,
                                    onSaveTuitionStructure = {},
                                    onCollectFee = { stu, ft, amt, pm -> schoolViewModel.collectFee(stu, ft, amt, pm) },
                                    onDeleteTransaction = { tx, reason -> schoolViewModel.cancelFeeTransaction(tx, reason) }
                                )
                            }

                            NavDestination.QUICK_FEE_COLLECT -> {
                                FeeManagementModuleScreen(
                                    mode = "quick",
                                    config = config,
                                    students = students,
                                    transactions = transactions,
                                    onSaveTuitionStructure = {},
                                    onCollectFee = { stu, ft, amt, pm -> schoolViewModel.collectFee(stu, ft, amt, pm) },
                                    onDeleteTransaction = { tx, reason -> schoolViewModel.cancelFeeTransaction(tx, reason) }
                                )
                            }

                            NavDestination.WHATSAPP_REMINDER -> {
                                FeeManagementModuleScreen(
                                    mode = "whatsapp",
                                    config = config,
                                    students = students,
                                    transactions = transactions,
                                    onSaveTuitionStructure = {},
                                    onCollectFee = { _, _, _, _ -> },
                                    onDeleteTransaction = { _, _ -> }
                                )
                            }

                            NavDestination.FEE_DELETION_LOG -> {
                                FeeManagementModuleScreen(
                                    mode = "deletion_log",
                                    config = config,
                                    students = students,
                                    transactions = transactions,
                                    onSaveTuitionStructure = {},
                                    onCollectFee = { _, _, _, _ -> },
                                    onDeleteTransaction = { _, _ -> }
                                )
                            }

                            NavDestination.ATTENDANCE,
                            NavDestination.ATTENDANCE_REPORT,
                            NavDestination.EXAMS,
                            NavDestination.MARKS_ENTRY,
                            NavDestination.CLASS_RESULT,
                            NavDestination.SUBJECTS,
                            NavDestination.CLASS_SUBJECT_MAP,
                            NavDestination.EXAM_SCHEDULE -> {
                                AcademicsModuleScreen(
                                    destination = currentDest,
                                    students = students,
                                    records = records,
                                    onMarkAttendance = { stu, present -> schoolViewModel.markStudentAttendance(stu, present) },
                                    onUpdateStudentPercentage = { stu, pct ->
                                        schoolViewModel.updateStudent(stu.copy(examPercentage = pct), "Saved ${pct}% for ${stu.studentName}")
                                    },
                                    onAddRecord = { type, title, sub, det, score ->
                                        schoolViewModel.addRecord(type, title, sub, det, score)
                                    },
                                    onDeleteRecord = { schoolViewModel.deleteRecord(it) }
                                )
                            }

                            NavDestination.EXPENSES,
                            NavDestination.ALL_REPORTS,
                            NavDestination.INCOME_VS_EXPENSE -> {
                                FinanceModuleScreen(
                                    destination = currentDest,
                                    metrics = metrics,
                                    transactions = transactions,
                                    records = records,
                                    onAddExpense = { title, cat, note, amt ->
                                        schoolViewModel.addRecord("Expense", title, cat, note, amt)
                                    },
                                    onDeleteExpense = { schoolViewModel.deleteRecord(it) }
                                )
                            }

                            NavDestination.STAFF_MANAGEMENT,
                            NavDestination.SALARY_MANAGEMENT,
                            NavDestination.SALARY_REPORT -> {
                                StaffAndPayrollScreen(
                                    destination = currentDest,
                                    staffList = staffList,
                                    onAddStaff = { n, r, d, m, s -> schoolViewModel.addStaff(n, r, d, m, s) },
                                    onPaySalary = { staff, month -> schoolViewModel.disburseSalary(staff, month) },
                                    onDeleteStaff = { schoolViewModel.deleteStaff(it) }
                                )
                            }

                            NavDestination.NOTICES -> {
                                CommunicationNoticesScreen(
                                    notices = records.filter { it.recordType == "Notice" },
                                    onPublishNotice = { t, aud, b -> schoolViewModel.addRecord("Notice", t, aud, b, 0L) },
                                    onDeleteNotice = { schoolViewModel.deleteRecord(it) }
                                )
                            }

                            NavDestination.ADMIN_SETTINGS,
                            NavDestination.SETTINGS,
                            NavDestination.PASSWORD_CHANGE,
                            NavDestination.LOGOUT -> {
                                AdminAndAccountScreen(
                                    destination = currentDest,
                                    config = config,
                                    darkThemeEnabled = darkThemeEnabled,
                                    onToggleDarkTheme = { schoolViewModel.toggleDarkTheme(it) },
                                    onSaveSettings = { sName, help, web, yt1, yt2, fb, ig ->
                                        schoolViewModel.saveGeneralSchoolSettings(sName, help, web, yt1, yt2, fb, ig)
                                    },
                                    onShowMessage = { schoolViewModel.postMessage(it) }
                                )
                            }
                        }
                    }
                }
            }

            if (isWideScreen) {
                // Fixed Dark Blue Sidebar on Expanded / Landscape Screens
                Row(modifier = Modifier.fillMaxSize()) {
                    SchoolSidebarContent(
                        schoolName = config.schoolName,
                        userEmail = userEmail,
                        currentDestination = currentDest,
                        onSelectDestination = { schoolViewModel.navigateTo(it) },
                        onLogoutClick = {
                            signOutUser(context, onSignOutComplete = onSignedOut, scope = scope)
                        }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        mainPaneContent(false) {}
                    }
                }
            } else {
                // Responsive Modal Drawer Dark Blue Sidebar on Handheld Portrait
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        SchoolSidebarContent(
                            schoolName = config.schoolName,
                            userEmail = userEmail,
                            currentDestination = currentDest,
                            onSelectDestination = { dest ->
                                schoolViewModel.navigateTo(dest)
                                scope.launch { drawerState.close() }
                            },
                            onLogoutClick = {
                                scope.launch { drawerState.close() }
                                signOutUser(context, onSignOutComplete = onSignedOut, scope = scope)
                            }
                        )
                    }
                ) {
                    mainPaneContent(true) {
                        scope.launch { drawerState.open() }
                    }
                }
            }
        }
    }
}
