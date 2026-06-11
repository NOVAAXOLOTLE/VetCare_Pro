package com.example.vetcarepro.presentation.navigation

object VetCareRoutes {
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val PetRegistration = "pet_registration"
    const val Scanner = "scanner"
    const val Appointments = "appointments"
    const val MedicalHistory = "medical_history"
    const val BranchMap = "branch_map"
    const val Vaccinations = "vaccinations"
    const val Multimedia = "multimedia"
    const val Offline = "offline"

    const val PetIdArg = "petId"
    const val MedicalHistoryWithPet = "medical_history/{$PetIdArg}"

    fun medicalHistoryRoute(petId: String) = "medical_history/$petId"
}

