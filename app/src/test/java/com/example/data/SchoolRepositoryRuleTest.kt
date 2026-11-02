package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SchoolRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveEnquiry_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser("alice_school@test.com")
        val repository = SchoolRepository(firestore)

        val createRes = withTimeout(5000L) {
            repository.createEnquiry(
                studentName = "Ishaan Verma",
                fatherName = "Vikram Verma",
                mobileNumber = "9876543210",
                classInterested = "Class 2",
                address = "Salt Lake Sector V",
                remarks = "Needs school bus",
                session = "2027-28",
                formIssued = true,
                formPrice = 300L
            )
        }
        assertTrue("Expected enquiry creation to succeed: ${createRes.exceptionOrNull()}", createRes.isSuccess)
        val enquiryId = createRes.getOrThrow()

        val enquiries = withTimeout(4000L) {
            repository.observeEnquiries(aliceUid).first { list -> list.any { it.id == enquiryId } }
        }
        assertTrue(enquiries.any { it.id == enquiryId && it.studentName == "Ishaan Verma" })
    }

    @Test
    fun saveSchoolConfig_andClassFormPrices_succeeds() = runBlocking {
        val aliceUid = signInTestUser("alice_cfg@test.com")
        val repository = SchoolRepository(firestore)

        val cfg = SchoolConfig(
            userId = aliceUid,
            schoolName = "EduPulse Model School",
            helplineNumber = "+91 98111 22334",
            activeSession = "2027-28",
            defaultFormPrice = 250L,
            classFormPrices = mapOf("Nursery" to 200L, "Class 1" to 300L, "Class 10" to 500L)
        )

        val saveRes = withTimeout(5000L) { repository.saveSchoolConfig(cfg) }
        assertTrue("Expected saveSchoolConfig to succeed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val loaded = withTimeout(4000L) {
            repository.observeConfig(aliceUid).first { it.defaultFormPrice == 250L && it.classFormPrices["Class 10"] == 500L }
        }
        assertEquals(500L, loaded.resolveFormPriceForClass("Class 10"))
        assertEquals(250L, loaded.resolveFormPriceForClass("Class 4"))
    }

    @Test
    fun crossUserReadEnquiry_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_priv@test.com")
        val aliceRepo = SchoolRepository(firestore)
        val enqId = withTimeout(5000L) {
            aliceRepo.createEnquiry(
                studentName = "Ananya Roy",
                fatherName = "Suman Roy",
                mobileNumber = "9000011111",
                classInterested = "UKG",
                address = "Ballygunge",
                remarks = "Private",
                session = "2027-28",
                formIssued = false,
                formPrice = 250L
            ).getOrThrow()
        }

        signInTestUser("bob_intruder@test.com")
        val bobRepo = SchoolRepository(firestore)
        try {
            withTimeout(5000L) { bobRepo.getEnquiryById(aliceUid, enqId) }
            fail("Expected cross-user getEnquiryById to throw FirebaseFirestoreException")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException() ?: (e as? FirebaseFirestoreException)
            assertNotNull("Expected FirebaseFirestoreException but got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun unauthenticatedObserveEnquiries_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_unauth@test.com")
        auth.signOut()
        val repository = SchoolRepository(firestore)

        try {
            withTimeout(4000L) {
                repository.observeEnquiries(aliceUid).first()
            }
            fail("Expected unauthenticated observeEnquiries to fail")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }
}
