package com.example.vetcarepro.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vetcarepro.data.local.LocalVetCareStore
import com.example.vetcarepro.data.remote.toAppNotification
import com.example.vetcarepro.data.remote.toAppUser
import com.example.vetcarepro.data.remote.toOwner
import com.example.vetcarepro.data.remote.toAppointment
import com.example.vetcarepro.data.remote.toFirestore
import com.example.vetcarepro.data.remote.toMedicalRecord
import com.example.vetcarepro.data.remote.toPet
import com.example.vetcarepro.data.remote.toVaccineRecord
import com.example.vetcarepro.domain.model.AppNotification
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppUser
import com.example.vetcarepro.domain.model.Branch
import com.example.vetcarepro.domain.model.DashboardSummary
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.NotificationType
import com.example.vetcarepro.domain.model.OfflineGuide
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.SessionState
import com.example.vetcarepro.domain.model.ServiceItem
import com.example.vetcarepro.domain.model.UserRole
import com.example.vetcarepro.domain.model.VaccineRecord
import com.example.vetcarepro.domain.repository.VetCareRepository
import com.example.vetcarepro.data.messaging.VetCareNotificationHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val Context.vetcareSessionStore by preferencesDataStore(name = "vetcare_session")

@Singleton
class FirebaseVetCareRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localStore: LocalVetCareStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : VetCareRepository {
    private val firebaseReady: Boolean = true

    private val auth: FirebaseAuth?
        get() = if (firebaseReady) FirebaseAuth.getInstance() else null

    private val firestore: FirebaseFirestore?
        get() = if (firebaseReady) FirebaseFirestore.getInstance() else null

    private val storage: FirebaseStorage?
        get() = if (firebaseReady) FirebaseStorage.getInstance() else null

    override val session: StateFlow<SessionState> = localStore.session
    override val users: StateFlow<List<AppUser>> = localStore.users
    override val owners: StateFlow<List<Owner>> = localStore.owners
    override val pets: StateFlow<List<Pet>> = localStore.pets
    override val appointments: StateFlow<List<Appointment>> = localStore.appointments
    override val medicalRecords: StateFlow<List<MedicalRecord>> = localStore.medicalRecords
    override val vaccines: StateFlow<List<VaccineRecord>> = localStore.vaccines
    override val branches: StateFlow<List<Branch>> = localStore.branches
    override val services: StateFlow<List<ServiceItem>> = localStore.services
    override val notifications: StateFlow<List<AppNotification>> = localStore.notifications
    override val mediaCatalog: StateFlow<List<MultimediaItem>> = localStore.mediaCatalog
    override val offlineGuides: StateFlow<List<OfflineGuide>> = localStore.offlineGuides
    override val dashboard: StateFlow<DashboardSummary> = localStore.dashboard

    override suspend fun bootstrap() {
        withContext(ioDispatcher) {
            localStore.bootstrap()
            loadPersistentSession()
            setupListeners()
            if (firebaseReady && auth?.currentUser != null && session.value.user == null) {
                localStore.login(
                    email = auth!!.currentUser?.email.orEmpty(),
                    password = "Vet12345"
                )
            }
        }
    }

    private fun setupListeners() {
        val firestore = firestore ?: return
        
        firestore.collection("owners").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setOwners(it.documents.map { doc -> doc.data.orEmpty().toOwner() }) }
        }
        firestore.collection("pets").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setPets(it.documents.map { doc -> doc.data.orEmpty().toPet() }) }
        }
        firestore.collection("appointments").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setAppointments(it.documents.map { doc -> doc.data.orEmpty().toAppointment() }) }
        }
        firestore.collection("medical_records").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setMedicalRecords(it.documents.map { doc -> doc.data.orEmpty().toMedicalRecord() }) }
        }
        firestore.collection("vaccines").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setVaccines(it.documents.map { doc -> doc.data.orEmpty().toVaccineRecord() }) }
        }
        firestore.collection("notifications").addSnapshotListener { snapshot, _ ->
            snapshot?.let { localStore.setNotifications(it.documents.map { doc -> doc.data.orEmpty().toAppNotification() }) }
        }
    }

    override suspend fun login(email: String, password: String): Result<AppUser> {
        val localResult = localStore.login(email, password)
        if (localResult.isSuccess) {
            persistSession(localResult.getOrNull())
            syncAuthToFirebase(email, password)
        }
        return localResult
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AppUser> {
        val auth = auth ?: return Result.failure(Exception("Firebase not ready"))
        return runCatching {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google sign in failed")
            
            // Map Firebase User to AppUser. Try to fetch from Firestore first.
            val email = firebaseUser.email.orEmpty()
            
            val userDoc = runCatching { firestore?.collection("users")?.document(firebaseUser.uid)?.get()?.await() }.getOrNull()
            val appUser = if (userDoc?.exists() == true) {
                userDoc.data.orEmpty().toAppUser()
            } else {
                val existing = localStore.users.value.firstOrNull { it.email.equals(email, ignoreCase = true) }
                existing ?: AppUser(
                    id = firebaseUser.uid,
                    email = email,
                    fullName = firebaseUser.displayName ?: "Google User",
                    role = UserRole.PROPIETARIO
                )
            }
            
            persistSession(appUser)
            localStore.setSession(appUser) // Explicitly update local store session
            
            // Auto-register as Owner if Propietario
            if (appUser.role == UserRole.PROPIETARIO) {
                saveOwner(Owner(
                    id = appUser.id,
                    fullName = appUser.fullName,
                    email = appUser.email,
                    phone = ""
                ))
            }

            appUser
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        val local = localStore.forgotPassword(email)
        if (local.isSuccess) {
            runCatching {
                auth?.sendPasswordResetEmail(email.trim())?.await()
            }
        }
        return local
    }

    override suspend fun logout() {
        localStore.logout()
        context.vetcareSessionStore.edit { it.clear() }
        auth?.signOut()
    }

    override suspend fun saveOwner(owner: Owner): Result<Owner> {
        val saved = localStore.saveOwner(owner)
        if (saved.isSuccess) {
            syncDocument("owners", owner.id, owner.toFirestore())
        }
        return saved
    }

    override suspend fun savePet(pet: Pet, photoBytes: ByteArray?): Result<Pet> {
        val saved = localStore.savePet(pet, photoBytes)
        if (saved.isSuccess) {
            val value = saved.getOrNull() ?: pet
            syncDocument("owners", value.ownerId, mapOf("id" to value.ownerId, "fullName" to value.ownerName))
            syncPet(value, photoBytes)
        }
        return saved
    }

    override suspend fun findPetByQrCode(qrCode: String): Pet? = localStore.findPetByQrCode(qrCode)

    override suspend fun saveAppointment(appointment: Appointment): Result<Appointment> {
        val saved = localStore.saveAppointment(appointment)
        if (saved.isSuccess) {
            syncDocument("appointments", appointment.id, appointment.toFirestore())
        }
        return saved
    }

    override suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        val result = localStore.cancelAppointment(appointmentId)
        if (result.isSuccess) {
            syncDocument("appointments", appointmentId, localStore.appointments.value.first { it.id == appointmentId }.toFirestore())
        }
        return result
    }

    override suspend fun saveMedicalRecord(record: MedicalRecord): Result<MedicalRecord> {
        val saved = localStore.saveMedicalRecord(record)
        if (saved.isSuccess) {
            syncDocument("medical_records", record.id, record.toFirestore())
            if (record.prescriptionApproved || record.prescriptionSent) {
                localStore.markNotificationRead(record.id)
            }
        }
        return saved
    }

    override suspend fun saveVaccineRecord(record: VaccineRecord): Result<VaccineRecord> {
        val saved = localStore.saveVaccineRecord(record)
        if (saved.isSuccess) {
            syncDocument("vaccines", record.id, record.toFirestore())
        }
        return saved
    }

    override suspend fun markNotificationRead(notificationId: String) {
        localStore.markNotificationRead(notificationId)
        localStore.notifications.value.firstOrNull { it.id == notificationId }?.let { syncDocument("notifications", notificationId, it.toFirestore()) }
    }

    override suspend fun refreshReminders() {
        localStore.refreshReminders()
        localStore.notifications.value.forEach { notification ->
            if (!notification.isRead) {
                VetCareNotificationHelper.showActionableNotification(
                    context,
                    notification.title,
                    notification.message,
                    notification.targetRoute
                )
            }
            syncDocument("notifications", notification.id, notification.toFirestore())
        }
    }

    override fun searchPets(query: String, vaccinationOnly: Boolean, serviceType: String, role: UserRole?): List<Pet> =
        localStore.searchPets(query, vaccinationOnly, serviceType, role)

    override fun appointmentsForPet(petId: String): List<Appointment> = localStore.appointmentsForPet(petId)
    override fun medicalRecordsForPet(petId: String): List<MedicalRecord> = localStore.medicalRecordsForPet(petId)
    override fun vaccinesForPet(petId: String): List<VaccineRecord> = localStore.vaccinesForPet(petId)
    override fun branchById(branchId: String): Branch? = localStore.branchById(branchId)
    override fun ownerById(ownerId: String): Owner? = localStore.ownerById(ownerId)
    override fun petById(petId: String): Pet? = localStore.petById(petId)
    override fun userById(userId: String): AppUser? = localStore.userById(userId)
    override fun mediaByCategory(category: MediaCategory?): List<MultimediaItem> = localStore.mediaByCategory(category)
    override fun guidesByQuery(query: String): List<OfflineGuide> = localStore.guidesByQuery(query)
    override fun canAccessRoute(role: UserRole?, route: String): Boolean = localStore.canAccessRoute(role, route)

    private suspend fun loadPersistentSession() {
        val prefs = context.vetcareSessionStore.data.first()
        val email = prefs[SessionKeys.EMAIL].orEmpty()
        val userId = prefs[SessionKeys.USER_ID].orEmpty()
        if (email.isBlank() || userId.isBlank()) return
        val user = localStore.users.value.firstOrNull { it.id == userId || it.email.equals(email, ignoreCase = true) }
        if (user != null) {
            localStore.login(email, "Vet12345")
        }
    }

    private suspend fun persistSession(user: AppUser?) {
        if (user == null) return
        context.vetcareSessionStore.edit { prefs ->
            prefs[SessionKeys.EMAIL] = user.email
            prefs[SessionKeys.USER_ID] = user.id
            prefs[SessionKeys.USER_ROLE] = user.role.name
            prefs[SessionKeys.USER_NAME] = user.fullName
        }
    }

    private suspend fun syncAuthToFirebase(email: String, password: String) {
        val auth = auth ?: return
        runCatching { auth.signInWithEmailAndPassword(email, password).await() }
    }

    private suspend fun syncPet(pet: Pet, photoBytes: ByteArray?) {
        val firestore = firestore ?: return
        val payload = pet.toFirestore().toMutableMap()
        if (photoBytes != null) {
            val uploaded = uploadBytes("pets/${pet.id}/${UUID.randomUUID()}.jpg", photoBytes)
            if (uploaded.isNotBlank()) {
                payload["photoUrl"] = uploaded
            }
        }
        syncDocument("pets", pet.id, payload)
    }

    private suspend fun uploadBytes(path: String, bytes: ByteArray): String {
        val storage = storage ?: return ""
        return runCatching {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }.getOrDefault("")
    }

    private suspend fun syncDocument(collection: String, documentId: String, data: Map<String, Any?>) {
        val firestore = firestore ?: return
        if (documentId.isBlank() || data.isEmpty()) return
        runCatching {
            firestore.collection(collection).document(documentId).set(data).await()
        }
    }

    private object SessionKeys {
        val EMAIL = stringPreferencesKey("session_email")
        val USER_ID = stringPreferencesKey("session_user_id")
        val USER_ROLE = stringPreferencesKey("session_user_role")
        val USER_NAME = stringPreferencesKey("session_user_name")
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}


