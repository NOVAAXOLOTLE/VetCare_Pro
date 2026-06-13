package com.example.vetcarepro.domain.usecase

import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.VaccineRecord
import com.example.vetcarepro.domain.repository.VetCareRepository
import javax.inject.Inject

class BootstrapUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke() = repository.bootstrap()
}

class LoginUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}

class LoginWithGoogleUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(idToken: String) = repository.loginWithGoogle(idToken)
}

class ForgotPasswordUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(email: String) = repository.forgotPassword(email)
}

class LogoutUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke() = repository.logout()
}

class SaveOwnerUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(owner: Owner) = repository.saveOwner(owner)
}

class SavePetUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(pet: Pet, photoBytes: ByteArray? = null) = repository.savePet(pet, photoBytes)
}

class SearchPetsUseCase @Inject constructor(private val repository: VetCareRepository) {
    operator fun invoke(query: String, vaccinationOnly: Boolean = false, serviceType: String = "") = repository.searchPets(query, vaccinationOnly, serviceType)
}

class SaveAppointmentUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(appointment: Appointment) = repository.saveAppointment(appointment)
}

class CancelAppointmentUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(appointmentId: String) = repository.cancelAppointment(appointmentId)
}

class SaveMedicalRecordUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(record: MedicalRecord) = repository.saveMedicalRecord(record)
}

class SaveVaccineRecordUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke(record: VaccineRecord) = repository.saveVaccineRecord(record)
}

class RefreshRemindersUseCase @Inject constructor(private val repository: VetCareRepository) {
    suspend operator fun invoke() = repository.refreshReminders()
}

