<table>
  <tr>
    <td>
      <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="Logo Recordatorios de Puchi" style="border-radius: 20%;">
    </td>
    <td>
      <h1>Recordatorios de Puchi</h1>
      <p>
        <a href="https://creativecommons.org/licenses/by-nc/4.0/">
          <img src="https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg" alt="License: CC BY-NC 4.0">
        </a>
        <br>
        <b>Una solución digital para la tercera edad.</b>
      </p>
    </td>
  </tr>
</table>

---

## 🇪🇸 Español

### 🧠 La Filosofía del Proyecto: Más que una App
**Recordatorios de Puchi** no es un simple "Launcher" para mayores. Es un proyecto de ingeniería de software aplicado a una necesidad neurodegenerativa específica.

Nace para devolver la autonomía a **Puchi**, mi abuela de **85 años**, diagnosticada con **Alzheimer** en fase inicial. Mi experiencia conviviendo con la enfermedad me ha enseñado que el problema no es solo que "no recuerde números"; el problema es la **agnosia funcional**: Puchi olvida para qué sirven los objetos.

Si el teléfono no interactúa con ella, su cerebro deja de percibirlo como una herramienta de comunicación y pasa a categorizarlo como un "marco de fotos digital" donde ve pasar a sus nietos.

**Esta aplicación transforma el Smartphone pasivo en un Compañero Activo.**

---

### ✨ Innovación y Funcionalidades Clave

#### 1. 🤖 El Asistente Virtual: Un "Ancla a la Realidad"
Esta es la funcionalidad más crítica del sistema. No es un adorno; es una herramienta de **refuerzo cognitivo constante**.
* **Recordatorio de Propósito:** Cada cierto tiempo (configurable), el asistente "despierta" y le recuerda verbalmente a Puchi: *"Hola, soy tu teléfono, estoy aquí para que llames a tus hijos"*. Sin este estímulo, ella olvida que el dispositivo sirve para llamar.
* **Compañía y Empatía:** Un avatar animado con sincronización labial (*lipsync*) y gestos de saludo reduce la sensación de interactuar con una máquina fría.
* **Interacción Natural:** Elimina la barrera de entrada tecnológica mediante instrucciones por voz claras y sencillas.

#### 2. 🛡️ Seguridad Telefónica Activa (Anti-Spam)
Las personas mayores son el objetivo principal de estafas telefónicas.
* **Role Manager:** La app toma el control total de la telefonía del sistema (InCallService).
* **Lista Blanca Estricta:** Solo pueden entrar llamadas de números explícitamente guardados en la base de datos local.
* **Bloqueo Silencioso:** Cualquier número desconocido es rechazado automáticamente en segundo plano. El teléfono ni siquiera suena, evitando ansiedad y confusión.

#### 3. 👁️ Interfaz Hiper-Accesible (Modo Kiosco)
Diseñada para suplir carencias motoras y visuales:
* **Botones Gigantes y Fotos Reales:** Eliminamos la abstracción de leer nombres. Puchi ve la cara de su hijo y pulsa.
* **Navegación Lineal:** Sin menús anidados. Todo está a un toque de distancia.
* **Prevención de Errores:** Ocultación de barras de sistema y notificaciones para evitar salidas accidentales de la app.

#### 4. 💊 Salud y Fiabilidad Técnica
* **Recordatorios de Voz:** Mensajes grabados por familiares ("Mamá, tómate la pastilla azul"). Es mucho más efectivo escuchar la voz de un nieto que un pitido de alarma genérico.
* **Ingeniería Robusta (Doze Mode):** Implementación avanzada de `AlarmManager` para garantizar que las alarmas médicas suenen incluso cuando el sistema operativo intenta hibernar la app para ahorrar batería.

#### 5. 📺 Protección de Hardware (OLED)
Al ser una aplicación *Always-On* (siempre encendida para evitar el desbloqueo), implementamos un **Salvapantallas Inteligente**:
* **Efecto Yoyó:** Las burbujas de contactos flotan suavemente para evitar el quemado de píxeles (burn-in).
* **Sensores:** Uso del acelerómetro para detectar cuándo Puchi coge el teléfono y despertar la interfaz inmediatamente.

---

### 📸 Galería de la Interfaz

| Menú Principal | Llamada Segura | Panel de Control | El Asistente |
|:---:|:---:|:---:|:---:|
| <img src="ruta/a/captura_menu.png" width="200"> | <img src="ruta/a/captura_llamada.png" width="200"> | <img src="ruta/a/captura_admin.png" width="200"> | <img src="ruta/a/captura_asistente.png" width="200"> |

*(Sustituir rutas por imágenes reales)*

---

### 🔧 Arquitectura Técnica
Este proyecto demuestra un dominio avanzado del ecosistema Android:
* **Lenguaje:** Kotlin puro.
* **UI:** Jetpack Compose (Modern Android Development).
* **Persistencia:** Room Database (SQLite) para privacidad total de datos.
* **Arquitectura:** MVVM (Model-View-ViewModel) con Inyección de Dependencias manual.
* **Android Services:** Implementación de `InCallService` (Telecom), `BroadcastReceiver` (Alarmas) y `SensorManager`.

---

### 📄 Licencia y Uso
Este proyecto se distribuye bajo la licencia **Creative Commons Atribución-NoComercial 4.0 Internacional (CC BY-NC 4.0)**.

Esta licencia ha sido elegida para proteger la naturaleza social del proyecto:
1.  **Reconocimiento:** Debes citar al autor original.
2.  **No Comercial:** Queda terminantemente prohibido lucrarse con este software diseñado para ayudar a personas vulnerables.

---
---

## 🇺🇸 English

### 🧠 Project Philosophy: More Than an App
**Puchi's Reminders** is not just a "Senior Launcher." It is a software engineering project applied to a specific neurodegenerative need.

It was created to restore autonomy to **Puchi**, my **85-year-old** grandmother, diagnosed with early-stage **Alzheimer's**. My experience living with this disease has taught me that the issue isn't just "forgetting numbers"; the issue is **functional agnosia**: Puchi forgets what objects are for.

If the phone doesn't interact with her, her brain stops perceiving it as a communication tool and categorizes it as a "digital photo frame" where she watches her grandchildren pass by.

**This app transforms the passive Smartphone into an Active Companion.**

### ✨ Key Innovation & Features

#### 1. 🤖 The Virtual Assistant: An "Anchor to Reality"
This is the system's most critical feature. It is not a gimmick; it is a tool for **constant cognitive reinforcement**.
* **Purpose Reminder:** Periodically (configurable), the assistant "wakes up" and verbally reminds Puchi: *"Hello, I am your phone, I am here for you to call your children."* Without this stimulus, she forgets the device's function.
* **Companionship:** An animated avatar with lipsync reduces the coldness of interacting with a machine.

#### 2. 🛡️ Active Phone Security (Anti-Spam)
* **Role Manager:** The app takes full control of system telephony via `InCallService`.
* **Strict Whitelist:** Only calls from numbers explicitly saved in the local database are allowed through.
* **Silent Blocking:** Unknown numbers are automatically rejected in the background to prevent anxiety and confusion.

#### 3. 👁️ Hyper-Accessible Interface (Kiosky Mode)
* **Giant Buttons & Real Photos:** We remove the abstraction of reading names. Puchi sees her son's face and taps.
* **Linear Navigation:** No nested menus.
* **Hardware Protection:** An interactive screensaver with floating elements prevents OLED burn-in while the app remains always-on.

#### 4. 💊 Health & Reliability
* **Voice Reminders:** Messages recorded by family members ("Mom, take the blue pill").
* **Robust Engineering:** Advanced `AlarmManager` implementation ensures medical alarms ring even in "Doze Mode" (battery saving).

### 🔧 Tech Stack
* **Language:** Kotlin.
* **UI:** Jetpack Compose.
* **Architecture:** MVVM + Room Database.
* **Services:** Telecom, BroadcastReceivers, Sensors.

### 📄 License
Licensed under **Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)**. Commercial use is strictly prohibited.

Copyright (c) 2026 [Aarón Benítez]
