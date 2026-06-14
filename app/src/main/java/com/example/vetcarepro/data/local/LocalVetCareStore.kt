package com.example.vetcarepro.data.local

import android.content.Context
import com.example.vetcarepro.domain.model.AppNotification
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.AppUser
import com.example.vetcarepro.domain.model.Branch
import com.example.vetcarepro.domain.model.DashboardSummary
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MediaType
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
import com.example.vetcarepro.domain.model.VaccinationStatus
import com.example.vetcarepro.domain.model.displayQrCode
import com.example.vetcarepro.domain.model.status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class LocalVetCareStore(private val appContext: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sampleCredentials = mapOf(
        "vet@vetcare.pro" to "Vet12345",
        "recepcion@vetcare.pro" to "Vet12345",
        "owner@vetcare.pro" to "Vet12345"
    )

    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session.asStateFlow()

    private val _users = MutableStateFlow(
        listOf(
            AppUser(id = "u-vet", email = "vet@vetcare.pro", fullName = "Dra. Ana Torres", role = UserRole.VETERINARIO, branchId = "b-1"),
            AppUser(id = "u-rec", email = "recepcion@vetcare.pro", fullName = "Carla Mendoza", role = UserRole.RECEPCIONISTA, branchId = "b-2"),
            AppUser(id = "u-own", email = "owner@vetcare.pro", fullName = "Javier Ruiz", role = UserRole.PROPIETARIO, branchId = "b-1")
        )
    )
    val users: StateFlow<List<AppUser>> = _users.asStateFlow()

    private val _owners = MutableStateFlow<List<Owner>>(emptyList())
    val owners: StateFlow<List<Owner>> = _owners.asStateFlow()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _vaccines = MutableStateFlow<List<VaccineRecord>>(emptyList())
    val vaccines: StateFlow<List<VaccineRecord>> = _vaccines.asStateFlow()

    private val _branches = MutableStateFlow(
        listOf(
            Branch(
                id = "b-1",
                name = "VetCare Centro",
                address = "Av. Principal 100",
                latitude = 10.4806,
                longitude = -66.9036,
                phone = "+58 212 555 1010",
                services = listOf("Consultation", "Laboratory", "Dental")
            ),
            Branch(
                id = "b-2",
                name = "VetCare Norte",
                address = "Calle 5, Centro Comercial Norte",
                latitude = 10.5001,
                longitude = -66.9183,
                phone = "+58 212 555 2020",
                services = listOf("Grooming", "Surgery")
            ),
            Branch(
                id = "b-3",
                name = "VetCare Este",
                address = "Urbanización El Este",
                latitude = 10.4861,
                longitude = -66.8801,
                phone = "+58 212 555 3030",
                services = listOf("Laboratory", "Dental", "Vaccination")
            )
        )
    )
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _services = MutableStateFlow(
        listOf(
            ServiceItem(id = "s-1", name = "Cirugía general", category = MediaCategory.SURGERY, description = "Procedimientos quirúrgicos y seguimiento", price = 45.0),
            ServiceItem(id = "s-2", name = "Baño y grooming", category = MediaCategory.GROOMING, description = "Baño, corte y cepillado", price = 18.0),
            ServiceItem(id = "s-3", name = "Laboratorio clínico", category = MediaCategory.LABORATORY, description = "Perfil sanguíneo y pruebas rápidas", price = 30.0),
            ServiceItem(id = "s-4", name = "Odontología", category = MediaCategory.DENTAL, description = "Limpieza y evaluación dental", price = 25.0)
        )
    )
    val services: StateFlow<List<ServiceItem>> = _services.asStateFlow()

    private val _mediaCatalog = MutableStateFlow(
        listOf(
            MultimediaItem(id = "mm-1", category = MediaCategory.SURGERY, title = "Sutura básica", description = "Guía visual de suturas", mediaUrl = "https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b", thumbnailUrl = "https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b", mediaType = MediaType.IMAGE),
            MultimediaItem(id = "mm-2", category = MediaCategory.GROOMING, title = "Secado profesional", description = "Video de grooming", mediaUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", thumbnailUrl = "https://images.unsplash.com/photo-1517423440428-a5a00ad493e8", mediaType = MediaType.VIDEO),
            MultimediaItem(id = "mm-3", category = MediaCategory.LABORATORY, title = "Toma de muestras", description = "Procedimiento de laboratorio", mediaUrl = "https://images.unsplash.com/photo-1579165466741-7f35e4755660", thumbnailUrl = "https://images.unsplash.com/photo-1579165466741-7f35e4755660", mediaType = MediaType.IMAGE),
            MultimediaItem(id = "mm-4", category = MediaCategory.DENTAL, title = "Limpieza dental", description = "Checklist dental", mediaUrl = "https://images.unsplash.com/photo-1551601651-6e1a5cd8b5f9", thumbnailUrl = "https://images.unsplash.com/photo-1551601651-6e1a5cd8b5f9", mediaType = MediaType.IMAGE)
        )
    )
    val mediaCatalog: StateFlow<List<MultimediaItem>> = _mediaCatalog.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _offlineGuides = MutableStateFlow<List<OfflineGuide>>(emptyList())
    val offlineGuides: StateFlow<List<OfflineGuide>> = _offlineGuides.asStateFlow()

    private val _dashboard = MutableStateFlow(DashboardSummary())
    val dashboard: StateFlow<DashboardSummary> = _dashboard.asStateFlow()

    suspend fun bootstrap() {
        _offlineGuides.value = loadOfflineGuides()
        refreshDashboard()
        refreshReminders()
    }

    suspend fun login(email: String, password: String): Result<AppUser> = runCatching {
        val normalized = email.trim().lowercase()
        require(sampleCredentials[normalized] == password) { "Invalid credentials" }
        val user = _users.value.firstOrNull { it.email.equals(normalized, ignoreCase = true) }
            ?: error("User not found")
        setSession(user)
        user
    }

    fun setSession(user: AppUser?) {
        _session.value = if (user != null) {
            SessionState(isLoading = false, isAuthenticated = true, user = user, error = null)
        } else {
            SessionState()
        }
    }

    fun setOwners(items: List<Owner>) { _owners.value = items; scope.launch { refreshDashboard() } }
    fun setPets(items: List<Pet>) { _pets.value = items; scope.launch { refreshDashboard() } }
    fun setAppointments(items: List<Appointment>) { _appointments.value = items; scope.launch { refreshDashboard() } }
    fun setMedicalRecords(items: List<MedicalRecord>) { _medicalRecords.value = items; scope.launch { refreshDashboard() } }
    fun setVaccines(items: List<VaccineRecord>) { _vaccines.value = items; scope.launch { refreshDashboard() } }
    fun setNotifications(items: List<AppNotification>) { _notifications.value = items; scope.launch { refreshDashboard() } }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        require(_users.value.any { it.email.equals(email.trim(), ignoreCase = true) }) { "Email not found" }
        Unit
    }

    suspend fun logout() {
        _session.value = SessionState()
    }

    suspend fun saveOwner(owner: Owner): Result<Owner> = runCatching {
        _owners.update { items ->
            val cleaned = items.filterNot { it.id == owner.id }
            cleaned + owner
        }
        owner
    }

    suspend fun savePet(pet: Pet, photoBytes: ByteArray?): Result<Pet> = runCatching {
        val normalized = pet.copy(
            qrCode = pet.displayQrCode(),
            photoUrl = pet.photoUrl.ifBlank { pet.photoUrl }
        )
        _pets.update { items ->
            val cleaned = items.filterNot { it.id == normalized.id }
            cleaned + normalized
        }
        refreshDashboard()
        normalized
    }

    suspend fun findPetByQrCode(qrCode: String): Pet? =
        _pets.value.firstOrNull { it.displayQrCode().equals(qrCode.trim(), ignoreCase = true) || it.id.equals(qrCode.trim(), ignoreCase = true) }

    suspend fun saveAppointment(appointment: Appointment): Result<Appointment> = runCatching {
        _appointments.update { items ->
            val cleaned = items.filterNot { it.id == appointment.id }
            cleaned + appointment
        }
        refreshDashboard()
        appointment
    }

    suspend fun cancelAppointment(appointmentId: String): Result<Unit> = runCatching {
        _appointments.update { items ->
            items.map { if (it.id == appointmentId) it.copy(status = AppointmentStatus.CANCELLED) else it }
        }
        refreshDashboard()
        Unit
    }

    suspend fun saveMedicalRecord(record: MedicalRecord): Result<MedicalRecord> = runCatching {
        _medicalRecords.update { items ->
            val cleaned = items.filterNot { it.id == record.id }
            cleaned + record
        }
        _pets.update { items ->
            items.map { pet -> if (pet.id == record.petId) pet.copy(lastConsultationDate = record.consultationDate.toLocalDate(), serviceType = record.diagnosis) else pet }
        }
        refreshDashboard()
        record
    }

    suspend fun saveVaccineRecord(record: VaccineRecord): Result<VaccineRecord> = runCatching {
        _vaccines.update { items ->
            val cleaned = items.filterNot { it.id == record.id }
            cleaned + record
        }
        _pets.update { items ->
            items.map { pet -> if (pet.id == record.petId) pet.copy(vaccinated = record.status() == VaccinationStatus.UP_TO_DATE) else pet }
        }
        refreshDashboard()
        record
    }

    suspend fun markNotificationRead(notificationId: String) {
        _notifications.update { items -> items.map { if (it.id == notificationId) it.copy(isRead = true) else it } }
        refreshDashboard()
    }

    suspend fun refreshReminders() {
        val now = LocalDateTime.now()
        val reminderNotifications = mutableListOf<AppNotification>()

        _appointments.value.filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING }.forEach { appointment ->
            val hours = java.time.Duration.between(now, appointment.dateTime).toHours()
            if (hours in 23..25 && !appointment.reminder24hSent) {
                reminderNotifications += AppNotification(
                    title = "24 hour appointment reminder",
                    message = "${appointment.reason} is due in 24 hours.",
                    type = NotificationType.APPOINTMENT_REMINDER,
                    actionable = true,
                    targetRoute = "appointments"
                )
            }
            if (hours in 1..2 && !appointment.reminder2hSent) {
                reminderNotifications += AppNotification(
                    title = "2 hour appointment reminder",
                    message = "${appointment.reason} is due in 2 hours.",
                    type = NotificationType.APPOINTMENT_REMINDER,
                    actionable = true,
                    targetRoute = "appointments"
                )
            }
        }

        _vaccines.value.forEach { vaccine ->
            if (vaccine.nextDoseDate.minusDays(7).isBefore(LocalDate.now()) || vaccine.nextDoseDate.minusDays(7).isEqual(LocalDate.now())) {
                reminderNotifications += AppNotification(
                    title = "Vaccine reminder",
                    message = "${vaccine.vaccineName} is due soon.",
                    type = NotificationType.VACCINE_REMINDER,
                    actionable = true,
                    targetRoute = "vaccinations"
                )
            }
        }

        _notifications.update { current ->
            val merged = current.filterNot { it.type == NotificationType.APPOINTMENT_REMINDER || it.type == NotificationType.VACCINE_REMINDER }
            merged + reminderNotifications
        }
        refreshDashboard()
    }

    fun searchPets(query: String, vaccinationOnly: Boolean, serviceType: String, role: UserRole?): List<Pet> {
        val normalized = query.trim().lowercase()
        return _pets.value.filter { pet ->
            val owner = ownerById(pet.ownerId)
            val matchesQuery = normalized.isBlank() || pet.name.lowercase().contains(normalized) || pet.species.lowercase().contains(normalized) || pet.displayQrCode().lowercase().contains(normalized) || (owner?.fullName?.lowercase()?.contains(normalized) == true)
            val matchesVaccination = !vaccinationOnly || pet.vaccinated
            val matchesService = serviceType.isBlank() || pet.serviceType.equals(serviceType, ignoreCase = true)
            matchesQuery && matchesVaccination && matchesService
        }
    }

    fun appointmentsForPet(petId: String): List<Appointment> = _appointments.value.filter { it.petId == petId }.sortedByDescending { it.dateTime }

    fun medicalRecordsForPet(petId: String): List<MedicalRecord> = _medicalRecords.value.filter { it.petId == petId }.sortedByDescending { it.consultationDate }

    fun vaccinesForPet(petId: String): List<VaccineRecord> = _vaccines.value.filter { it.petId == petId }.sortedByDescending { it.applicationDate }

    fun branchById(branchId: String): Branch? = _branches.value.firstOrNull { it.id == branchId }

    fun ownerById(ownerId: String): Owner? = _owners.value.firstOrNull { it.id == ownerId }

    fun petById(petId: String): Pet? = _pets.value.firstOrNull { it.id == petId }

    fun userById(userId: String): AppUser? = _users.value.firstOrNull { it.id == userId }

    fun mediaByCategory(category: MediaCategory?): List<MultimediaItem> =
        if (category == null) _mediaCatalog.value else _mediaCatalog.value.filter { it.category == category }

    fun guidesByQuery(query: String): List<OfflineGuide> {
        val normalized = query.trim().lowercase()
        return _offlineGuides.value.filter { guide ->
            normalized.isBlank() || guide.title.lowercase().contains(normalized) || guide.category.lowercase().contains(normalized) || guide.content.lowercase().contains(normalized)
        }
    }

    fun canAccessRoute(role: UserRole?, route: String): Boolean {
        if (route == "login" || route == "dashboard") return true
        val userRole = role ?: return false
        return when (route) {
            "owner_registration" -> userRole == UserRole.VETERINARIO || userRole == UserRole.RECEPCIONISTA
            "pet_registration" -> userRole == UserRole.VETERINARIO || userRole == UserRole.RECEPCIONISTA
            "scanner" -> userRole == UserRole.VETERINARIO
            "appointments" -> true // All can see their context
            "medical_history" -> true
            "branch_map" -> true
            "vaccinations" -> true
            "multimedia" -> true
            "offline" -> true
            else -> true
        }
    }

    private suspend fun loadOfflineGuides(): List<OfflineGuide> {
        return runCatching {
            val json = appContext.assets.open("offline_guides.json").bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        OfflineGuide(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            title = item.getString("title"),
                            category = item.getString("category"),
                            content = item.getString("content"),
                            assetPath = item.optString("assetPath", "")
                        )
                    )
                }
            }
        }.getOrElse {
            listOf(
                OfflineGuide(id = "g-1", title = "Triage Basics", category = "Clinical", content = "Stabilize, observe, and escalate if needed.", assetPath = ""),
                OfflineGuide(id = "g-2", title = "Vaccination Flow", category = "Preventive Care", content = "Verify schedule and confirm next dose date.", assetPath = "")
            )
        }
    }

    private suspend fun refreshDashboard() {
        _dashboard.value = DashboardSummary(
            totalPets = _pets.value.size,
            totalAppointments = _appointments.value.size,
            pendingAppointments = _appointments.value.count { it.status == AppointmentStatus.PENDING },
            totalVaccines = _vaccines.value.size,
            overdueVaccines = _vaccines.value.count { it.status() == VaccinationStatus.OVERDUE },
            totalBranches = _branches.value.size,
            totalMedia = _mediaCatalog.value.size,
            totalGuides = _offlineGuides.value.size,
            activeNotifications = _notifications.value.count { !it.isRead }
        )
    }
}
