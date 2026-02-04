# 👵 Recordatorios de Puchi

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

> **Una solución de accesibilidad digital para combatir la brecha tecnológica en la tercera edad.**
> *A digital accessibility solution bridging the tech gap for the elderly.*

![Logo App](main/ic_launcher-playstore.png)

---

## 🇪🇸 Español

### 📖 Contexto del Proyecto
Este proyecto nace de una necesidad social urgente y personal: devolver la autonomía comunicativa a **Puchi**, mi abuela de **85 años**.

Aunque todavía es una persona independiente y se vale por sí misma, lleva muchos años diagnosticada de **Alzheimer** en fase inicial. Esta larga convivencia con la enfermedad me ha permitido adquirir un **conocimiento experto** sobre las barreras cognitivas reales que enfrentan estos pacientes.

He podido adaptar el proyecto milimétricamente a las necesidades de una persona con este tipo de deterioro, diseñando soluciones específicas para suplir la pérdida de facultades como el uso del teléfono fijo convencional (olvido de números, incapacidad para seguir secuencias, etc.). "Recordatorios de Puchi" transforma un smartphone en una herramienta de asistencia cognitiva real, validada por la experiencia diaria.

### ✨ Características Principales

1.  **Interfaz Adaptada (UI Accesible):**
    * Diseño Gigante: Botones de gran tamaño, alto contraste y tipografías legibles.
    * Navegación Simplificada: Sin menús complejos. Solo "Llamar" y "Ver más".
    * Fotos Reales: Los contactos se identifican por fotos grandes para facilitar el reconocimiento cognitivo.

2.  **Seguridad Telefónica (Anti-Spam):**
    * La app actúa como el **Teléfono Predeterminado (Default Dialer)**.
    * **Lista Blanca Estricta:** Solo entran llamadas de números guardados en la agenda de la app.
    * **Bloqueo Automático:** Cualquier número desconocido es rechazado automáticamente sin que el teléfono suene, protegiendo al usuario de fraudes.

3.  **Asistente Virtual "Puchi" 🤖:**
    * No es solo un menú, es compañía. Un avatar animado que parpadea, "habla" (lipsync) y saluda.
    * Explica al usuario qué puede hacer mediante voz.

4.  **Recordatorios de Medicación:**
    * Sistema de alarmas de voz grabadas por familiares ("Mamá, tómate la pastilla azul").
    * Fiabilidad total incluso en reposo.

### 📸 Capturas de Pantalla

| Menú Principal | Llamada Entrante | Panel de Admin | Asistente Virtual |
|:---:|:---:|:---:|:---:|
| <img src="ruta/a/captura_menu.png" width="200"> | <img src="ruta/a/captura_llamada.png" width="200"> | <img src="ruta/a/captura_admin.png" width="200"> | <img src="ruta/a/captura_asistente.png" width="200"> |

*(Nota: Sustituir rutas por las imágenes reales)*

### 🛠️ Retos Técnicos y Soluciones

Durante el desarrollo, nos enfrentamos a desafíos críticos relacionados con el hardware y la usabilidad en personas mayores:

#### 🔋 1. El Reto de la Batería y las Alarmas (Doze Mode)
* **Problema:** Android moderno "mata" los procesos en segundo plano para ahorrar batería. Las alarmas de medicación fallaban si el móvil llevaba horas quieto.
* **Solución:** Implementación de `AlarmManager` con `setExactAndAllowWhileIdle`. Esto garantiza que el sistema "despierte" el procesador para reproducir el audio vital, ignorando el ahorro de energía.

#### 📺 2. Protección de Pantalla (Burn-in en OLED)
* **Problema:** Al ser una app diseñada para estar siempre encendida (Always-on) para evitar bloqueos, los elementos fijos quemarían la pantalla OLED.
* **Solución:** Desarrollo de un **Salvapantallas Interactivo**.
    * **Pixel Shifting:** Las burbujas de contactos nunca están quietas; flotan suavemente (animación Yoyó) para rotar los píxeles activos.
    * **Ciclos de Fade:** Los elementos aparecen y desaparecen.
    * **Sensores:** Uso del acelerómetro para despertar la pantalla automáticamente al levantar el móvil.

#### 🔊 3. Accesibilidad Auditiva
* **Problema:** El volumen del auricular estándar es demasiado bajo para personas con hipoacusia.
* **Solución:** Enrutamiento forzado de audio al **Altavoz (Speakerphone)** en todas las llamadas y avisos mediante `AudioManager` y `CallAudioState`.

### 🔧 Stack Tecnológico
* **Lenguaje:** Kotlin.
* **UI:** Jetpack Compose (Declarativa).
* **Base de Datos:** Room (SQLite) para persistencia local y privacidad.
* **Arquitectura:** MVVM (Model-View-ViewModel).
* **Servicios:** `InCallService` (Telecom), `BroadcastReceiver` (Alarmas), `SensorManager`.

### 📄 Licencia
Este proyecto se distribuye bajo la licencia **Creative Commons Atribución-NoComercial 4.0 Internacional (CC BY-NC 4.0)**.
Usted es libre de usar y modificar este código siempre que **mencione al autor** y **no lo utilice con fines comerciales**.

---

## 🇺🇸 English

### 📖 Project Context
This project was born from an urgent personal and social need: restoring communicative autonomy to **Puchi**, my **85-year-old** grandmother.

Although she is still independent, Puchi has been diagnosed with early-stage **Alzheimer's** for many years. Living closely with this condition has given me **deep, first-hand expertise** in understanding the specific cognitive barriers these patients face daily.

This practical experience allowed me to tailor the project precisely to the needs of someone with this type of cognitive impairment, designing specific solutions to overcome challenges such as using a standard landline (forgetting numbers, inability to follow dialing sequences, etc.). "Puchi's Reminders" transforms a smartphone into a true cognitive assistive tool, validated by daily experience.

### ✨ Key Features

1.  **Adaptive Interface (Accessible UI):**
    * Giant Design: Oversized buttons, high contrast.
    * Simplified Navigation: No complex menus.
    * Real Photos: Contacts are identified by large photos to reduce cognitive load.

2.  **Phone Security (Anti-Spam):**
    * Acts as the **Default Dialer**.
    * **Strict Whitelist:** Only calls from contacts saved in the database are allowed.
    * **Auto-Blocking:** Unknown numbers are automatically rejected to protect the user from fraud.

3.  **Virtual Assistant "Puchi" 🤖:**
    * Provides companionship via an animated avatar with lipsync and friendly gestures.
    * Offers voice guidance on how to use the device.

4.  **Medication Reminders:**
    * Voice alarms recorded by family members.
    * Reliable triggering even in deep sleep mode.

### 🛠️ Technical Challenges & Solutions

#### 🔋 1. Battery & Alarm Reliability (Doze Mode)
* **Problem:** Android aggressively kills background processes. Medication alarms failed when the phone was idle.
* **Solution:** Used `AlarmManager` with `setExactAndAllowWhileIdle`. This ensures the device wakes up to play the critical audio, bypassing battery optimization.

#### 📺 2. Screen Burn-in Protection (OLED)
* **Problem:** As an "Always-on" app, static text would burn into the OLED screen.
* **Solution:** **Interactive Screensaver**.
    * **Pixel Shifting:** Contact bubbles float gently (Yoyo animation) to shift active pixels.
    * **Fade Cycles:** Elements fade in and out.
    * **Sensors:** Uses the accelerometer to wake the app upon pickup.

#### 🔊 3. Auditory Accessibility
* **Problem:** Standard earpiece volume is too low for users with hearing loss.
* **Solution:** Forced audio routing to the **Loudspeaker** for all calls and reminders using `AudioManager`.

### 📄 License
This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0)**.
You are free to use and adapt this code as long as you **attribute the author** and **do not use it for commercial purposes**.

---
Copyright (c) 2026 [Aarón Benítez]
