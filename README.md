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

## 🇪🇸 Español

### 📖 Contexto del Proyecto: La Realidad de Puchi
El centro de este proyecto es **Puchi**, mi abuela de **85 años**.
Puchi lleva **muchos años diagnosticada de Alzheimer**. No es una condición nueva, lo que nos ha permitido estudiar a fondo su comportamiento. Su vida se basa estrictamente en la **rutina**; su cerebro funciona como un reloj de costumbres.

Sin embargo, el problema surge cuando ocurre algo fuera de esa rutina (una cita médica, una visita, un cambio de medicación). **Si algo se sale de su guion diario, para ella no existe.** Hemos comprobado que la única forma de que Puchi retenga levemente una información nueva es mediante la **repetición constante ("machaque")**: hay que recordarle el evento cada poco tiempo para que se fije mínimamente en su memoria a corto plazo.

### 🧩 El Problema: Presencia vs. Autonomía
La familia no puede estar físicamente a su lado cada 2 o 3 horas para repetirle: *"Abuela, hoy viene el médico"* o *"Abuela, recuerda llamar a tu hijo"*. Aquí es donde entra la aplicación como una **prótesis de memoria**.

La app se ha diseñado para cubrir ese hueco cuando nosotros no estamos, basándose en dos pilares técnicos:

1.  **Accesibilidad Física (Acelerómetro):** Puchi no debe lidiar con botones de bloqueo. Gracias al sensor de movimiento, **simplemente al levantar el teléfono, este se despierta y está listo**.
2.  **El Dilema del "Siempre Encendido" (Salvapantallas):**
    * *La idea ideal:* Para una persona con Alzheimer, lo ideal sería que el teléfono estuviera **siempre encendido con la misma imagen fija** (las fotos de sus hijos), como un cuadro, para que siempre supiera qué hacer.
    * *El problema técnico:* Mantener una imagen estática al 100% de brillo **quemaría la pantalla OLED (Burn-in)** del dispositivo en cuestión de días.
    * *La solución (El Asistente):* Hemos desarrollado un sistema híbrido. Un **Asistente Virtual** que actúa como salvapantallas. Se mueve, aparece y desaparece (evitando quemar la pantalla), pero cumple la función cognitiva: **le habla y le recuerda constantemente qué es ese aparato y qué puede hacer con él** ("Soy tu teléfono, puedes llamar a...").

---

### ✨ Innovación y Funcionalidades Clave

#### 1. 🤖 El Asistente Virtual: Un "Ancla a la Realidad"
Esta es la funcionalidad más crítica del sistema. No es un adorno; es una herramienta de **refuerzo cognitivo constante**.
* **Recordatorio de Propósito:** Cada cierto tiempo (configurable), el asistente "despierta" y le recuerda verbalmente a Puchi: *"Hola, soy tu teléfono, estoy aquí para que llames a tus hijos"*. Sin este estímulo, ella olvida que el dispositivo sirve para llamar.
* **Compañía y Empatía:** Un avatar animado con sincronización labial (*lipsync*) y gestos de saludo reduce la sensación de interactuar con una máquina fría.

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

---

### 📸 Galería de la Interfaz

| Menú Principal | Llamada Segura | Panel de Control | El Asistente |
|:---:|:---:|:---:|:---:|
| <img src="ruta/a/captura_menu.png" width="200"> | <img src="ruta/a/captura_llamada.png" width="200"> | <img src="ruta/a/captura_admin.png" width="200"> | <img src="ruta/a/captura_asistente.png" width="200"> |

*(Sustituir rutas por las imágenes reales)*

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

### 📖 Project Context: Puchi's Reality
At the core of this project is **Puchi**, my **85-year-old** grandmother.
Puchi was diagnosed with **Alzheimer's disease many years ago**. This is not a new condition, which has allowed us to deeply study her behavior. Her life is strictly based on **routine**; her brain operates on habit.

The problem arises when something disrupts that routine (a doctor's appointment, a visit, a medication change). **If an event falls outside her daily script, it simply does not exist for her.** We have learned that the only way for Puchi to vaguely retain new information is through **constant repetition ("hammering")**: the event must be mentioned repeatedly every short interval for it to stick in her short-term memory.

### 🧩 The Problem: Presence vs. Autonomy
The family cannot be physically present every 2 or 3 hours to repeat: *"Grandma, the doctor is coming today"* or *"Grandma, remember to call your son."* This app serves as a **memory prosthesis** to fill that gap.

The design relies on two key pillars based on her needs:
1.  **Physical Accessibility (Accelerometer):** Puchi shouldn't struggle with lock buttons. Thanks to the motion sensor, **simply picking up the phone wakes it up**, ready to use.
2.  **The "Always-On" Dilemma (Screensaver):**
    * *The Ideal Scenario:* For someone with Alzheimer's, the phone should ideally be **always on with a static image** (her children's photos), like a poster, so she always knows what to do.
    * *The Technical Constraint:* Keeping a static image at high brightness would cause **OLED Screen Burn-in** within days, destroying the hardware.
    * *The Solution (The Assistant):* We developed a hybrid system. A **Virtual Assistant** acts as a screensaver. It moves, fades in and out (preventing screen damage), but fulfills the cognitive function: **it speaks to her and constantly reminds her what this device is and what she can do with it** ("I am your phone, you can call...").

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
