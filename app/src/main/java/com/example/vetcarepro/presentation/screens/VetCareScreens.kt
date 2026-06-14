package com.example.vetcarepro.presentation.screens

import android.content.Intent
import android.util.Log
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vetcarepro.R
import com.example.vetcarepro.domain.model.Appointment
import com.example.vetcarepro.domain.model.AppointmentStatus
import com.example.vetcarepro.domain.model.MapMode
import com.example.vetcarepro.domain.model.MediaCategory
import com.example.vetcarepro.domain.model.MedicalRecord
import com.example.vetcarepro.domain.model.MultimediaItem
import com.example.vetcarepro.domain.model.Owner
import com.example.vetcarepro.domain.model.Pet
import com.example.vetcarepro.domain.model.VaccineRecord
import com.example.vetcarepro.domain.model.VaccinationStatus
import com.example.vetcarepro.domain.model.displayQrCode
import com.example.vetcarepro.domain.model.formattedDateTime
import com.example.vetcarepro.domain.model.status
import com.example.vetcarepro.presentation.components.ErrorState
import com.example.vetcarepro.presentation.components.ScreenHeader
import com.example.vetcarepro.presentation.components.SummaryCard
import com.example.vetcarepro.presentation.components.VetCareButton
import com.example.vetcarepro.presentation.components.VetCareFilterChip
import com.example.vetcarepro.presentation.components.VetCareExposedDropdown
import com.example.vetcarepro.presentation.components.VetCareImagePicker
import com.example.vetcarepro.presentation.components.VetCareTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by authViewModel.state.collectAsStateWithLifecycle()
    val session by authViewModel.session.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.vetcarepro.R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In result code: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken: String? = account?.idToken
            Log.d("LoginScreen", "Google Sign-In success, idToken present: ${idToken != null}")
            if (idToken != null) {
                authViewModel.loginWithGoogle(idToken)
            } else {
                authViewModel.onGoogleSignInError(Exception("Google ID Token is null"))
            }
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Google Sign-In API error: ${e.statusCode}", e)
            authViewModel.onGoogleSignInError(e)
        } catch (e: Exception) {
            Log.e("LoginScreen", "Google Sign-In unexpected error", e)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id= R.drawable.app_logo_tran), // This points to your file
                        contentDescription = "App Logo",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            item {
                ScreenHeader(title = "Welcome", subtitle = "Salud que se siente")
            }
            item {
                VetCareTextField(value = state.email, onValueChange = authViewModel::onEmailChange, label = "Email")
            }
            item {
                VetCareTextField(
                    value = state.password,
                    onValueChange = authViewModel::onPasswordChange,
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
            item {
                VetCareButton(text = if (state.isLoading) "Signing in..." else "Login", onClick = authViewModel::login)
            }
            item {
                OutlinedButton(
                    onClick = {
                        Log.d("LoginScreen", "Starting Google Sign-In flow")
                        // Sign out first to ensure account picker is shown
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
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
                val role = session.user?.role
                if (vetCareViewModel.canAccessRoute(role, "pet_registration")) {
                    NavigationDrawerItem(label = { Text("Pet Registration") }, selected = false, onClick = { onNavigate("pet_registration"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "owner_registration")) {
                    NavigationDrawerItem(label = { Text("Owner Registration") }, selected = false, onClick = { onNavigate("owner_registration"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "scanner")) {
                    NavigationDrawerItem(label = { Text("QR Scanner") }, selected = false, onClick = { onNavigate("scanner"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "appointments")) {
                    NavigationDrawerItem(label = { Text("Appointments") }, selected = false, onClick = { onNavigate("appointments"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "medical_history")) {
                    NavigationDrawerItem(label = { Text("Medical History") }, selected = false, onClick = { onNavigate("medical_history"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "branch_map")) {
                    NavigationDrawerItem(label = { Text("Branches") }, selected = false, onClick = { onNavigate("branch_map"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "vaccinations")) {
                    NavigationDrawerItem(label = { Text("Vaccinations") }, selected = false, onClick = { onNavigate("vaccinations"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "multimedia")) {
                    NavigationDrawerItem(label = { Text("Multimedia") }, selected = false, onClick = { onNavigate("multimedia"); scope.launch { drawerState.close() } })
                }
                if (vetCareViewModel.canAccessRoute(role, "offline")) {
                    NavigationDrawerItem(label = { Text("Offline Guides") }, selected = false, onClick = { onNavigate("offline"); scope.launch { drawerState.close() } })
                }
                HorizontalDivider()
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
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
                Card {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Welcome to VetCare Pro", fontWeight = FontWeight.Bold)
                        Text("Use the sidebar menu to navigate through the modules enabled for your role.")
                    }
                }
            }
            item {
                ScreenHeader(title = "Notifications")
                notifications.take(3).forEach { notification ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
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
    val owners by vetCareViewModel.owners.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var weight by remember { mutableStateOf("") }
    var coatColor by remember { mutableStateOf("") }
    var microchipNumber by remember { mutableStateOf("") }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var ownerId by remember { mutableStateOf("") }
    var qrCode by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Pet Registration") }) }) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Register pet", subtitle = "Store data in Firestore-ready format") }
            item { VetCareTextField(name, { name = it }, "Name") }
            item { VetCareTextField(species, { species = it }, "Species") }
            item { VetCareTextField(breed, { breed = it }, "Breed") }
            item { VetCareTextField(birthDate, { birthDate = it }, "Birth date YYYY-MM-DD") }
            item { VetCareTextField(weight, { weight = it }, "Weight") }
            item { VetCareTextField(coatColor, { coatColor = it }, "Coat color") }
            item { VetCareTextField(microchipNumber, { microchipNumber = it }, "Microchip number") }
            item {
                VetCareImagePicker("Pet Photo", null, onImageSelected = { photoBytes = it })
            }
            item {
                VetCareExposedDropdown(
                    label = "Owner",
                    selectedOption = ownerId,
                    options = owners.map { it.id to it.fullName },
                    onOptionSelected = { ownerId = it }
                )
            }
            item { VetCareTextField(qrCode, { qrCode = it }, "QR code (optional)") }
            item {
                VetCareButton("Save pet", onClick = {
                    val selectedOwner = owners.firstOrNull { it.id == ownerId }
                    vetCareViewModel.savePet(
                        Pet(
                            name = name,
                            species = species,
                            breed = breed,
                            birthDate = runCatching { LocalDate.parse(birthDate) }.getOrElse { LocalDate.now() },
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            coatColor = coatColor,
                            microchipNumber = microchipNumber,
                            ownerId = ownerId,
                            ownerName = selectedOwner?.fullName ?: "",
                            qrCode = qrCode.ifBlank { "PET-$microchipNumber" }
                        ),
                        photoBytes = photoBytes
                    )
                    if (name.isNotBlank()) {
                        name = ""; species = ""; breed = ""; birthDate = LocalDate.now().toString()
                        weight = ""; coatColor = ""; microchipNumber = ""; qrCode = ""; ownerId = ""
                        photoBytes = null
                    }
                })
            }
            item {
                ScreenHeader(title = "Existing pets")
                vetCareViewModel.pets.collectAsStateWithLifecycle().value.forEach { pet ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
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
fun OwnerRegistrationScreen(vetCareViewModel: VetCareViewModel) {
    val owners by vetCareViewModel.owners.collectAsStateWithLifecycle()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Owner Registration") }) }) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Register owner", subtitle = "Required for pet registration") }
            item { VetCareTextField(fullName, { fullName = it }, "Full Name") }
            item { VetCareTextField(email, { email = it }, "Email") }
            item { VetCareTextField(phone, { phone = it }, "Phone") }
            item { VetCareTextField(address, { address = it }, "Address") }
            item { VetCareTextField(documentId, { documentId = it }, "Document ID") }
            item {
                VetCareButton("Save owner", onClick = {
                    vetCareViewModel.saveOwner(
                        Owner(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            address = address,
                            documentId = documentId
                        )
                    )
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        fullName = ""; email = ""; phone = ""; address = ""; documentId = ""
                    }
                })
            }
            item {
                ScreenHeader(title = "Existing owners")
                owners.forEach { owner ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(owner.fullName, fontWeight = FontWeight.Bold)
                            Text(owner.email)
                            Text(owner.phone)
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
    var manualCode by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: return@rememberLauncherForActivityResult
        vetCareViewModel.findPetByQrCode(contents)?.let { onPetFound(it.id) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("QR Scanner") }) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    val branches by vetCareViewModel.branches.collectAsStateWithLifecycle()
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    val owners by vetCareViewModel.owners.collectAsStateWithLifecycle()
    var appointmentId by remember { mutableStateOf("") }
    var petId by remember { mutableStateOf("") }
    var branchId by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppointmentStatus.PENDING) }
    var serviceType by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Appointment Calendar") }) }) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Appointments", subtitle = "Create, update, cancel, sync") }
            item {
                VetCareExposedDropdown(
                    label = "Pet",
                    selectedOption = petId,
                    options = pets.map { it.id to it.name },
                    onOptionSelected = { petId = it }
                )
            }
            item {
                VetCareExposedDropdown(
                    label = "Branch",
                    selectedOption = branchId,
                    options = branches.map { it.id to it.name },
                    onOptionSelected = { branchId = it }
                )
            }
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
                    val selectedPet = pets.firstOrNull { it.id == petId }
                    vetCareViewModel.saveAppointment(
                        Appointment(
                            id = appointmentId.ifBlank { "a-${System.currentTimeMillis()}" },
                            petId = petId,
                            ownerId = selectedPet?.ownerId ?: "",
                            branchId = branchId,
                            dateTime = runCatching { LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }.getOrElse { LocalDateTime.now().plusDays(1) },
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
                    val pet = pets.firstOrNull { it.id == appointment.petId }
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Pet: ${pet?.name ?: "Unknown"}", fontWeight = FontWeight.Bold)
                            Text(appointment.reason)
                            Text(appointment.formattedDateTime())
                            Text(appointment.status.label)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    appointmentId = appointment.id
                                    petId = appointment.petId
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
    var selectedPetId by remember { mutableStateOf(petId ?: "") }
    val selectedPet = pets.firstOrNull { it.id == selectedPetId }
    val records = selectedPet?.let { vetCareViewModel.medicalRecordsForPet(it.id) }.orEmpty()
    var diagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Medical History") }) }) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                VetCareExposedDropdown(
                    label = "Select Pet",
                    selectedOption = selectedPetId,
                    options = pets.map { it.id to it.name },
                    onOptionSelected = { selectedPetId = it }
                )
            }
            item { ScreenHeader(title = selectedPet?.name ?: "No pet selected", subtitle = "Chronological timeline") }
            item {
                VetCareTextField(diagnosis, { diagnosis = it }, "Diagnosis")
                VetCareTextField(treatment, { treatment = it }, "Treatment")
                VetCareTextField(notes, { notes = it }, "Notes")
                VetCareTextField(temperature, { temperature = it }, "Temperature")
                VetCareTextField(weight, { weight = it }, "Weight")
                VetCareImagePicker("Clinical Image", null, onImageSelected = { photoBytes = it })
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
                                weight = weight.toDoubleOrNull() ?: it.weight
                            )
                        )
                    }
                })
            }
            item {
                records.forEach { record ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(record.diagnosis, fontWeight = FontWeight.Bold)
                            Text(record.formattedDateTime())
                            Text(record.treatment)
                            Text("T=${record.temperature} W=${record.weight}")
                            Text(record.notes)
                            if (record.images.isNotEmpty()) {
                                record.images.forEach { image ->
                                    AsyncImage(model = image, contentDescription = null, modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp))
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
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
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
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
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
    val vaccines by vetCareViewModel.vaccines.collectAsStateWithLifecycle()
    val pets by vetCareViewModel.pets.collectAsStateWithLifecycle()
    var petId by remember { mutableStateOf("") }
    var vaccineName by remember { mutableStateOf("") }
    var laboratory by remember { mutableStateOf("") }
    var lot by remember { mutableStateOf("") }
    var applicationDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var nextDoseDate by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var notes by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Vaccination Control") }) }) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Vaccines", subtitle = "Green = up to date • Amber = expiring • Red = overdue") }
            item {
                VetCareExposedDropdown(
                    label = "Pet",
                    selectedOption = petId,
                    options = pets.map { it.id to it.name },
                    onOptionSelected = { petId = it }
                )
            }
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
                    val pet = pets.firstOrNull { it.id == record.petId }
                    val status = record.status()
                    val color = when (status) {
                        VaccinationStatus.UP_TO_DATE -> Color(0xFF2E7D32) // Success Green
                        VaccinationStatus.EXPIRING -> Color(0xFFF9A825)   // Warning Amber
                        VaccinationStatus.OVERDUE -> Color(0xFFE64A19)    // Accent Red
                    }
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Pet: ${pet?.name ?: "Unknown"}", fontWeight = FontWeight.Bold)
                                    Text(record.vaccineName)
                                }
                                Box(modifier = Modifier
                                    .size(16.dp)
                                    .background(color, RoundedCornerShape(999.dp)))
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
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(media.title, fontWeight = FontWeight.Bold)
                            Text(media.description)
                            if (media.mediaType == com.example.vetcarepro.domain.model.MediaType.VIDEO) {
                                Text("Video: ${media.mediaUrl}")
                            } else {
                                AsyncImage(model = media.thumbnailUrl.ifBlank { media.mediaUrl }, contentDescription = null, modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp))
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
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader(title = "Offline guides", subtitle = "Local JSON + cached images") }
            item { VetCareTextField(query, { query = it }, "Search guides") }
            item {
                guides.forEach { guide ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
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
