package com.example.vetcarepro.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class UserRole(val label: String) {
    VETERINARIO("Veterinario"),
    RECEPCIONISTA("Recepcionista"),
    PROPIETARIO("Propietario")
}

enum class AppointmentStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed")
}

enum class VaccinationStatus(val label: String) {
    UP_TO_DATE("Green"),
    EXPIRING("Amber"),
    OVERDUE("Red")
}

enum class MediaCategory(val label: String) {
    SURGERY("Surgery"),
    GROOMING("Grooming"),
    LABORATORY("Laboratory"),
    DENTAL("Dental")
}

enum class MapMode(val label: String) {
    NORMAL("Normal"),
    SATELLITE("Satellite"),
    HYBRID("Hybrid")
}

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class NotificationType {
    APPOINTMENT_REMINDER,
    VACCINE_REMINDER,
    APPROVAL,
    CONSULTATION,
    PRESCRIPTION
}

data class AppUser(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val role: UserRole,
    val branchId: String = "",
    val photoUrl: String = "",
    val active: Boolean = true
)

data class Owner(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String = "",
    val documentId: String = ""
)

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: LocalDate,
    val weight: Double,
    val coatColor: String,
    val microchipNumber: String,
    val photoUrl: String = "",
    val ownerId: String,
    val ownerName: String = "",
    val qrCode: String = "",
    val vaccinated: Boolean = true,
    val lastConsultationDate: LocalDate? = null,
    val serviceType: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val ownerId: String,
    val branchId: String,
    val dateTime: LocalDateTime,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val reason: String,
    val notes: String = "",
    val serviceType: String = "",
    val reminder24hSent: Boolean = false,
    val reminder2hSent: Boolean = false
)

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val veterinarianId: String,
    val veterinarianName: String,
    val diagnosis: String,
    val treatment: String,
    val notes: String,
    val temperature: Double,
    val weight: Double,
    val images: List<String> = emptyList(),
    val consultationDate: LocalDateTime = LocalDateTime.now(),
    val prescriptionApproved: Boolean = false,
    val prescriptionSent: Boolean = false
)

data class VaccineRecord(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val vaccineName: String,
    val laboratory: String,
    val lot: String,
    val applicationDate: LocalDate,
    val nextDoseDate: LocalDate,
    val notes: String = ""
)

data class Branch(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val services: List<String> = emptyList()
)

data class ServiceItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: MediaCategory,
    val description: String,
    val price: Double = 0.0
)

data class MultimediaItem(
    val id: String = UUID.randomUUID().toString(),
    val category: MediaCategory,
    val title: String,
    val description: String,
    val mediaUrl: String,
    val thumbnailUrl: String = "",
    val mediaType: MediaType = MediaType.IMAGE
)

data class OfflineGuide(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val content: String,
    val assetPath: String = ""
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val actionable: Boolean = false,
    val targetRoute: String = "",
    val isRead: Boolean = false
)

data class DashboardSummary(
    val totalPets: Int = 0,
    val totalAppointments: Int = 0,
    val pendingAppointments: Int = 0,
    val totalVaccines: Int = 0,
    val overdueVaccines: Int = 0,
    val totalBranches: Int = 0,
    val totalMedia: Int = 0,
    val totalGuides: Int = 0,
    val activeNotifications: Int = 0
)

data class SessionState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: AppUser? = null,
    val error: String? = null
)

fun Pet.displayQrCode(): String = qrCode.ifBlank { "PET-$id" }

fun VaccineRecord.status(today: LocalDate = LocalDate.now()): VaccinationStatus =
    when {
        nextDoseDate.isBefore(today) -> VaccinationStatus.OVERDUE
        !nextDoseDate.isAfter(today.plusDays(7)) -> VaccinationStatus.EXPIRING
        else -> VaccinationStatus.UP_TO_DATE
    }

fun Appointment.formattedDateTime(): String = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

fun MedicalRecord.formattedDateTime(): String = consultationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

