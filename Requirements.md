# ESPECIFICACIÓN TÉCNICA DE DESARROLLO - VETCARE PRO

Este documento contiene las directrices de arquitectura, reglas de generación de código, requerimientos funcionales y el sistema de diseño para la aplicación **VetCare Pro**. Debe ser procesado por la IA como la única fuente de verdad para el desarrollo del proyecto.

---

## 1. MODO DE OPERACIÓN DE LA IA (CAVEMAN MODE)

La IA debe seguir estrictamente las siguientes reglas de interacción y generación de código:

* **Sin explicaciones largas:** No justifiques las decisiones de diseño arquitectónico ni expliques cómo funciona el código a menos que se te solicite explícitamente.
* **Sin tutoriales:** Ve directo a la implementación.
* **Sin comentarios innecesarios:** El código debe ser autodocumentado. Evita comentarios obvios.
* **Código listo para producción:** Todo el código generado debe ser funcional, libre de errores sintácticos y optimizado.
* **Archivos completos:** No utilices marcadores de posición (`// TODO`, `...`). Genera el archivo completo desde los `import` hasta la última llave.
* **Continuidad automática:** Si una funcionalidad requiere múltiples archivos (e.g., Modelo, Repositorio, ViewModel, Screen), genera todos de forma secuencial sin detenerte.
* **Sin simplificaciones:** Implementa todos los campos, reglas de negocio y validaciones descritas en los requerimientos.

---

## 2. INFORMACIÓN DEL PROYECTO Y ARQUITECTURA

### 2.1. Datos Base
* **Nombre de la Aplicación:** VetCare Pro
* **Eslógan:** "Salud que se siente"
* **Plataforma Target:** Android (Kotlin)
* **UI Framework:** Jetpack Compose con Material Design 3

### 2.2. Arquitectura (Clean Architecture + MVVM)
El proyecto se divide estrictamente en tres capas aisladas:

```text
app/
├── data/          # Implementación de Repositorios, API Remota (Firebase), Caché Local
├── domain/        # Modelos de Entidad puros, Interfaces de Repositorio, Casos de Uso (Use Cases)
└── presentation/  # UI (Compose), ViewModels (StateFlow), Navegación, Componentes Comunes
```

### 2.3. Tecnologías y Patrones Obligatorios

* **Inyección de Dependencias:** Hilt DI (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@Module`, `@Provides`).
* **Manejo de Estado:** `StateFlow` y `SharingStarted.WhileSubscribed` en ViewModels; `collectAsStateWithLifecycle()` en la capa de UI.
* **Navegación:** Navigation Compose (Uso de Rutas Seguras/Type-Safe si es posible o Strings fuertemente tipados).
* **Backend Ecosystem:** Firebase (Auth, Cloud Firestore, Storage, Messaging).

---

## 3. ROLES DE USUARIO Y SEGURIDAD

La aplicación cuenta con tres roles de usuario distintos. Tras una autenticación exitosa, el rol debe ser recuperado desde la colección `users` en Cloud Firestore para determinar el flujo de navegación de la UI:

| Rol | Permisos Clave |
| --- | --- |
| **Veterinario** | Gestión clínica completa (Historiales médicos, Vacunación, QR). |
| **Recepcionista** | Gestión operativa (Calendario de citas, Registro de mascotas y propietarios). |
| **Propietario** | Consulta y visualización (Ver mascotas, historial, recibir notificaciones de citas/vacunas). |

---

## 4. SISTEMA DE DISEÑO (MATERIAL 3)

Todos los componentes de la interfaz de usuario deben respetar los siguientes tokens de color y la guía tipográfica de Material 3:

* **Primary:** `#007B7F`
* **Primary Dark:** `#005B5E`
* **Accent:** `#E64A19`
* **Success:** `#2E7D32`
* **Warning:** `#F9A825`
* **Background:** `#F4F7F7`
* **Text:** `#444444`

---

## 5. REQUERIMIENTOS FUNCIONALES (RF)

### RF-01: Autenticación y Control de Accesos

* Login clásico con Email y Contraseña a través de Firebase Auth.
* Login social federado con Google Sign-In.
* Flujo de "Olvidé mi contraseña" con envío de correo de recuperación.
* Persistencia de sesión activa al reiniciar la app.
* Enrutamiento dinámico inicial según el rol almacenado en la colección `users`.

### RF-02: Registro y Gestión de Mascotas

* Formulario de alta con los siguientes campos: `name`, `species`, `breed`, `birthDate`, `weight`, `coatColor`, `microchipNumber`, `photoUrl`, `ownerId`.
* Subida obligatoria de la fotografía de la mascota a Firebase Storage antes de persistir el documento en Firestore.

### RF-03: Expediente Médico mediante Código QR

* Generación automática de un código QR único para cada mascota basado en su ID de Firestore.
* Módulo de escaneo integrado utilizando la librería ZXing.
* El escaneo exitoso redirige inmediatamente a la pantalla de Historial Médico (`RF-04`) de la mascota correspondiente.

### RF-04: Historial Clínico Digital

* Registro cronológico de consultas médicas.
* Datos a almacenar por consulta: `diagnosis`, `treatment`, `notes`, `temperature`, `weight`, `images` (lista de URLs de Firestore con imágenes adjuntas, almacenadas dentro del dispositivo del usuario), `consultationDate`.
* Renderizado en la UI en formato de línea de tiempo (Chronological Timeline).

### RF-05: Gestión del Calendario de Citas

* Operaciones CRUD completas para citas veterinarias sincronizadas en tiempo real con Firestore (`addSnapshotListener`).
* Estados de la cita: `Pending`, `Confirmed`, `Cancelled`, `Completed`.

### RF-06: Notificaciones Push Automatizadas

* Integración con Firebase Cloud Messaging (FCM).
* Lógica para alertas del sistema:
* Recordatorio de cita: 24 horas antes.
* Recordatorio de cita crítico: 2 horas antes.
* Aviso de expiración de vacuna: 7 días antes de la fecha límite (`nextDoseDate`).



### RF-07: Geolocalización de Sucursales

* Integración de Google Maps Compose.
* Marcadores interactivos con todas las sucursales de la veterinaria obtenidas desde Firestore (`branches`).
* Trazado de rutas y navegación desde la ubicación actual del dispositivo a la sucursal seleccionada.
* Controles en UI para alternar modos de mapa: Normal, Satélite e Híbrido.

### RF-08: Notificaciones de Aprobación Accionables

* Al generar una nueva consulta o receta, se dispara una notificación push con acciones interactivas directas en la barra de estado.
* Botones embebidos en la notificación: `[Aceptar]` y `[Ver Detalles]`.

### RF-09: Catálogo Multimedia de Servicios

* Secciones categorizadas: `Surgery`, `Grooming`, `Laboratory`, `Dental`.
* Reproductor de video nativo o compatible con Compose.
* Galería de imágenes optimizada. Los recursos deben consumirse directamente desde rutas estructuradas en Firebase Storage.

### RF-10: Búsqueda Avanzada y Filtrado

* Barra de búsqueda reactiva por: Nombre de la mascota, Nombre del propietario, Especie o Código QR.
* Filtros acumulativos por: Estado de vacunación (Al día/Vencido), Fecha de última consulta y Tipo de servicio requerido.

### RF-11: Control de Vacunación con Semáforo Visual

* Estructura de datos del registro: `vaccineName`, `laboratory`, `lot`, `applicationDate`, `nextDoseDate`.
* Estados lógicos y colores mandatorios en la UI:
* **Verde (#2E7D32):** Vacunación al día.
* **Ámbar (#F9A825):** Próxima a expirar.
* **Rojo (#E64A19):** Vencida / Pendiente urgente.



### RF-12: Modo Offline e Información Local

* Disponibilidad de guías veterinarias locales mediante el parseo de un archivo JSON estático incluido en los `assets`.
* Caché local de imágenes usando Coil con configuración de persistencia en disco.
* Mecanismo de sincronización diferida para cambios realizados sin conexión a internet cuando el estado de la red se restablezca.

---

## 6. MAPEO DE PANTALLAS (SCREENS)

La aplicación debe implementar obligatoriamente las siguientes 10 pantallas dentro de la capa de presentación:

1. `LoginScreen`: Acceso y recuperación de cuenta.
2. `DashboardScreen`: Vista principal diferenciada por el rol del usuario activo.
3. `PetRegistrationScreen`: Formulario de captura y carga de datos de la mascota.
4. `QrScannerScreen`: Interfaz de cámara para escaneo de códigos ZXing.
5. `AppointmentCalendarScreen`: Calendario interactivo y gestión de citas.
6. `MedicalHistoryScreen`: Línea de tiempo con el expediente y notas clínicas de la mascota.
7. `BranchMapScreen`: Mapa con las sucursales y geolocalización.
8. `VaccinationControlScreen`: Panel de control de vacunas con el semáforo visual de estados.
9. `MultimediaCatalogScreen`: Biblioteca de medios categorizada por servicios.
10. `OfflineInformationScreen`: Vista de consulta de guías descargadas y estado de la caché.

---

## 7. REGLAS IMPRESCINDIBLES DE GENERACIÓN DE CÓDIGO

Al codificar cualquier feature de esta especificación, debes estructurar los archivos respetando el patrón de inyección de dependencias con Hilt, el patrón repositorio y el flujo unidireccional de datos (UDF). Cada pantalla debe implementar obligatoriamente tres estados de UI representados en una jerarquía sellada (`sealed interface` o `sealed class`): `Loading`, `Success(data)` y `Error(message)`.

```

```