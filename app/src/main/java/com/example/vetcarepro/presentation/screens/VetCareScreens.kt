package com.example.vetcarepro.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButtonDefaults.colors
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.IconButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.MapMode
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.NotificationType
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.UserRole
import com.example.vetcarepro.domain.model.VaccineRecord
import com.example.vetcarepro.domain.model.VaccinationStatus
import com.example.vetcarepro.domain.model.displayQrCode
import com.example.vetcarepro.domain.model.formattedDateTime
import com.example.vetcarepro.domain.model.status
import com.example.vetcarepro.presentation.components.ErrorState
import com.example.vetcarepro.presentation.components.LoadingState
import com.example.vetcarepro.presentation.components.ScreenHeader
import com.example.vetcarepro.presentation.components.SmallInfoPill
import com.example.vetcarepro.presentation.components.SummaryCard
import com.example.vetcarepro.presentation.components.VetCareButton
import com.example.vetcarepro.presentation.components.VetCareFilterChip
import com.example.vetcarepro.presentation.components.VetCareTextField
import com.example.vetcarepro.presentation.viewmodels.AuthUiState
import com.example.vetcarepro.presentation.viewmodels.AuthViewModel
import com.example.vetcarepro.presentation.viewmodels.VetCareViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by authViewModel.state.collectAsStateWithLifecycle()
    val session by authViewModel.session.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken: String? = account?.idToken
            if (idToken != null) {
                authViewModel.loginWithGoogle(idToken)
            } else {
                authViewModel.onGoogleSignInError(Exception("Google ID Token is null"))
            }
        } catch (e: ApiException) {
            authViewModel.onGoogleSignInError(e)
        }
    }

    LaunchedEffect(session.isAuthenticated) {
        if (session.isAuthenticated) onLoginSuccess()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("VetCare Pro") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScreenHeader(title = "Welcome", subtitle = "Salud que se siente")
            }
            item {
                VetCareTextField(value = state.email, onValueChange = authViewModel::onEmailChange, label = "Email")
            }
            item {
                VetCareTextField(value = state.password, onValueChange = authViewModel::onPasswordChange, label = "Password")
            }
            item {
                VetCareButton(text = if (state.isLoading) "Signing in..." else "Login", onClick = authViewModel::login)
            }
            item {
                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(com.example.vetcarepro.R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign in with Google")
                }
            }
            item {
                VetCareTextField(value = state.forgotEmail, onValueChange = authViewModel::onForgotEmailChange, label = "Forgot password email")
            }
            item {
                OutlinedButton(onClick = authViewModel::forgotPassword, modifier = Modifier.fillMaxWidth()) { Text("Forgot password") }
            }
            item {
                state.error?.let { ErrorState(it) }
                state.resetMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
            }
            item {
                Text(text = "Demo accounts: vet@vetcare.pro / recepcion@vetcare.pro / owner@vetcare.pro")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vetCareViewModel: VetCareViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val session by vetCareViewModel.session.collectAsStateWithLifecycle()
    val dashboard by vetCareViewModel.dashboard.collectAsStateWithLifecycle()
    val notifications by vetCareViewModel.notifications.collectAsStateWithLifecycle()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "VetCare Pro",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                ScreenHeader(
                    title = session.user?.fullName ?: "VetCare Pro",
                    subtitle = session.user?.role?.label ?: "Guest"
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Pets", dashboard.totalPets.toString(), MaterialTheme.colorScheme.primary)
                    SummaryCard("Appointments", dashboard.totalAppointments.toString(), MaterialTheme.colorScheme.secondary)
                    SummaryCard("Vaccines", dashboard.totalVaccines.toString(), MaterialTheme.colorScheme.tertiary)
                    SummaryCard("Alerts", dashboard.activeNotifications.toString(), MaterialTheme.colorScheme.error)
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val role = session.user?.role
                    if (vetCareViewModel.canAccessRoute(role, "pet_registration")) {
                        VetCareFilterChip(text = "Pet Registration", selected = true, onClick = { onNavigate("pet_registration") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "scanner")) {
                        VetCareFilterChip(text = "Scanner", selected = true, onClick = { onNavigate("scanner") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "appointments")) {
                        VetCareFilterChip(text = "Appointments", selected = true, onClick = { onNavigate("appointments") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "medical_history")) {
                        VetCareFilterChip(text = "Medical History", selected = true, onClick = { onNavigate("medical_history") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "branch_map")) {
                        VetCareFilterChip(text = "Branches", selected = true, onClick = { onNavigate("branch_map") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "vaccinations")) {
                        VetCareFilterChip(text = "Vaccinations", selected = true, onClick = { onNavigate("vaccinations") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "multimedia")) {
                        VetCareFilterChip(text = "Multimedia", selected = true, onClick = { onNavigate("multimedia") })
                    }
                    if (vetCareViewModel.canAccessRoute(role, "offline")) {
                        VetCareFilterChip(text = "Offline", selected = true, onClick = { onNavigate("offline") })
                    }
                }
            }
            item {
                Card {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Role based navigation", fontWeight = FontWeight.Bold)
                        Text(session.user?.role?.label ?: "Guest")
                        Text("Use the buttons to access enabled modules.")
                    }
                }
            }
            item {
                ScreenHeader(title = "Notifications")
                notifications.take(3).forEach { notification ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(notification.title, fontWeight = FontWeight.Bold)
                            Text(notification.message)
                            if (notification.actionable) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onNavigate(notification.targetRoute) }) { Text("View details") }
                                    OutlinedButton(onClick = { vetCareViewModel.markNotificationRead(notification.id) }) { Text("Accept") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegistrationScreen(vetCareViewModel: VetCareViewModel) {
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    val owners by vetCareViewModel.owners.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var weight by remember { mutableStateOf("0") }
    var coatColor by remember { mutableStateOf("") }
    var microchipNumber by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var ownerId by remember { mutableStateOf(owners.firstOrNull()?.id.orEmpty()) }
    var ownerName by remember { mutableStateOf(owners.firstOrNull()?.fullName.orEmpty()) }
    var qrCode by remember { mutableStateOf("") }

    LaunchedEffect(owners) {
        if (ownerId.isBlank()) ownerId = owners.firstOrNull()?.id.orEmpty()
        if (ownerName.isBlank()) ownerName = owners.firstOrNull()?.fullName.orEmpty()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Pet Registration") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Register pet", subtitle = "Store data in Firestore-ready format") }
            item { VetCareTextField(name, { name = it }, "Name") }
            item { VetCareTextField(species, { species = it }, "Species") }
            item { VetCareTextField(breed, { breed = it }, "Breed") }
            item { VetCareTextField(birthDate, { birthDate = it }, "Birth date YYYY-MM-DD") }
            item { VetCareTextField(weight, { weight = it }, "Weight") }
            item { VetCareTextField(coatColor, { coatColor = it }, "Coat color") }
            item { VetCareTextField(microchipNumber, { microchipNumber = it }, "Microchip number") }
            item { VetCareTextField(photoUrl, { photoUrl = it }, "Photo URL") }
            item { VetCareTextField(ownerId, { ownerId = it }, "Owner ID") }
            item { VetCareTextField(ownerName, { ownerName = it }, "Owner name") }
            item { VetCareTextField(qrCode, { qrCode = it }, "QR code") }
            item {
                VetCareButton("Save pet", onClick = {
                    vetCareViewModel.savePet(
                        Pet(
                            name = name,
                            species = species,
                            breed = breed,
                            birthDate = LocalDate.parse(birthDate),
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            coatColor = coatColor,
                            microchipNumber = microchipNumber,
                            photoUrl = photoUrl,
                            ownerId = ownerId,
                            ownerName = ownerName,
                            qrCode = qrCode.ifBlank { "PET-$microchipNumber" }
                        )
                    )
                    name = ""
                    species = ""
                    breed = ""
                    birthDate = LocalDate.now().toString()
                    weight = "0"
                    coatColor = ""
                    microchipNumber = ""
                    photoUrl = ""
                    qrCode = ""
                })
            }
            item {
                ScreenHeader(title = "Existing pets")
                pets.forEach { pet ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(pet.name, fontWeight = FontWeight.Bold)
                            Text("${pet.species} • ${pet.breed}")
                            Text("Owner: ${pet.ownerName}")
                            Text("QR: ${pet.displayQrCode()}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(vetCareViewModel: VetCareViewModel, onPetFound: (String) -> Unit) {
    val context = LocalContext.current
    var manualCode by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: return@rememberLauncherForActivityResult
        vetCareViewModel.findPetByQrCode(contents)?.let { onPetFound(it.id) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("QR Scanner") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader(title = "Scan pet QR", subtitle = "Open the record directly")
            VetCareButton("Start ZXing scan", onClick = {
                val options = ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Scan pet QR").setBeepEnabled(true).setOrientationLocked(false)
                launcher.launch(options)
            })
            VetCareTextField(manualCode, { manualCode = it }, "Manual QR / pet id")
            OutlinedButton(onClick = {
                vetCareViewModel.findPetByQrCode(manualCode)?.let { onPetFound(it.id) }
            }, modifier = Modifier.fillMaxWidth()) { Text("Open record") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCalendarScreen(vetCareViewModel: VetCareViewModel) {
    val appointments by vetCareViewModel.appointments.collectAsStateWithLifecycle()
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    val branches by vetCareViewModel.branches.collectAsStateWithLifecycle()
    var appointmentId by remember { mutableStateOf("") }
    var petId by remember { mutableStateOf(pets.firstOrNull()?.id.orEmpty()) }
    var ownerId by remember { mutableStateOf(pets.firstOrNull()?.ownerId.orEmpty()) }
    var branchId by remember { mutableStateOf(branches.firstOrNull()?.id.orEmpty()) }
    var dateTime by remember { mutableStateOf(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppointmentStatus.PENDING) }
    var serviceType by remember { mutableStateOf("") }

    LaunchedEffect(pets, branches) {
        if (petId.isBlank()) petId = pets.firstOrNull()?.id.orEmpty()
        if (ownerId.isBlank()) ownerId = pets.firstOrNull()?.ownerId.orEmpty()
        if (branchId.isBlank()) branchId = branches.firstOrNull()?.id.orEmpty()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Appointment Calendar") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Appointments", subtitle = "Create, update, cancel, sync") }
            item { VetCareTextField(appointmentId, { appointmentId = it }, "Appointment id (empty to create)") }
            item { VetCareTextField(petId, { petId = it }, "Pet id") }
            item { VetCareTextField(ownerId, { ownerId = it }, "Owner id") }
            item { VetCareTextField(branchId, { branchId = it }, "Branch id") }
            item { VetCareTextField(dateTime, { dateTime = it }, "Date time yyyy-MM-dd HH:mm") }
            item { VetCareTextField(reason, { reason = it }, "Reason") }
            item { VetCareTextField(notes, { notes = it }, "Notes") }
            item { VetCareTextField(serviceType, { serviceType = it }, "Service type") }
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppointmentStatus.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = status == item,
                            onClick = { status = item },
                            shape = SegmentedButtonDefaults.itemShape(index, AppointmentStatus.entries.size)
                        ) {
                            Text(item.label)
                        }
                    }
                }
            }
            item {
                VetCareButton("Save appointment", onClick = {
                    vetCareViewModel.saveAppointment(
                        Appointment(
                            id = appointmentId.ifBlank { "a-${System.currentTimeMillis()}" },
                            petId = petId,
                            ownerId = ownerId,
                            branchId = branchId,
                            dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            status = status,
                            reason = reason,
                            notes = notes,
                            serviceType = serviceType
                        )
                    )
                })
            }
            item {
                appointments.sortedBy { it.dateTime }.forEach { appointment ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(appointment.reason, fontWeight = FontWeight.Bold)
                            Text(appointment.formattedDateTime())
                            Text(appointment.status.label)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    appointmentId = appointment.id
                                    petId = appointment.petId
                                    ownerId = appointment.ownerId
                                    branchId = appointment.branchId
                                    dateTime = appointment.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    reason = appointment.reason
                                    notes = appointment.notes
                                    serviceType = appointment.serviceType
                                    status = appointment.status
                                }) { Text("Update") }
                                OutlinedButton(onClick = { vetCareViewModel.cancelAppointment(appointment.id) }) { Text("Cancel") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(vetCareViewModel: VetCareViewModel, petId: String?) {
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    val selectedPet = petId?.let { vetCareViewModel.petById(it) } ?: pets.firstOrNull()
    val records = selectedPet?.let { vetCareViewModel.medicalRecordsForPet(it.id) }.orEmpty()
    var diagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("38.5") }
    var weight by remember { mutableStateOf(selectedPet?.weight?.toString().orEmpty()) }
    var imageUrl by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Medical History") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = selectedPet?.name ?: "No pet selected", subtitle = "Chronological timeline") }
            item {
                VetCareTextField(diagnosis, { diagnosis = it }, "Diagnosis")
                VetCareTextField(treatment, { treatment = it }, "Treatment")
                VetCareTextField(notes, { notes = it }, "Notes")
                VetCareTextField(temperature, { temperature = it }, "Temperature")
                VetCareTextField(weight, { weight = it }, "Weight")
                VetCareTextField(imageUrl, { imageUrl = it }, "Image URL")
                VetCareButton("Add record", onClick = {
                    selectedPet?.let {
                        vetCareViewModel.saveMedicalRecord(
                            MedicalRecord(
                                petId = it.id,
                                veterinarianId = vetCareViewModel.session.value.user?.id.orEmpty(),
                                veterinarianName = vetCareViewModel.session.value.user?.fullName.orEmpty(),
                                diagnosis = diagnosis,
                                treatment = treatment,
                                notes = notes,
                                temperature = temperature.toDoubleOrNull() ?: 0.0,
                                weight = weight.toDoubleOrNull() ?: it.weight,
                                images = imageUrl.takeIf { url -> url.isNotBlank() }?.let { url -> listOf(url) }.orEmpty()
                            )
                        )
                    }
                })
            }
            item {
                records.forEach { record ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(record.diagnosis, fontWeight = FontWeight.Bold)
                            Text(record.formattedDateTime())
                            Text(record.treatment)
                            Text("T=${record.temperature} W=${record.weight}")
                            Text(record.notes)
                            if (record.images.isNotEmpty()) {
                                record.images.forEach { image ->
                                    AsyncImage(model = image, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BranchMapScreen(vetCareViewModel: VetCareViewModel) {
    val branches by vetCareViewModel.branches.collectAsStateWithLifecycle()
    val mapMode by vetCareViewModel.mapMode.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(branches.firstOrNull()?.latitude ?: 10.4806, branches.firstOrNull()?.longitude ?: -66.9036), 12f)
    }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Branch Map") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Branches", subtitle = "Normal / Satellite / Hybrid") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MapMode.entries.forEach { mode ->
                        VetCareFilterChip(mode.label, mapMode == mode, onClick = { vetCareViewModel.setMapMode(mode) })
                    }
                }
            }
            item {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = when (mapMode) {
                            MapMode.NORMAL -> com.google.maps.android.compose.MapType.NORMAL
                            MapMode.SATELLITE -> com.google.maps.android.compose.MapType.SATELLITE
                            MapMode.HYBRID -> com.google.maps.android.compose.MapType.HYBRID
                        }
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                ) {
                    branches.forEach { branch ->
                        Marker(
                            state = MarkerState(position = LatLng(branch.latitude, branch.longitude)),
                            title = branch.name,
                            snippet = branch.address,
                            onInfoWindowClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${branch.latitude},${branch.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }
                        )
                    }
                }
            }
            item {
                branches.forEach { branch ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(branch.name, fontWeight = FontWeight.Bold)
                            Text(branch.address)
                            Text(branch.phone)
                            Text(branch.services.joinToString())
                            OutlinedButton(onClick = {
                                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${branch.latitude},${branch.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }) { Text("Route") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationControlScreen(vetCareViewModel: VetCareViewModel) {
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    val vaccines by vetCareViewModel.vaccines.collectAsStateWithLifecycle()
    var petId by remember { mutableStateOf(pets.firstOrNull()?.id.orEmpty()) }
    var vaccineName by remember { mutableStateOf("") }
    var laboratory by remember { mutableStateOf("") }
    var lot by remember { mutableStateOf("") }
    var applicationDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var nextDoseDate by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var notes by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Vaccination Control") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Vaccines", subtitle = "Green = up to date • Amber = expiring • Red = overdue") }
            item { VetCareTextField(petId, { petId = it }, "Pet id") }
            item { VetCareTextField(vaccineName, { vaccineName = it }, "Vaccine name") }
            item { VetCareTextField(laboratory, { laboratory = it }, "Laboratory") }
            item { VetCareTextField(lot, { lot = it }, "Lot") }
            item { VetCareTextField(applicationDate, { applicationDate = it }, "Application date yyyy-MM-dd") }
            item { VetCareTextField(nextDoseDate, { nextDoseDate = it }, "Next dose date yyyy-MM-dd") }
            item { VetCareTextField(notes, { notes = it }, "Notes") }
            item {
                VetCareButton("Save vaccine", onClick = {
                    vetCareViewModel.saveVaccineRecord(
                        VaccineRecord(
                            petId = petId,
                            vaccineName = vaccineName,
                            laboratory = laboratory,
                            lot = lot,
                            applicationDate = LocalDate.parse(applicationDate),
                            nextDoseDate = LocalDate.parse(nextDoseDate),
                            notes = notes
                        )
                    )
                })
            }
            item {
                vaccines.sortedByDescending { it.nextDoseDate }.forEach { record ->
                    val status = record.status()
                    val color = when (status) {
                        VaccinationStatus.UP_TO_DATE -> Color(0xFF2E7D32) // Success Green
                        VaccinationStatus.EXPIRING -> Color(0xFFF9A825)   // Warning Amber
                        VaccinationStatus.OVERDUE -> Color(0xFFE64A19)    // Accent Red
                    }
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(record.vaccineName, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.size(16.dp).background(color, RoundedCornerShape(999.dp)))
                            }
                            Text("${record.laboratory} • ${record.lot}")
                            Text("Next dose: ${record.nextDoseDate}")
                            Text(status.name, color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MultimediaCatalogScreen(vetCareViewModel: VetCareViewModel) {
    val items by vetCareViewModel.filteredMedia.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf<MediaCategory?>(null) }
    LaunchedEffect(category) { vetCareViewModel.setMediaCategory(category) }

    Scaffold(topBar = { TopAppBar(title = { Text("Multimedia Catalog") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Media library", subtitle = "Image gallery and video player") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VetCareFilterChip("All", category == null, onClick = { category = null })
                    MediaCategory.entries.forEach { entry ->
                        VetCareFilterChip(entry.label, category == entry, onClick = { category = entry })
                    }
                }
            }
            item {
                items.forEach { media ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(media.title, fontWeight = FontWeight.Bold)
                            Text(media.description)
                            if (media.mediaType == com.example.vetcarepro.domain.model.MediaType.VIDEO) {
                                Text("Video: ${media.mediaUrl}")
                            } else {
                                AsyncImage(model = media.thumbnailUrl.ifBlank { media.mediaUrl }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineInformationScreen(vetCareViewModel: VetCareViewModel) {
    var query by remember { mutableStateOf("") }
    val guides = vetCareViewModel.guidesByQuery(query)

    Scaffold(topBar = { TopAppBar(title = { Text("Offline Information") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Offline guides", subtitle = "Local JSON + cached images") }
            item { VetCareTextField(query, { query = it }, "Search guides") }
            item {
                guides.forEach { guide ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(guide.title, fontWeight = FontWeight.Bold)
                            Text(guide.category)
                            Text(guide.content)
                        }
                    }
                }
            }
        }
    }
}
