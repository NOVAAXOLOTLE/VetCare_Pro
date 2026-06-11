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
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    private val _owners = MutableStateFlow(
        listOf(
            Owner(id = "o-1", fullName = "Javier Ruiz", email = "owner@vetcare.pro", phone = "+58 412 555 1212", address = "Av. Central 123", documentId = "V-12345678"),
            Owner(id = "o-2", fullName = "María López", email = "maria@correo.com", phone = "+58 414 555 9988", address = "Calle 9 #45", documentId = "V-87654321"),
            Owner(id = "o-3", fullName = "Pedro García", email = "pedro@correo.com", phone = "+58 424 555 7777", address = "Urbanización Las Palmas", documentId = "V-99887766")
        )
    )
    val owners: StateFlow<List<Owner>> = _owners.asStateFlow()

    private val _pets = MutableStateFlow(
        listOf(
            Pet(
                id = "p-1",
                name = "Luna",
                species = "Canina",
                breed = "Labrador",
                birthDate = LocalDate.now().minusYears(4),
                weight = 24.5,
                coatColor = "Miel",
                microchipNumber = "MC-1001",
                photoUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b",
                ownerId = "o-1",
                ownerName = "Javier Ruiz",
                qrCode = "PET-p-1",
                vaccinated = true,
                lastConsultationDate = LocalDate.now().minusDays(12),
                serviceType = "Laboratory"
            ),
            Pet(
                id = "p-2",
                name = "Milo",
                species = "Felina",
                breed = "Maine Coon",
                birthDate = LocalDate.now().minusYears(2),
                weight = 6.2,
                coatColor = "Gris",
                microchipNumber = "MC-1002",
                photoUrl = "https://images.unsplash.com/photo-1519052537078-e6302a4968d4",
                ownerId = "o-2",
                ownerName = "María López",
                qrCode = "PET-p-2",
                vaccinated = false,
                lastConsultationDate = LocalDate.now().minusMonths(2),
                serviceType = "Dental"
            ),
            Pet(
                id = "p-3",
                name = "Kira",
                species = "Canina",
                breed = "Poodle",
                birthDate = LocalDate.now().minusYears(6),
                weight = 9.1,
                coatColor = "Blanco",
                microchipNumber = "MC-1003",
                photoUrl = "https://images.unsplash.com/photo-1601758123927-19890f6a0de3",
                ownerId = "o-3",
                ownerName = "Pedro García",
                qrCode = "PET-p-3",
                vaccinated = true,
                lastConsultationDate = LocalDate.now().minusDays(4),
                serviceType = "Surgery"
            )
        )
    )
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _appointments = MutableStateFlow(
        listOf(
            Appointment(
                id = "a-1",
                petId = "p-1",
                ownerId = "o-1",
                branchId = "b-1",
                dateTime = LocalDateTime.now().plusHours(26),
                status = AppointmentStatus.CONFIRMED,
                reason = "Control general",
                notes = "Llegar 10 min antes",
                serviceType = "Consultation",
                reminder24hSent = false,
                reminder2hSent = false
            ),
            Appointment(
                id = "a-2",
                petId = "p-2",
                ownerId = "o-2",
                branchId = "b-2",
                dateTime = LocalDateTime.now().plusHours(3),
                status = AppointmentStatus.PENDING,
                reason = "Vacunación",
                notes = "Traer cartilla",
                serviceType = "Vaccination",
                reminder24hSent = false,
                reminder2hSent = false
            ),
            Appointment(
                id = "a-3",
                petId = "p-3",
                ownerId = "o-3",
                branchId = "b-1",
                dateTime = LocalDateTime.now().minusHours(2),
                status = AppointmentStatus.COMPLETED,
                reason = "Cirugía menor",
                notes = "Recuperación favorable",
                serviceType = "Surgery",
                reminder24hSent = true,
                reminder2hSent = true
            )
        )
    )
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _medicalRecords = MutableStateFlow(
        listOf(
            MedicalRecord(
                id = "m-1",
                petId = "p-1",
                veterinarianId = "u-vet",
                veterinarianName = "Dra. Ana Torres",
                diagnosis = "Otitis externa",
                treatment = "Limpieza y gotas antibióticas",
                notes = "Control en 7 días",
                temperature = 38.6,
                weight = 24.4,
                images = listOf("https://images.unsplash.com/photo-1517841905240-472988babdf9"),
                consultationDate = LocalDateTime.now().minusDays(12),
                prescriptionApproved = true,
                prescriptionSent = true
            ),
            MedicalRecord(
                id = "m-2",
                petId = "p-2",
                veterinarianId = "u-vet",
                veterinarianName = "Dra. Ana Torres",
                diagnosis = "Profilaxis dental",
                treatment = "Limpieza dental y pulido",
                notes = "Evitar alimento duro 48h",
                temperature = 38.3,
                weight = 6.1,
                images = listOf(),
                consultationDate = LocalDateTime.now().minusMonths(2),
                prescriptionApproved = false,
                prescriptionSent = false
            ),
            MedicalRecord(
                id = "m-3",
                petId = "p-3",
                veterinarianId = "u-vet",
                veterinarianName = "Dra. Ana Torres",
                diagnosis = "Recuperación post quirúrgica",
                treatment = "Antiinflamatorio y reposo",
                notes = "Sin complicaciones",
                temperature = 38.2,
                weight = 9.0,
                images = listOf(),
                consultationDate = LocalDateTime.now().minusDays(4),
                prescriptionApproved = true,
                prescriptionSent = false
            )
        )
    )
    val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _vaccines = MutableStateFlow(
        listOf(
            VaccineRecord(
                id = "v-1",
                petId = "p-1",
                vaccineName = "Rabia",
                laboratory = "VetLab",
                lot = "RB-221",
                applicationDate = LocalDate.now().minusMonths(11),
                nextDoseDate = LocalDate.now().plusDays(10),
                notes = "Refuerzo anual"
            ),
            VaccineRecord(
                id = "v-2",
                petId = "p-2",
                vaccineName = "Trivalente",
                laboratory = "BioPet",
                lot = "TV-442",
                applicationDate = LocalDate.now().minusMonths(14),
                nextDoseDate = LocalDate.now().minusDays(3),
                notes = "Refuerzo vencido"
            ),
            VaccineRecord(
                id = "v-3",
                petId = "p-3",
                vaccineName = "Leptospirosis",
                laboratory = "VetLab",
                lot = "LP-780",
                applicationDate = LocalDate.now().minusMonths(7),
                nextDoseDate = LocalDate.now().plusMonths(4),
                notes = "Completar esquema"
            )
        )
    )
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

    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(id = "n-1", title = "Appointment reminder", message = "Luna tiene cita en 24 horas", type = NotificationType.APPOINTMENT_REMINDER, actionable = true, targetRoute = "appointments"),
            AppNotification(id = "n-2", title = "Vaccine reminder", message = "Milo vence en 7 días", type = NotificationType.VACCINE_REMINDER, actionable = true, targetRoute = "vaccinations")
        )
    )
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
        _session.value = SessionState(isLoading = false, isAuthenticated = true, user = user, error = null)
        user
    }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        require(_users.value.any { it.email.equals(email.trim(), ignoreCase = true) }) { "Email not found" }
        Unit
    }

    suspend fun logout() {
        _session.value = SessionState()
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
        if (route == "login") return true
        return role != null
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

