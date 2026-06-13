package com.example.vetcarepro.domain.repository

import com.example.vetcarepro.domain.model.AppNotification
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.AppUser
import com.example.vetcarepro.domain.model.Branch
import com.example.vetcarepro.domain.model.DashboardSummary
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.OfflineGuide
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.SessionState
import com.example.vetcarepro.domain.model.ServiceItem
import com.example.vetcarepro.domain.model.UserRole
import com.example.vetcarepro.domain.model.VaccineRecord
import kotlinx.coroutines.flow.StateFlow

interface VetCareRepository {
    val session: StateFlow<SessionState>
    val users: StateFlow<List<AppUser>>
    val owners: StateFlow<List<Owner>>
    val pets: StateFlow<List<Pet>>
    val appointments: StateFlow<List<Appointment>>
    val medicalRecords: StateFlow<List<MedicalRecord>>
    val vaccines: StateFlow<List<VaccineRecord>>
    val branches: StateFlow<List<Branch>>
    val services: StateFlow<List<ServiceItem>>
    val notifications: StateFlow<List<AppNotification>>
    val mediaCatalog: StateFlow<List<MultimediaItem>>
    val offlineGuides: StateFlow<List<OfflineGuide>>
    val dashboard: StateFlow<DashboardSummary>

    suspend fun bootstrap()
    suspend fun login(email: String, password: String): Result<AppUser>
    suspend fun loginWithGoogle(idToken: String): Result<AppUser>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun logout()
    suspend fun saveOwner(owner: Owner): Result<Owner>
    suspend fun savePet(pet: Pet, photoBytes: ByteArray? = null): Result<Pet>
    suspend fun findPetByQrCode(qrCode: String): Pet?
    suspend fun saveAppointment(appointment: Appointment): Result<Appointment>
    suspend fun cancelAppointment(appointmentId: String): Result<Unit>
    suspend fun saveMedicalRecord(record: MedicalRecord): Result<MedicalRecord>
    suspend fun saveVaccineRecord(record: VaccineRecord): Result<VaccineRecord>
    suspend fun markNotificationRead(notificationId: String)
    suspend fun refreshReminders()
    fun searchPets(query: String, vaccinationOnly: Boolean = false, serviceType: String = "", role: UserRole? = null): List<Pet>
    fun appointmentsForPet(petId: String): List<Appointment>
    fun medicalRecordsForPet(petId: String): List<MedicalRecord>
    fun vaccinesForPet(petId: String): List<VaccineRecord>
    fun branchById(branchId: String): Branch?
    fun ownerById(ownerId: String): Owner?
    fun petById(petId: String): Pet?
    fun userById(userId: String): AppUser?
    fun mediaByCategory(category: MediaCategory? = null): List<MultimediaItem>
    fun guidesByQuery(query: String = ""): List<OfflineGuide>
    fun canAccessRoute(role: UserRole?, route: String): Boolean
}

