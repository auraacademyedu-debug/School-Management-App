package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SchoolRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. School Config & Class-wise Form Pricing ---

    fun observeConfig(userId: String = requireUserId()): Flow<SchoolConfig> = flow {
        val docPath = "users/$userId/config/main"
        emitAll(
            db.collection("users").document(userId).collection("config").document("main")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshotToSchoolConfig(snapshot)
                    } else {
                        SchoolConfig(id = "main", userId = userId)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, docPath)
                    throw error
                }
        )
    }

    private fun snapshotToSchoolConfig(snapshot: DocumentSnapshot): SchoolConfig {
        val data = snapshot.data ?: return SchoolConfig(id = snapshot.id)
        val rawPrices = data["classFormPrices"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val cleanPrices = rawPrices.entries.mapNotNull { (k, v) ->
            val key = k as? String ?: return@mapNotNull null
            val num = (v as? Number)?.toLong() ?: return@mapNotNull null
            key to num
        }.toMap()

        val rawTuition = data["classTuitionFees"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val cleanTuition = rawTuition.entries.mapNotNull { (k, v) ->
            val key = k as? String ?: return@mapNotNull null
            val num = (v as? Number)?.toLong() ?: return@mapNotNull null
            key to num
        }.toMap()

        return SchoolConfig(
            id = snapshot.id,
            userId = data["userId"] as? String ?: "",
            schoolName = data["schoolName"] as? String ?: "EduPulse International School",
            helplineNumber = data["helplineNumber"] as? String ?: "+91 98000 11223",
            websiteUrl = data["websiteUrl"] as? String ?: "https://edupulseschool.edu",
            youtubeMainUrl = data["youtubeMainUrl"] as? String ?: "https://youtube.com/@EduPulseAcademy",
            youtubeJuniorUrl = data["youtubeJuniorUrl"] as? String ?: "https://youtube.com/@EduPulseJunior",
            facebookUrl = data["facebookUrl"] as? String ?: "https://facebook.com/EduPulseSchool",
            instagramUrl = data["instagramUrl"] as? String ?: "https://instagram.com/EduPulseSchool",
            activeSession = data["activeSession"] as? String ?: "2027-28",
            defaultFormPrice = (data["defaultFormPrice"] as? Number)?.toLong() ?: 250L,
            classFormPrices = cleanPrices,
            classTuitionFees = cleanTuition,
            createdAt = snapshot.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            updatedAt = snapshot.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    suspend fun saveSchoolConfig(config: SchoolConfig): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("config").document("main")
        return try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "schoolName" to config.schoolName.trim().ifBlank { "EduPulse International School" },
                    "helplineNumber" to config.helplineNumber.trim().ifBlank { "+91 98000 11223" },
                    "websiteUrl" to config.websiteUrl.trim(),
                    "youtubeMainUrl" to config.youtubeMainUrl.trim(),
                    "youtubeJuniorUrl" to config.youtubeJuniorUrl.trim(),
                    "facebookUrl" to config.facebookUrl.trim(),
                    "instagramUrl" to config.instagramUrl.trim(),
                    "activeSession" to config.activeSession.trim().ifBlank { "2027-28" },
                    "defaultFormPrice" to config.defaultFormPrice.coerceAtLeast(0L),
                    "classFormPrices" to config.classFormPrices,
                    "classTuitionFees" to config.classTuitionFees,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "schoolName" to config.schoolName.trim().ifBlank { "EduPulse International School" },
                    "helplineNumber" to config.helplineNumber.trim().ifBlank { "+91 98000 11223" },
                    "websiteUrl" to config.websiteUrl.trim(),
                    "youtubeMainUrl" to config.youtubeMainUrl.trim(),
                    "youtubeJuniorUrl" to config.youtubeJuniorUrl.trim(),
                    "facebookUrl" to config.facebookUrl.trim(),
                    "instagramUrl" to config.instagramUrl.trim(),
                    "activeSession" to config.activeSession.trim().ifBlank { "2027-28" },
                    "defaultFormPrice" to config.defaultFormPrice.coerceAtLeast(0L),
                    "classFormPrices" to config.classFormPrices,
                    "classTuitionFees" to config.classTuitionFees,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 2. Enquiries Module ---

    fun observeEnquiries(userId: String = requireUserId()): Flow<List<Enquiry>> = flow {
        val path = "users/$userId/enquiries"
        emitAll(
            db.collection("users").document(userId).collection("enquiries")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Enquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getEnquiryById(targetOwnerId: String, enquiryId: String): Result<Enquiry?> {
        val docRef = db.collection("users").document(targetOwnerId).collection("enquiries").document(enquiryId)
        return try {
            val snap = docRef.get().await()
            Result.success(snap.toObject(Enquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createEnquiry(
        studentName: String,
        fatherName: String,
        mobileNumber: String,
        classInterested: String,
        address: String,
        remarks: String,
        session: String,
        formIssued: Boolean,
        formPrice: Long
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("enquiries")
        return try {
            val status = if (formIssued) "Form Issued" else "Pending"
            val payload = mapOf(
                "userId" to uid,
                "studentName" to studentName.trim(),
                "fatherName" to fatherName.trim(),
                "mobileNumber" to mobileNumber.trim(),
                "classInterested" to classInterested.trim(),
                "address" to address.trim(),
                "remarks" to remarks.trim(),
                "session" to session.trim().ifBlank { "2027-28" },
                "status" to status,
                "formIssued" to formIssued,
                "formPrice" to formPrice.coerceAtLeast(0L),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val docRef = colRef.add(payload).await()
            if (formIssued && formPrice > 0L) {
                recordFeeTransactionInternal(
                    uid = uid,
                    studentId = docRef.id,
                    studentName = studentName.trim(),
                    className = classInterested.trim(),
                    feeType = "Form Fee",
                    amount = formPrice.coerceAtLeast(0L),
                    paymentMode = "Cash",
                    session = session.trim().ifBlank { "2027-28" }
                )
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateEnquiry(enquiry: Enquiry, recordFormFeeIfNewlyIssued: Boolean = false): Result<Unit> {
        val uid = requireUserId()
        require(enquiry.id.isNotBlank()) { "Enquiry ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("enquiries").document(enquiry.id)
        return try {
            val updates = mapOf(
                "studentName" to enquiry.studentName.trim(),
                "fatherName" to enquiry.fatherName.trim(),
                "mobileNumber" to enquiry.mobileNumber.trim(),
                "classInterested" to enquiry.classInterested.trim(),
                "address" to enquiry.address.trim(),
                "remarks" to enquiry.remarks.trim(),
                "session" to enquiry.session.trim(),
                "status" to enquiry.status,
                "formIssued" to enquiry.formIssued,
                "formPrice" to enquiry.formPrice.coerceAtLeast(0L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            if (recordFormFeeIfNewlyIssued && enquiry.formIssued && enquiry.formPrice > 0L) {
                recordFeeTransactionInternal(
                    uid = uid,
                    studentId = enquiry.id,
                    studentName = enquiry.studentName.trim(),
                    className = enquiry.classInterested.trim(),
                    feeType = "Form Fee",
                    amount = enquiry.formPrice.coerceAtLeast(0L),
                    paymentMode = "Cash",
                    session = enquiry.session.trim()
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteEnquiry(enquiryId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("enquiries").document(enquiryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 3. Students & Admissions ---

    fun observeStudents(userId: String = requireUserId()): Flow<List<Student>> = flow {
        val path = "users/$userId/students"
        emitAll(
            db.collection("users").document(userId).collection("students")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Student::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun admitStudent(
        studentName: String,
        fatherName: String,
        mobileNumber: String,
        className: String,
        section: String,
        rollNumber: String,
        address: String,
        session: String,
        annualFeeTotal: Long,
        initialAdmissionFeePaid: Long,
        paymentMode: String = "Cash",
        linkedEnquiry: Enquiry? = null
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("students")
        return try {
            val admNo = "ADM-${(1000..9999).random()}"
            val paid = initialAdmissionFeePaid.coerceAtLeast(0L)
            val payload = mapOf(
                "userId" to uid,
                "admissionNo" to admNo,
                "studentName" to studentName.trim(),
                "fatherName" to fatherName.trim(),
                "mobileNumber" to mobileNumber.trim(),
                "className" to className.trim(),
                "section" to section.trim().ifBlank { "A" },
                "rollNumber" to rollNumber.trim().ifBlank { "1" },
                "address" to address.trim(),
                "session" to session.trim().ifBlank { "2027-28" },
                "status" to "Active",
                "annualFeeTotal" to annualFeeTotal.coerceAtLeast(0L),
                "annualFeePaid" to paid,
                "attendancePresent" to 0L,
                "attendanceTotal" to 0L,
                "examPercentage" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val docRef = colRef.add(payload).await()

            if (paid > 0L) {
                recordFeeTransactionInternal(
                    uid = uid,
                    studentId = docRef.id,
                    studentName = studentName.trim(),
                    className = className.trim(),
                    feeType = "Admission Fee",
                    amount = paid,
                    paymentMode = paymentMode,
                    session = session.trim().ifBlank { "2027-28" }
                )
            }

            if (linkedEnquiry != null && linkedEnquiry.id.isNotBlank()) {
                val enqDoc = db.collection("users").document(uid).collection("enquiries").document(linkedEnquiry.id)
                val issueFormNow = !linkedEnquiry.formIssued
                enqDoc.update(
                    mapOf(
                        "status" to "Admitted",
                        "formIssued" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (issueFormNow && linkedEnquiry.formPrice > 0L) {
                    recordFeeTransactionInternal(
                        uid = uid,
                        studentId = linkedEnquiry.id,
                        studentName = linkedEnquiry.studentName.trim(),
                        className = linkedEnquiry.classInterested.trim(),
                        feeType = "Form Fee",
                        amount = linkedEnquiry.formPrice,
                        paymentMode = paymentMode,
                        session = session.trim().ifBlank { "2027-28" }
                    )
                }
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateStudent(student: Student): Result<Unit> {
        val uid = requireUserId()
        require(student.id.isNotBlank()) { "Student ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("students").document(student.id)
        return try {
            val updates = mapOf(
                "admissionNo" to student.admissionNo,
                "studentName" to student.studentName.trim(),
                "fatherName" to student.fatherName.trim(),
                "mobileNumber" to student.mobileNumber.trim(),
                "className" to student.className.trim(),
                "section" to student.section.trim(),
                "rollNumber" to student.rollNumber.trim(),
                "address" to student.address.trim(),
                "session" to student.session.trim(),
                "status" to student.status,
                "annualFeeTotal" to student.annualFeeTotal.coerceAtLeast(0L),
                "annualFeePaid" to student.annualFeePaid.coerceAtLeast(0L),
                "attendancePresent" to student.attendancePresent.coerceIn(0L, 400L),
                "attendanceTotal" to student.attendanceTotal.coerceIn(0L, 400L),
                "examPercentage" to student.examPercentage.coerceIn(0L, 100L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(studentId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 4. Fee Transactions & Deletion Log ---

    fun observeFeeTransactions(userId: String = requireUserId()): Flow<List<FeeTransaction>> = flow {
        val path = "users/$userId/fee_transactions"
        emitAll(
            db.collection("users").document(userId).collection("fee_transactions")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FeeTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    private suspend fun recordFeeTransactionInternal(
        uid: String,
        studentId: String,
        studentName: String,
        className: String,
        feeType: String,
        amount: Long,
        paymentMode: String,
        session: String
    ): String {
        val colRef = db.collection("users").document(uid).collection("fee_transactions")
        val payload = mapOf(
            "userId" to uid,
            "studentId" to studentId,
            "studentName" to studentName,
            "className" to className,
            "feeType" to feeType,
            "amount" to amount.coerceAtLeast(0L),
            "paymentMode" to paymentMode,
            "session" to session,
            "isDeleted" to false,
            "deletionReason" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return colRef.add(payload).await().id
    }

    suspend fun collectStudentFee(
        student: Student,
        feeType: String,
        amount: Long,
        paymentMode: String
    ): Result<String> {
        val uid = requireUserId()
        val colPath = "users/$uid/fee_transactions"
        return try {
            val txId = recordFeeTransactionInternal(
                uid = uid,
                studentId = student.id,
                studentName = student.studentName,
                className = student.className,
                feeType = feeType,
                amount = amount,
                paymentMode = paymentMode,
                session = student.session
            )
            if (student.id.isNotBlank()) {
                updateStudent(student.copy(annualFeePaid = student.annualFeePaid + amount))
            }
            Result.success(txId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun softDeleteFeeTransaction(txId: String, reason: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("fee_transactions").document(txId)
        return try {
            docRef.update(
                mapOf(
                    "isDeleted" to true,
                    "deletionReason" to reason.trim().ifBlank { "Cancelled by Admin" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 5. Staff & Payroll ---

    fun observeStaff(userId: String = requireUserId()): Flow<List<StaffMember>> = flow {
        val path = "users/$userId/staff"
        emitAll(
            db.collection("users").document(userId).collection("staff")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(StaffMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addStaffMember(
        name: String,
        role: String,
        department: String,
        mobileNumber: String,
        monthlySalary: Long
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("staff")
        return try {
            val payload = mapOf(
                "userId" to uid,
                "name" to name.trim(),
                "role" to role.trim(),
                "department" to department.trim(),
                "mobileNumber" to mobileNumber.trim(),
                "monthlySalary" to monthlySalary.coerceAtLeast(0L),
                "lastPaidMonth" to "Unpaid",
                "status" to "Active",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = colRef.add(payload).await()
            Result.success(doc.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun payStaffSalary(staff: StaffMember, monthLabel: String, session: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("staff").document(staff.id)
        return try {
            docRef.update(
                mapOf(
                    "lastPaidMonth" to monthLabel.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addSchoolRecord(
                recordType = "Expense",
                title = "Salary: ${staff.name}",
                subtitle = "${staff.role} ($monthLabel)",
                details = "Monthly payroll disbursement",
                amountOrScore = staff.monthlySalary,
                session = session
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStaffMember(staffId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("staff").document(staffId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 6. School Records (Expenses, Notices, Subjects, Exams, Marks) ---

    fun observeRecords(userId: String = requireUserId()): Flow<List<SchoolRecord>> = flow {
        val path = "users/$userId/records"
        emitAll(
            db.collection("users").document(userId).collection("records")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SchoolRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addSchoolRecord(
        recordType: String,
        title: String,
        subtitle: String,
        details: String,
        amountOrScore: Long,
        session: String
    ): Result<String> {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("records")
        return try {
            val payload = mapOf(
                "userId" to uid,
                "recordType" to recordType,
                "title" to title.trim(),
                "subtitle" to subtitle.trim(),
                "details" to details.trim(),
                "amountOrScore" to amountOrScore.coerceAtLeast(0L),
                "session" to session.trim().ifBlank { "2027-28" },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = colRef.add(payload).await()
            Result.success(doc.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSchoolRecord(recordId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
