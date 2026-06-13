package com.example.vetcarepro.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
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
import com.example.vetcarepro.domain.usecase.SaveOwnerUseCase
import com.example.vetcarepro.domain.usecase.CancelAppointmentUseCase
import com.example.vetcarepro.domain.usecase.ForgotPasswordUseCase
import com.example.vetcarepro.domain.usecase.LoginUseCase
import com.example.vetcarepro.domain.usecase.LoginWithGoogleUseCase
import com.example.vetcarepro.domain.usecase.LogoutUseCase
import com.example.vetcarepro.domain.usecase.RefreshRemindersUseCase
import com.example.vetcarepro.domain.usecase.SaveAppointmentUseCase
import com.example.vetcarepro.domain.usecase.SaveMedicalRecordUseCase
import com.example.vetcarepro.domain.usecase.SavePetUseCase
import com.example.vetcarepro.domain.usecase.SaveVaccineRecordUseCase
import com.example.vetcarepro.domain.usecase.SearchPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VetCareUiState<out T> {
    object Loading : VetCareUiState<Nothing>
    data class Success<T>(val data: T) : VetCareUiState<T>
    data class Error(val message: String) : VetCareUiState<Nothing>
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}

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
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
    repository: VetCareRepository,
    bootstrapUseCase: BootstrapUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state
    val session: StateFlow<SessionState> = repository.session

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch { bootstrapUseCase() }
    }

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null, resetMessage = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null, resetMessage = null) }
    fun onForgotEmailChange(value: String) { _state.value = _state.value.copy(forgotEmail = value, error = null, resetMessage = null) }

    fun login() {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Please fill all fields")) }
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            loginUseCase(_state.value.email, _state.value.password)
                .onFailure { 
                    _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Login failed")
                    _eventFlow.emit(UiEvent.ShowToast(it.message ?: "Login failed"))
                }
                .onSuccess { 
                    _state.value = _state.value.copy(isLoading = false, password = "", error = null)
                    _eventFlow.emit(UiEvent.ShowToast("Welcome back!"))
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            loginWithGoogleUseCase(idToken)
                .onFailure { 
                    _state.value = _state.value.copy(isLoading = false, error = it.message ?: "Google Sign-In failed")
                    _eventFlow.emit(UiEvent.ShowToast(it.message ?: "Google Sign-In failed"))
                }
                .onSuccess { 
                    _state.value = _state.value.copy(isLoading = false, error = null)
                    _eventFlow.emit(UiEvent.ShowToast("Signed in with Google!"))
                }
        }
    }

    fun onGoogleSignInError(e: Exception) {
        val message = if (e is ApiException) {
            "Google error ${e.statusCode}: ${e.message}"
        } else {
            e.message ?: "Google Sign-In failed"
        }
        _state.value = _state.value.copy(isLoading = false, error = message)
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
    private val saveOwnerUseCase: SaveOwnerUseCase,
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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

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

    fun saveOwner(owner: Owner) {
        if (owner.fullName.isBlank() || owner.email.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Name and Email are required")) }
            return
        }
        viewModelScope.launch {
            saveOwnerUseCase(owner)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Owner saved successfully")) }
                .onFailure { _eventFlow.emit(UiEvent.ShowToast("Error saving owner: ${it.message}")) }
        }
    }

    fun savePet(pet: Pet, photoBytes: ByteArray? = null) {
        if (pet.name.isBlank() || pet.species.isBlank() || pet.ownerId.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Name, Species and Owner are required")) }
            return
        }
        viewModelScope.launch { 
            savePetUseCase(pet, photoBytes)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Pet saved successfully")) }
                .onFailure { _eventFlow.emit(UiEvent.ShowToast("Error saving pet: ${it.message}")) }
        }
    }

    fun saveAppointment(appointment: Appointment) {
        if (appointment.petId.isBlank() || appointment.reason.isBlank() || appointment.branchId.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Pet, Reason and Branch are required")) }
            return
        }
        viewModelScope.launch { 
            saveAppointmentUseCase(appointment)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Appointment scheduled")) }
                .onFailure { _eventFlow.emit(UiEvent.ShowToast("Error: ${it.message}")) }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch { 
            cancelAppointmentUseCase(appointmentId)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Appointment cancelled")) }
        }
    }

    fun saveMedicalRecord(record: MedicalRecord) {
        if (record.diagnosis.isBlank() || record.treatment.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Diagnosis and Treatment are required")) }
            return
        }
        viewModelScope.launch { 
            saveMedicalRecordUseCase(record)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Record added to history")) }
                .onFailure { _eventFlow.emit(UiEvent.ShowToast("Error: ${it.message}")) }
        }
    }

    fun saveVaccineRecord(record: VaccineRecord) {
        if (record.vaccineName.isBlank() || record.petId.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Pet and Vaccine Name are required")) }
            return
        }
        viewModelScope.launch { 
            saveVaccineRecordUseCase(record)
                .onSuccess { _eventFlow.emit(UiEvent.ShowToast("Vaccination record saved")) }
                .onFailure { _eventFlow.emit(UiEvent.ShowToast("Error: ${it.message}")) }
        }
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
    fun canAccessRoute(role: UserRole?, route: String): Boolean = repository.canAccessRoute(role, route)
    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch { repository.markNotificationRead(notificationId) }
    }
}

