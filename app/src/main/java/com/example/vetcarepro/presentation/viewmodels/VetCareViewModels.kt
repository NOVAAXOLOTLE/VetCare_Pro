package com.example.vetcarepro.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.AppUser
import com.example.vetcarepro.domain.model.Branch
import com.example.vetcarepro.domain.model.DashboardSummary
import com.example.vetcarepro.domain.model.MapMode
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.OfflineGuide
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.SessionState
import com.example.vetcarepro.domain.model.UserRole
import com.example.vetcarepro.domain.model.VaccineRecord
import com.example.vetcarepro.domain.repository.VetCareRepository
import com.example.vetcarepro.domain.usecase.BootstrapUseCase
import com.example.vetcarepro.domain.usecase.CancelAppointmentUseCase
import com.example.vetcarepro.domain.usecase.ForgotPasswordUseCase
import com.example.vetcarepro.domain.usecase.LoginUseCase
import com.example.vetcarepro.domain.usecase.LogoutUseCase
import com.example.vetcarepro.domain.usecase.RefreshRemindersUseCase
import com.example.vetcarepro.domain.usecase.SaveAppointmentUseCase
import com.example.vetcarepro.domain.usecase.SaveMedicalRecordUseCase
import com.example.vetcarepro.domain.usecase.SavePetUseCase
import com.example.vetcarepro.domain.usecase.SaveVaccineRecordUseCase
import com.example.vetcarepro.domain.usecase.SearchPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val forgotEmail: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
    repository: VetCareRepository,
    bootstrapUseCase: BootstrapUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state
    val session: StateFlow<SessionState> = repository.session

    init {
        viewModelScope.launch { bootstrapUseCase() }
    }

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null, resetMessage = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null, resetMessage = null) }
    fun onForgotEmailChange(value: String) { _state.value = _state.value.copy(forgotEmail = value, error = null, resetMessage = null) }

    fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            loginUseCase(_state.value.email, _state.value.password)
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Login failed") }
                .onSuccess { _state.value = _state.value.copy(isLoading = false, password = "", error = null) }
        }
    }

    fun forgotPassword() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resetMessage = null)
            forgotPasswordUseCase(_state.value.forgotEmail.ifBlank { _state.value.email })
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Reset failed") }
                .onSuccess { _state.value = _state.value.copy(isLoading = false, resetMessage = "Reset email sent") }
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}

@HiltViewModel
class VetCareViewModel @Inject constructor(
    private val repository: VetCareRepository,
    private val bootstrapUseCase: BootstrapUseCase,
    private val savePetUseCase: SavePetUseCase,
    private val searchPetsUseCase: SearchPetsUseCase,
    private val saveAppointmentUseCase: SaveAppointmentUseCase,
    private val cancelAppointmentUseCase: CancelAppointmentUseCase,
    private val saveMedicalRecordUseCase: SaveMedicalRecordUseCase,
    private val saveVaccineRecordUseCase: SaveVaccineRecordUseCase,
    private val refreshRemindersUseCase: RefreshRemindersUseCase
) : ViewModel() {
    val session: StateFlow<SessionState> = repository.session
    val dashboard: StateFlow<DashboardSummary> = repository.dashboard
    val users: StateFlow<List<AppUser>> = repository.users
    val owners: StateFlow<List<Owner>> = repository.owners
    val pets: StateFlow<List<Pet>> = repository.pets
    val appointments: StateFlow<List<Appointment>> = repository.appointments
    val medicalRecords: StateFlow<List<MedicalRecord>> = repository.medicalRecords
    val vaccines: StateFlow<List<VaccineRecord>> = repository.vaccines
    val branches: StateFlow<List<Branch>> = repository.branches
    val services = repository.services
    val notifications = repository.notifications
    val mediaCatalog: StateFlow<List<MultimediaItem>> = repository.mediaCatalog
    val offlineGuides: StateFlow<List<OfflineGuide>> = repository.offlineGuides

    private val searchQuery = MutableStateFlow("")
    private val vaccinationOnly = MutableStateFlow(false)
    private val serviceType = MutableStateFlow("")
    private val selectedMediaCategory = MutableStateFlow<MediaCategory?>(null)
    private val selectedMapMode = MutableStateFlow(MapMode.NORMAL)
    private val selectedPetId = MutableStateFlow<String?>(null)

    val filteredPets: StateFlow<List<Pet>> = combine(searchQuery, vaccinationOnly, serviceType, pets) { query, onlyVaccinated, type, currentPets ->
        searchPetsUseCase(query, onlyVaccinated, type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredMedia: StateFlow<List<MultimediaItem>> = combine(selectedMediaCategory, mediaCatalog) { category, items ->
        category?.let { items.filter { item -> item.category == category } } ?: items
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedPet: StateFlow<Pet?> = combine(selectedPetId, pets) { petId, items ->
        petId?.let { id -> items.firstOrNull { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val mapMode: StateFlow<MapMode> = selectedMapMode

    init {
        viewModelScope.launch {
            bootstrapUseCase()
            refreshRemindersUseCase()
        }
    }

    fun setSearchQuery(value: String) { searchQuery.value = value }
    fun setVaccinationOnly(value: Boolean) { vaccinationOnly.value = value }
    fun setServiceType(value: String) { serviceType.value = value }
    fun setMediaCategory(value: MediaCategory?) { selectedMediaCategory.value = value }
    fun setMapMode(value: MapMode) { selectedMapMode.value = value }
    fun selectPet(petId: String?) { selectedPetId.value = petId }

    fun savePet(pet: Pet, photoBytes: ByteArray? = null) {
        viewModelScope.launch { savePetUseCase(pet, photoBytes) }
    }

    fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch { saveAppointmentUseCase(appointment) }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch { cancelAppointmentUseCase(appointmentId) }
    }

    fun saveMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch { saveMedicalRecordUseCase(record) }
    }

    fun saveVaccineRecord(record: VaccineRecord) {
        viewModelScope.launch { saveVaccineRecordUseCase(record) }
    }

    fun findPetByQrCode(qrCode: String): Pet? =
        pets.value.firstOrNull { it.qrCode.equals(qrCode.trim(), ignoreCase = true) || it.id.equals(qrCode.trim(), ignoreCase = true) }

    fun appointmentsForPet(petId: String): List<Appointment> = repository.appointmentsForPet(petId)
    fun medicalRecordsForPet(petId: String): List<MedicalRecord> = repository.medicalRecordsForPet(petId)
    fun vaccinesForPet(petId: String): List<VaccineRecord> = repository.vaccinesForPet(petId)
    fun branchById(branchId: String): Branch? = repository.branchById(branchId)
    fun ownerById(ownerId: String): Owner? = repository.ownerById(ownerId)
    fun petById(petId: String): Pet? = repository.petById(petId)
    fun userById(userId: String): AppUser? = repository.userById(userId)
    fun mediaByCategory(category: MediaCategory? = selectedMediaCategory.value): List<MultimediaItem> = repository.mediaByCategory(category)
    fun guidesByQuery(query: String): List<OfflineGuide> = repository.guidesByQuery(query)
    fun canAccessRoute(route: String): Boolean = repository.canAccessRoute(session.value.user?.role, route)
    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch { repository.markNotificationRead(notificationId) }
    }
}

