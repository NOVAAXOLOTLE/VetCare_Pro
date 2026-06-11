package com.example.vetcarepro.data.remote

import com.example.vetcarepro.domain.model.AppNotification
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.AppUser
import com.example.vetcarepro.domain.model.Branch
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MediaType
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.NotificationType
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.ServiceItem
import com.example.vetcarepro.domain.model.UserRole
import com.example.vetcarepro.domain.model.VaccineRecord
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun AppUser.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "email" to email,
    "fullName" to fullName,
    "role" to role.name,
    "branchId" to branchId,
    "photoUrl" to photoUrl,
    "active" to active
)

fun Owner.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "fullName" to fullName,
    "email" to email,
    "phone" to phone,
    "address" to address,
    "documentId" to documentId
)

fun Pet.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "species" to species,
    "breed" to breed,
    "birthDate" to birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "weight" to weight,
    "coatColor" to coatColor,
    "microchipNumber" to microchipNumber,
    "photoUrl" to photoUrl,
    "ownerId" to ownerId,
    "ownerName" to ownerName,
    "qrCode" to qrCode,
    "vaccinated" to vaccinated,
    "lastConsultationDate" to lastConsultationDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    "serviceType" to serviceType,
    "createdAt" to createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

fun Appointment.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "petId" to petId,
    "ownerId" to ownerId,
    "branchId" to branchId,
    "dateTime" to dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "status" to status.name,
    "reason" to reason,
    "notes" to notes,
    "serviceType" to serviceType,
    "reminder24hSent" to reminder24hSent,
    "reminder2hSent" to reminder2hSent
)

fun MedicalRecord.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "petId" to petId,
    "veterinarianId" to veterinarianId,
    "veterinarianName" to veterinarianName,
    "diagnosis" to diagnosis,
    "treatment" to treatment,
    "notes" to notes,
    "temperature" to temperature,
    "weight" to weight,
    "images" to images,
    "consultationDate" to consultationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "prescriptionApproved" to prescriptionApproved,
    "prescriptionSent" to prescriptionSent
)

fun VaccineRecord.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "petId" to petId,
    "vaccineName" to vaccineName,
    "laboratory" to laboratory,
    "lot" to lot,
    "applicationDate" to applicationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "nextDoseDate" to nextDoseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "notes" to notes
)

fun Branch.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "address" to address,
    "latitude" to latitude,
    "longitude" to longitude,
    "phone" to phone,
    "services" to services
)

fun ServiceItem.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "category" to category.name,
    "description" to description,
    "price" to price
)

fun MultimediaItem.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "category" to category.name,
    "title" to title,
    "description" to description,
    "mediaUrl" to mediaUrl,
    "thumbnailUrl" to thumbnailUrl,
    "mediaType" to mediaType.name
)

fun AppNotification.toFirestore(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "message" to message,
    "type" to type.name,
    "createdAt" to createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    "actionable" to actionable,
    "targetRoute" to targetRoute,
    "isRead" to isRead
)

fun Map<String, Any?>.toAppUser(): AppUser = AppUser(
    id = this["id"] as? String ?: "",
    email = this["email"] as? String ?: "",
    fullName = this["fullName"] as? String ?: "",
    role = UserRole.valueOf(this["role"] as? String ?: UserRole.PROPIETARIO.name),
    branchId = this["branchId"] as? String ?: "",
    photoUrl = this["photoUrl"] as? String ?: "",
    active = this["active"] as? Boolean ?: true
)

fun Map<String, Any?>.toPet(): Pet = Pet(
    id = this["id"] as? String ?: "",
    name = this["name"] as? String ?: "",
    species = this["species"] as? String ?: "",
    breed = this["breed"] as? String ?: "",
    birthDate = epochToLocalDate(this["birthDate"]),
    weight = (this["weight"] as? Number)?.toDouble() ?: 0.0,
    coatColor = this["coatColor"] as? String ?: "",
    microchipNumber = this["microchipNumber"] as? String ?: "",
    photoUrl = this["photoUrl"] as? String ?: "",
    ownerId = this["ownerId"] as? String ?: "",
    ownerName = this["ownerName"] as? String ?: "",
    qrCode = this["qrCode"] as? String ?: "",
    vaccinated = this["vaccinated"] as? Boolean ?: true,
    lastConsultationDate = epochToLocalDate(this["lastConsultationDate"]),
    serviceType = this["serviceType"] as? String ?: ""
)

fun Map<String, Any?>.toAppointment(): Appointment = Appointment(
    id = this["id"] as? String ?: "",
    petId = this["petId"] as? String ?: "",
    ownerId = this["ownerId"] as? String ?: "",
    branchId = this["branchId"] as? String ?: "",
    dateTime = epochToLocalDateTime(this["dateTime"]),
    status = AppointmentStatus.valueOf(this["status"] as? String ?: AppointmentStatus.PENDING.name),
    reason = this["reason"] as? String ?: "",
    notes = this["notes"] as? String ?: "",
    serviceType = this["serviceType"] as? String ?: "",
    reminder24hSent = this["reminder24hSent"] as? Boolean ?: false,
    reminder2hSent = this["reminder2hSent"] as? Boolean ?: false
)

fun Map<String, Any?>.toMedicalRecord(): MedicalRecord = MedicalRecord(
    id = this["id"] as? String ?: "",
    petId = this["petId"] as? String ?: "",
    veterinarianId = this["veterinarianId"] as? String ?: "",
    veterinarianName = this["veterinarianName"] as? String ?: "",
    diagnosis = this["diagnosis"] as? String ?: "",
    treatment = this["treatment"] as? String ?: "",
    notes = this["notes"] as? String ?: "",
    temperature = (this["temperature"] as? Number)?.toDouble() ?: 0.0,
    weight = (this["weight"] as? Number)?.toDouble() ?: 0.0,
    images = this["images"] as? List<String> ?: emptyList(),
    consultationDate = epochToLocalDateTime(this["consultationDate"]),
    prescriptionApproved = this["prescriptionApproved"] as? Boolean ?: false,
    prescriptionSent = this["prescriptionSent"] as? Boolean ?: false
)

fun Map<String, Any?>.toVaccineRecord(): VaccineRecord = VaccineRecord(
    id = this["id"] as? String ?: "",
    petId = this["petId"] as? String ?: "",
    vaccineName = this["vaccineName"] as? String ?: "",
    laboratory = this["laboratory"] as? String ?: "",
    lot = this["lot"] as? String ?: "",
    applicationDate = epochToLocalDate(this["applicationDate"]),
    nextDoseDate = epochToLocalDate(this["nextDoseDate"]),
    notes = this["notes"] as? String ?: ""
)

fun Map<String, Any?>.toBranch(): Branch = Branch(
    id = this["id"] as? String ?: "",
    name = this["name"] as? String ?: "",
    address = this["address"] as? String ?: "",
    latitude = (this["latitude"] as? Number)?.toDouble() ?: 0.0,
    longitude = (this["longitude"] as? Number)?.toDouble() ?: 0.0,
    phone = this["phone"] as? String ?: "",
    services = this["services"] as? List<String> ?: emptyList()
)

fun Map<String, Any?>.toServiceItem(): ServiceItem = ServiceItem(
    id = this["id"] as? String ?: "",
    name = this["name"] as? String ?: "",
    category = MediaCategory.valueOf(this["category"] as? String ?: MediaCategory.SURGERY.name),
    description = this["description"] as? String ?: "",
    price = (this["price"] as? Number)?.toDouble() ?: 0.0
)

fun Map<String, Any?>.toMultimediaItem(): MultimediaItem = MultimediaItem(
    id = this["id"] as? String ?: "",
    category = MediaCategory.valueOf(this["category"] as? String ?: MediaCategory.SURGERY.name),
    title = this["title"] as? String ?: "",
    description = this["description"] as? String ?: "",
    mediaUrl = this["mediaUrl"] as? String ?: "",
    thumbnailUrl = this["thumbnailUrl"] as? String ?: "",
    mediaType = MediaType.valueOf(this["mediaType"] as? String ?: MediaType.IMAGE.name)
)

fun Map<String, Any?>.toAppNotification(): AppNotification = AppNotification(
    id = this["id"] as? String ?: "",
    title = this["title"] as? String ?: "",
    message = this["message"] as? String ?: "",
    type = NotificationType.valueOf(this["type"] as? String ?: NotificationType.APPROVAL.name),
    createdAt = epochToLocalDateTime(this["createdAt"]),
    actionable = this["actionable"] as? Boolean ?: false,
    targetRoute = this["targetRoute"] as? String ?: "",
    isRead = this["isRead"] as? Boolean ?: false
)

private fun epochToLocalDate(value: Any?): LocalDate = when (value) {
    is Number -> Timestamp(value.toLong(), 0).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    is Timestamp -> value.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    else -> LocalDate.now()
}

private fun epochToLocalDateTime(value: Any?): LocalDateTime = when (value) {
    is Number -> Timestamp(value.toLong(), 0).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    is Timestamp -> value.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    else -> LocalDateTime.now()
}

