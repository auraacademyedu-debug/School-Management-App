package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

val ALL_SCHOOL_CLASSES = listOf(
    "Nursery", "LKG", "UKG",
    "Class 1", "Class 2", "Class 3", "Class 4",
    "Class 5", "Class 6", "Class 7", "Class 8",
    "Class 9", "Class 10", "Class 11", "Class 12"
)

val ACADEMIC_SESSIONS = listOf(
    "2026-27", "2027-28", "2028-29"
)

@IgnoreExtraProperties
data class SchoolConfig(
    @DocumentId val id: String = "",
    val userId: String = "",
    val schoolName: String = "EduPulse International School",
    val helplineNumber: String = "+91 98000 11223",
    val websiteUrl: String = "https://edupulseschool.edu",
    val youtubeMainUrl: String = "https://youtube.com/@EduPulseAcademy",
    val youtubeJuniorUrl: String = "https://youtube.com/@EduPulseJunior",
    val facebookUrl: String = "https://facebook.com/EduPulseSchool",
    val instagramUrl: String = "https://instagram.com/EduPulseSchool",
    val activeSession: String = "2027-28",
    val defaultFormPrice: Long = 250L,
    val classFormPrices: Map<String, Long> = emptyMap(),
    val classTuitionFees: Map<String, Long> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolveFormPriceForClass(className: String): Long {
        val custom = (classFormPrices[className] as? Number)?.toLong()
        return if (custom != null && custom > 0L) custom else defaultFormPrice
    }

    fun resolveAnnualTuitionForClass(className: String): Long {
        val monthly = (classTuitionFees[className] as? Number)?.toLong() ?: when (className) {
            "Nursery", "LKG", "UKG" -> 1800L
            "Class 1", "Class 2", "Class 3", "Class 4", "Class 5" -> 2400L
            "Class 6", "Class 7", "Class 8" -> 3000L
            "Class 9", "Class 10" -> 3800L
            else -> 4500L
        }
        return monthly * 12L
    }
}

@IgnoreExtraProperties
data class Enquiry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val fatherName: String = "",
    val mobileNumber: String = "",
    val classInterested: String = "Nursery",
    val address: String = "",
    val remarks: String = "",
    val session: String = "2027-28",
    val status: String = "Pending",
    val formIssued: Boolean = false,
    val formPrice: Long = 250L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Student(
    @DocumentId val id: String = "",
    val userId: String = "",
    val admissionNo: String = "",
    val studentName: String = "",
    val fatherName: String = "",
    val mobileNumber: String = "",
    val className: String = "Class 1",
    val section: String = "A",
    val rollNumber: String = "1",
    val address: String = "",
    val session: String = "2027-28",
    val status: String = "Active",
    val annualFeeTotal: Long = 28800L,
    val annualFeePaid: Long = 0L,
    val attendancePresent: Long = 0L,
    val attendanceTotal: Long = 0L,
    val examPercentage: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val feeBalance: Long
        get() = (annualFeeTotal - annualFeePaid).coerceAtLeast(0L)
}

@IgnoreExtraProperties
data class FeeTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val className: String = "",
    val feeType: String = "Tuition Fee",
    val amount: Long = 0L,
    val paymentMode: String = "Cash",
    val session: String = "2027-28",
    val isDeleted: Boolean = false,
    val deletionReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StaffMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val department: String = "",
    val mobileNumber: String = "",
    val monthlySalary: Long = 0L,
    val lastPaidMonth: String = "Unpaid",
    val status: String = "Active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SchoolRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recordType: String = "Notice",
    val title: String = "",
    val subtitle: String = "",
    val details: String = "",
    val amountOrScore: Long = 0L,
    val session: String = "2027-28",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
