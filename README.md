# GlucoScan: Real-Time Sugar Analysis App 📱🩸

> **🏆 Awarded 2nd Place in the National Scientific Paper Competition (LKTIN)** organized by the Indonesian Young Scientist Association (IYSA).

GlucoScan is a mobile application designed to empower individuals with Diabetes Mellitus to manage their daily sugar intake. By utilizing device-camera text extraction, the app reads nutritional labels in real-time and calculates personalized dietary recommendations based on the user's clinical profile.

This project represents a multidisciplinary collaboration bridging clinical pharmacy logic with modern mobile software engineering.

## ✨ Key Features
* **Real-Time OCR Scanning:** Instantly captures text from physical food packaging and nutrition labels.
* **Smart Data Extraction:** Utilizes custom Regex patterns to identify and isolate sugar content values from diverse label formats.
* **Clinical Profile Integration:** Calculates a personalized daily sugar threshold and provides immediate feedback on whether the scanned item fits within the user's safe limits.
* **Modern UI/UX:** Built entirely with declarative UI for a smooth, responsive, and accessible user experience.

## 🛠️ Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Machine Learning / Vision:** Google ML Kit (Optical Character Recognition)
* **Logic & Parsing:** Regular Expressions (Regex)
* **IDE:** Android Studio

## 🚀 Getting Started

To run this project locally on your machine:

1. Clone this repository:
   `git clone https://github.com/K2Sandy/GlucoScan-Android-App.git`
2. Open the project in **Android Studio**.
3. Allow Gradle to sync and download the necessary dependencies (Kotlin, Compose, ML Kit).
4. Connect an Android device or start an emulator.
5. Click **Run** (`Shift + F10`) to build and install the app.

## 🤝 Acknowledgments
Developed as part of a multidisciplinary research initiative at Universitas Syiah Kuala (USK), integrating expertise from the Informatics and Pharmacy departments.
