package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Enquiry
import com.example.data.FeeTransaction
import com.example.data.SchoolConfig
import com.example.data.SchoolRecord
import com.example.data.SchoolRepository
import com.example.data.StaffMember
import com.example.data.Student
import com.example.ui.navigation.NavDestination
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardMetrics(
    val totalEnquiries: Int = 0,
    val formsIssued: Int = 0,
    val totalAdmissions: Int = 0,
    val formCollectionAmount: Long = 0L,
    val totalFeeCollected: Long = 0L,
    val totalExpenses: Long = 0L
)

class SchoolViewModel(
    private val repository: SchoolRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(NavDestination.DASHBOARD)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _selectedSession = MutableStateFlow("2027-28")
    val selectedSession: StateFlow<String> = _selectedSession.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _prefilledAdmissionEnquiry = MutableStateFlow<Enquiry?>(null)
    val prefilledAdmissionEnquiry: StateFlow<Enquiry?> = _prefilledAdmissionEnquiry.asStateFlow()

    private val _darkThemeEnabled = MutableStateFlow(false)
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled.asStateFlow()

    val configState: StateFlow<UiState<SchoolConfig>> = repository.observeConfig(currentUserId)
        .map<SchoolConfig, UiState<SchoolConfig>> { cfg ->
            if (cfg.activeSession.isNotBlank()) {
                _selectedSession.value = cfg.activeSession
            }
            UiState.Success(cfg)
        }
        .catch { err ->
            Log.w(TAG, "Error observing school config", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load school settings"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val enquiriesState: StateFlow<UiState<List<Enquiry>>> = repository.observeEnquiries(currentUserId)
        .map<List<Enquiry>, UiState<List<Enquiry>>> { list ->
            UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { err ->
            Log.w(TAG, "Error observing enquiries", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load enquiries"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val studentsState: StateFlow<UiState<List<Student>>> = repository.observeStudents(currentUserId)
        .map<List<Student>, UiState<List<Student>>> { list ->
            UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { err ->
            Log.w(TAG, "Error observing students", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load students"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val transactionsState: StateFlow<UiState<List<FeeTransaction>>> = repository.observeFeeTransactions(currentUserId)
        .map<List<FeeTransaction>, UiState<List<FeeTransaction>>> { list ->
            UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { err ->
            Log.w(TAG, "Error observing transactions", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load fee transactions"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val staffState: StateFlow<UiState<List<StaffMember>>> = repository.observeStaff(currentUserId)
        .map<List<StaffMember>, UiState<List<StaffMember>>> { list ->
            UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { err ->
            Log.w(TAG, "Error observing staff", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load staff members"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val recordsState: StateFlow<UiState<List<SchoolRecord>>> = repository.observeRecords(currentUserId)
        .map<List<SchoolRecord>, UiState<List<SchoolRecord>>> { list ->
            UiState.Success(list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { err ->
            Log.w(TAG, "Error observing school records", err)
            emit(UiState.Error(err.localizedMessage ?: "Failed to load school records"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        enquiriesState,
        studentsState,
        transactionsState,
        recordsState,
        _selectedSession
    ) { enqUi, stuUi, txUi, recUi, session ->
        val enquiries = (enqUi as? UiState.Success)?.data?.filter { it.session == session } ?: emptyList()
        val students = (stuUi as? UiState.Success)?.data?.filter { it.session == session && it.status == "Active" } ?: emptyList()
        val activeTxs = (txUi as? UiState.Success)?.data?.filter { it.session == session && !it.isDeleted } ?: emptyList()
        val expenses = (recUi as? UiState.Success)?.data?.filter { it.session == session && it.recordType == "Expense" } ?: emptyList()

        val formsIssuedCount = enquiries.count { it.formIssued || it.status == "Form Issued" || it.status == "Admitted" }
        val formTxCollection = activeTxs.filter { it.feeType == "Form Fee" }.sumOf { it.amount }
        val enquiryFormCollection = enquiries.filter { it.formIssued }.sumOf { it.formPrice }
        val resolvedFormCollection = maxOf(formTxCollection, enquiryFormCollection)

        DashboardMetrics(
            totalEnquiries = enquiries.size,
            formsIssued = formsIssuedCount,
            totalAdmissions = students.size,
            formCollectionAmount = resolvedFormCollection,
            totalFeeCollected = activeTxs.sumOf { it.amount },
            totalExpenses = expenses.sumOf { it.amountOrScore }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardMetrics())

    fun navigateTo(destination: NavDestination) {
        if (destination != NavDestination.NEW_ADMISSION) {
            _prefilledAdmissionEnquiry.value = null
        }
        _currentDestination.value = destination
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _darkThemeEnabled.value = enabled
    }

    fun selectSession(session: String) {
        _selectedSession.value = session
        val currentCfg = (configState.value as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
        viewModelScope.launch {
            repository.saveSchoolConfig(currentCfg.copy(activeSession = session))
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun postMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun startTakeAdmissionFromEnquiry(enquiry: Enquiry) {
        _prefilledAdmissionEnquiry.value = enquiry
        _currentDestination.value = NavDestination.NEW_ADMISSION
    }

    // --- Actions ---

    fun saveEnquiry(
        studentName: String,
        fatherName: String,
        mobileNumber: String,
        classInterested: String,
        address: String,
        remarks: String,
        issueFormNow: Boolean
    ) {
        val currentCfg = (configState.value as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
        val price = currentCfg.resolveFormPriceForClass(classInterested)
        val session = _selectedSession.value
        viewModelScope.launch {
            val res = repository.createEnquiry(
                studentName = studentName,
                fatherName = fatherName,
                mobileNumber = mobileNumber,
                classInterested = classInterested,
                address = address,
                remarks = remarks,
                session = session,
                formIssued = issueFormNow,
                formPrice = price
            )
            res.fold(
                onSuccess = { _snackbarMessage.value = "Enquiry saved for $studentName (Form ₹$price)" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to save enquiry" }
            )
        }
    }

    fun updateExistingEnquiry(enquiry: Enquiry, newlyIssuedForm: Boolean = false) {
        viewModelScope.launch {
            val res = repository.updateEnquiry(enquiry, recordFormFeeIfNewlyIssued = newlyIssuedForm)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Enquiry updated for ${enquiry.studentName}" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to update enquiry" }
            )
        }
    }

    fun deleteEnquiry(enquiry: Enquiry) {
        viewModelScope.launch {
            val res = repository.deleteEnquiry(enquiry.id)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Deleted enquiry for ${enquiry.studentName}" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to delete enquiry" }
            )
        }
    }

    fun saveFormPricing(defaultPrice: Long, classPrices: Map<String, Long>) {
        val currentCfg = (configState.value as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
        viewModelScope.launch {
            val updated = currentCfg.copy(
                defaultFormPrice = defaultPrice.coerceAtLeast(0L),
                classFormPrices = classPrices,
                activeSession = _selectedSession.value
            )
            val res = repository.saveSchoolConfig(updated)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Admission form prices saved successfully!" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to save prices" }
            )
        }
    }

    fun saveTuitionStructure(classTuitionMap: Map<String, Long>) {
        val currentCfg = (configState.value as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
        viewModelScope.launch {
            val res = repository.saveSchoolConfig(currentCfg.copy(classTuitionFees = classTuitionMap))
            res.fold(
                onSuccess = { _snackbarMessage.value = "Monthly tuition structure updated!" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to save fee structure" }
            )
        }
    }

    fun saveGeneralSchoolSettings(
        schoolName: String,
        helplineNumber: String,
        websiteUrl: String,
        youtubeMainUrl: String,
        youtubeJuniorUrl: String,
        facebookUrl: String,
        instagramUrl: String
    ) {
        val currentCfg = (configState.value as? UiState.Success)?.data ?: SchoolConfig(userId = currentUserId)
        viewModelScope.launch {
            val res = repository.saveSchoolConfig(
                currentCfg.copy(
                    schoolName = schoolName,
                    helplineNumber = helplineNumber,
                    websiteUrl = websiteUrl,
                    youtubeMainUrl = youtubeMainUrl,
                    youtubeJuniorUrl = youtubeJuniorUrl,
                    facebookUrl = facebookUrl,
                    instagramUrl = instagramUrl
                )
            )
            res.fold(
                onSuccess = { _snackbarMessage.value = "School profile & footer links updated!" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to update settings" }
            )
        }
    }

    fun admitStudent(
        studentName: String,
        fatherName: String,
        mobileNumber: String,
        className: String,
        section: String,
        rollNumber: String,
        address: String,
        annualFeeTotal: Long,
        initialPaid: Long,
        paymentMode: String,
        linkedEnquiry: Enquiry?
    ) {
        viewModelScope.launch {
            val res = repository.admitStudent(
                studentName = studentName,
                fatherName = fatherName,
                mobileNumber = mobileNumber,
                className = className,
                section = section,
                rollNumber = rollNumber,
                address = address,
                session = _selectedSession.value,
                annualFeeTotal = annualFeeTotal,
                initialAdmissionFeePaid = initialPaid,
                paymentMode = paymentMode,
                linkedEnquiry = linkedEnquiry
            )
            res.fold(
                onSuccess = {
                    _prefilledAdmissionEnquiry.value = null
                    _snackbarMessage.value = "$studentName admitted to $className!"
                    _currentDestination.value = NavDestination.ALL_STUDENTS
                },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to admit student" }
            )
        }
    }

    fun updateStudent(student: Student, successMsg: String = "Student record updated") {
        viewModelScope.launch {
            val res = repository.updateStudent(student)
            res.fold(
                onSuccess = { _snackbarMessage.value = successMsg },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to update student" }
            )
        }
    }

    fun promoteClassStudents(studentsToPromote: List<Student>, targetClass: String, targetSession: String) {
        viewModelScope.launch {
            var count = 0
            studentsToPromote.forEach { stu ->
                val newStatus = if (targetClass == "Passed Out") "Passed Out" else "Active"
                val newClass = if (targetClass == "Passed Out") stu.className else targetClass
                val r = repository.updateStudent(
                    stu.copy(className = newClass, session = targetSession, status = newStatus)
                )
                if (r.isSuccess) count++
            }
            _snackbarMessage.value = "Promoted $count student(s) to $targetClass ($targetSession)"
        }
    }

    fun markStudentAttendance(student: Student, isPresent: Boolean) {
        viewModelScope.launch {
            val updated = student.copy(
                attendanceTotal = student.attendanceTotal + 1L,
                attendancePresent = student.attendancePresent + if (isPresent) 1L else 0L
            )
            repository.updateStudent(updated)
            _snackbarMessage.value = "${student.studentName} marked ${if (isPresent) "Present" else "Absent"}"
        }
    }

    fun collectFee(student: Student, feeType: String, amount: Long, paymentMode: String) {
        viewModelScope.launch {
            val res = repository.collectStudentFee(student, feeType, amount, paymentMode)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Collected ₹$amount ($feeType) from ${student.studentName}" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Fee collection failed" }
            )
        }
    }

    fun cancelFeeTransaction(tx: FeeTransaction, reason: String) {
        viewModelScope.launch {
            val res = repository.softDeleteFeeTransaction(tx.id, reason)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Receipt marked deleted and logged in Audit Log" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to delete receipt" }
            )
        }
    }

    fun addStaff(name: String, role: String, department: String, mobile: String, salary: Long) {
        viewModelScope.launch {
            val res = repository.addStaffMember(name, role, department, mobile, salary)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Added staff member $name" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to add staff" }
            )
        }
    }

    fun disburseSalary(staff: StaffMember, monthLabel: String) {
        viewModelScope.launch {
            val res = repository.payStaffSalary(staff, monthLabel, _selectedSession.value)
            res.fold(
                onSuccess = { _snackbarMessage.value = "Salary ₹${staff.monthlySalary} disbursed to ${staff.name} for $monthLabel" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Payroll disbursement failed" }
            )
        }
    }

    fun deleteStaff(staff: StaffMember) {
        viewModelScope.launch {
            repository.deleteStaffMember(staff.id)
            _snackbarMessage.value = "Removed ${staff.name}"
        }
    }

    fun addRecord(recordType: String, title: String, subtitle: String, details: String, amountOrScore: Long) {
        viewModelScope.launch {
            val res = repository.addSchoolRecord(
                recordType = recordType,
                title = title,
                subtitle = subtitle,
                details = details,
                amountOrScore = amountOrScore,
                session = _selectedSession.value
            )
            res.fold(
                onSuccess = { _snackbarMessage.value = "$recordType saved: $title" },
                onFailure = { _snackbarMessage.value = it.localizedMessage ?: "Failed to save $recordType" }
            )
        }
    }

    fun deleteRecord(record: SchoolRecord) {
        viewModelScope.launch {
            repository.deleteSchoolRecord(record.id)
            _snackbarMessage.value = "Deleted ${record.title}"
        }
    }

    private companion object {
        const val TAG = "SchoolViewModel"
    }
}
