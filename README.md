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
        <b>Una prótesis cognitiva digital y física para la tercera edad.</b>
      </p>
    </td>
  </tr>
</table>

---

## 🇪🇸 Español

### 📖 Contexto del Proyecto: La Realidad de Puchi
El centro de este proyecto es **Puchi**, mi abuela de **85 años**.
Puchi lleva **muchos años diagnosticada de Alzheimer**. Su vida se basa estrictamente en la **rutina**; su cerebro funciona como un reloj de costumbres.

Sin embargo, **si algo se sale de su guion diario, para ella no existe.** Hemos comprobado que la única forma de que Puchi retenga una información nueva es mediante la **repetición constante ("machaque")**: hay que recordarle el evento cada poco tiempo para que se fije en su memoria a corto plazo.

### 🧩 El Problema: Presencia vs. Autonomía
La familia no puede estar físicamente a su lado cada 2 horas para repetirle: *"Abuela, hoy viene el médico"*. Aquí es donde entra la aplicación como una **prótesis de memoria**.

La app se ha diseñado para cubrir ese hueco, basándose en dos pilares:
1.  **Accesibilidad Física (Acelerómetro):** Gracias al sensor de movimiento, **al levantar el teléfono, este se despierta y está listo**.
2.  **El Dilema del "Siempre Encendido":**
    * *El problema:* Mantener una imagen fija quemaría la pantalla OLED.
    * *La solución:* Un **Asistente Virtual** que actúa como salvapantallas dinámico, protegiendo el hardware y recordando a Puchi verbalmente qué es ese aparato.

---

### 🔌 Adaptación de Hardware y Batería
Un software accesible no sirve de nada si el usuario no puede mantener el dispositivo encendido. Nos encontramos con dos barreras físicas críticas:

1.  **La Carga de Batería:**
    * **Problema:** Puchi ha perdido la motricidad fina necesaria para conectar un cable USB-C convencional. Intentarlo le genera frustración y acaba rompiendo el conector. Además, el dispositivo utilizado **no dispone de carga inalámbrica nativa**.
    * **Solución:** Hemos implementado una **Base de Carga Magnética USB-C**. Esto convierte la acción de cargar en un gesto simple de "dejar caer" el teléfono sobre la base, sin necesidad de apuntar ni hacer fuerza.

2.  **Gestión de Energía (Doze Mode):**
    * **Problema:** Al ser un dispositivo antiguo reutilizado, la batería se degrada rápido, y Android intenta "matar" la app para ahorrar energía, silenciando las alarmas médicas.
    * **Solución:** Uso de `AlarmManager` con permisos de alta prioridad (`SCHEDULE_EXACT_ALARM`) para garantizar que el procesador despierte para los avisos médicos, ignorando el ahorro de batería.

---

### ✨ Funcionalidades Clave

#### 1. 🤖 El Asistente Virtual: Un "Ancla a la Realidad"
* **Recordatorio de Propósito:** El asistente "despierta" y le recuerda verbalmente: *"Hola, soy tu teléfono, estoy aquí para que llames a tus hijos"*. Sin este estímulo, ella olvida la función del objeto.
* **Compañía:** Un avatar animado con sincronización labial (*lipsync*) reduce la sensación de soledad.

#### 2. 🛡️ Seguridad Telefónica (Role Manager)
* **Lista Blanca Estricta:** La app toma el control de la telefonía. Solo entran llamadas de números guardados.
* **Bloqueo Silencioso:** Cualquier número desconocido es rechazado automáticamente en segundo plano. El teléfono ni siquiera suena, evitando ansiedad y posibles estafas.

#### 3. 👁️ Interfaz Hiper-Accesible (Modo Kiosco)
* **Botones Gigantes y Fotos Reales:** Eliminamos la abstracción de leer nombres. Puchi ve la cara de su hijo y pulsa.
* **Navegación Lineal:** Sin menús anidados. Todo está a un toque de distancia.

---

### 🔧 Stack Tecnológico y Herramientas
* **Dispositivo:** Smartphone Android (Reutilizado).
* **Entorno de Desarrollo:** Android Studio Ladybug.
* **Lenguaje:** Kotlin.
* **UI:** Jetpack Compose.
* **Base de Datos:** Room (SQLite).
* **Hardware Adicional:** Adaptador magnético USB-C + Base de carga impresa/adaptada.

---

### 📄 Licencia
Este proyecto se distribuye bajo la licencia **Creative Commons Atribución-NoComercial 4.0 Internacional (CC BY-NC 4.0)**.
* **Atribución:** Debes citar al autor original.
* **No Comercial:** Prohibido lucrarse con este software de ayuda social.

---
---

## 🇺🇸 English

### 📖 Project Context: Puchi's Reality
At the core of this project is **Puchi**, my **85-year-old** grandmother, diagnosed with **Alzheimer's disease** many years ago.
Her life is strictly based on **routine**. If an event falls outside her daily script, **it simply does not exist for her.** We have learned that the only way for Puchi to retain information is through **constant repetition**: the event must be mentioned repeatedly to stick in her short-term memory.

### 🧩 The Problem: Presence vs. Autonomy
The family cannot be physically present every 2 hours to repeat reminders. This app serves as a **memory prosthesis**, designed on two pillars:
1.  **Physical Accessibility:** Thanks to the accelerometer, simply **picking up the phone wakes it up**.
2.  **The "Always-On" Dilemma:** A static image would burn the OLED screen. We created a **Virtual Assistant** that acts as a dynamic screensaver, protecting the hardware while verbally reminding Puchi of the device's purpose.

---

### 🔌 Hardware Adaptation & Battery
Accessible software is useless if the user cannot keep the device powered. We faced two critical physical barriers:

1.  **Battery Charging:**
    * **Problem:** Puchi lacks the fine motor skills to plug in a standard USB-C cable, causing frustration. Furthermore, the reused device **lacks native wireless charging**.
    * **Solution:** We implemented a **Magnetic USB-C Charging Dock**. This turns charging into a simple "drop and charge" action, removing the need to plug in cables.

2.  **Power Management (Doze Mode):**
    * **Problem:** Android aggressively kills background apps to save battery, silencing medical alarms.
    * **Solution:** Implementation of `AlarmManager` with high-priority permissions to ensure the processor wakes up for medical alerts, bypassing battery optimization.

---

### ✨ Key Features

#### 1. 🤖 The Virtual Assistant: An "Anchor to Reality"
* **Purpose Reminder:** The assistant verbally reminds her: *"Hello, I am your phone, I am here for you to call your children."*
* **Companionship:** An animated avatar with lipsync reduces the feeling of interacting with a cold machine.

#### 2. 🛡️ Phone Security (Role Manager)
* **Strict Whitelist:** The app controls telephony. Only calls from saved numbers are allowed.
* **Silent Blocking:** Unknown numbers are automatically rejected in the background to prevent scams and anxiety.

#### 3. 👁️ Hyper-Accessible Interface (Kiosk Mode)
* **Giant Buttons & Real Photos:** No reading required. Puchi sees a face and taps.
* **Linear Navigation:** No nested menus.

---

### 🔧 Tech Stack & Tools
* **Device:** Android Smartphone (Reused).
* **IDE:** Android Studio Ladybug.
* **Language:** Kotlin.
* **UI:** Jetpack Compose.
* **Database:** Room (SQLite).
* **Hardware Add-on:** Magnetic USB-C Adapter + Dock.

---

### 📄 License
Licensed under **Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)**.
* **Attribution:** You must credit the author.
* **Non-Commercial:** Profiting from this social aid software is prohibited.

Copyright (c) 2026 [Aarón Benítez]
