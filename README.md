# MedTrack Pro 💊

**An Android medication management application with AI-powered medication guidance and clinician support features.**

MedTrack Pro is an Android application developed as part of FIT2081 at Monash University. The application helps patients manage their medications, track symptoms and access personalised medication guidance. It also includes a clinician dashboard for reviewing patient information and identifying patients who may require closer attention.

The project was built using **Kotlin, Jetpack Compose and MVVM architecture**, with Room Database for persistent storage. I also integrated external APIs and generative AI to explore how these technologies can support medication management.

## ✨ Key Features

### 1. Patient Account Management

- Patient registration, login and account claiming.
- Existing patients can claim their accounts using their Patient ID and phone number.
- Persistent login sessions using SharedPreferences.
- Logout functionality that clears the saved session.

### 2. Medication & Symptom Tracking

- Add and manage medication records.
- Track medication schedules and mark medications as taken.
- Medication status is saved across application restarts and resets on a new calendar day.
- Record symptoms and their severity.
- Patient information is stored locally using Room Database.

### 3. MedCoach – AI Medication Assistant 🤖

MedCoach combines an external drug information API with generative AI to help patients better understand their medications.

**Drug Information (openFDA API)**

- Search medication information by generic or brand name.
- Retrieve information such as drug purpose, warnings, dosage instructions and when to stop use.
- Select medications directly from the patient's existing medication list.
- Handle missing information and network errors.

**Personalised Medication Tips (Google Gemini)**

- Generate medication tips using the patient's recorded medications and symptom history.
- Provide short, personalised reminders and suggestions to support medication adherence.
- Save generated tips in Room Database.
- View previously generated tips through a history dialog.

### 4. Clinician Dashboard 📊

A separate clinician interface was developed to provide an overview of patient records.

The dashboard displays statistics such as:

- Total number of patients.
- Average medications per patient.
- Most commonly reported symptom.
- Average symptom severity.

It also includes Gemini-generated observations based on the aggregated dashboard statistics.

### 5. Patient Priority Radar ⭐

This was my original feature extension for the project.

I wanted to explore how patient records could be organised to help clinicians identify which patients may need attention first, instead of reviewing every record without any prioritisation.

The Patient Priority Radar calculates a review-priority score using three factors:

| Factor | Maximum Score |
|---|---|
| Number of medications | 30 |
| Symptom frequency | 30 |
| Average symptom severity | 40 |
| **Total** | **100** |

The feature includes:

- Patient overview cards displaying relevant information.
- Priority rankings based on the calculated scores.
- Filters for reviewing different patient groups.
- Short explanations for each patient's priority level.
- AI-generated summaries to support clinician review.

**Note:** The priority scores are simple rule-based indicators designed for demonstration purposes. They are not medical diagnoses or clinically validated risk predictions.

## 🏗️ Application Architecture

The application follows the **Model-View-ViewModel (MVVM)** architecture to separate the user interface from the business logic and data management.

The main data flow is:

**Jetpack Compose UI → ViewModel → Repository → DAO → Room Database**

- **Jetpack Compose:** Builds the application's user interface.
- **ViewModel:** Manages application state and handles user interactions.
- **Repository:** Coordinates data operations between ViewModels, Room Database and external APIs.
- **DAO:** Provides database operations for accessing and updating records.
- **Room Database:** Stores patient, medication, symptom and generated medication-tip records.

StateFlow and Kotlin Coroutines are used to manage reactive UI updates and asynchronous operations.

The database is also populated with fictional CSV data on the application's first launch.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Jetpack Compose | Android user interface |
| MVVM | Application architecture |
| Room Database | Local data persistence |
| Retrofit | HTTP API integration |
| Kotlin Coroutines | Asynchronous operations |
| StateFlow | Reactive UI state management |
| Google Gemini API | Generative AI medication tips and summaries |
| openFDA API | Drug label information |
| SharedPreferences | Login session persistence |
| Android Studio | Development environment |

## 🚀 Getting Started

### Requirements

- Android Studio
- Android SDK configured for the project
- Android emulator or physical device running Android 7.0 (API 24) or later
- Internet connection for external API features
- Google Gemini API key for generative AI functionality

### Installation

1. Clone or download this repository from GitHub.
2. Open the project folder in Android Studio.
3. Wait for Gradle to finish syncing.
4. Check that your Android SDK is correctly configured.
5. Open the root `local.properties` file and add your Gemini API key using the property name `apiKey`.
6. Run the application on an Android emulator or physical device.

The `local.properties` file is excluded from Git tracking to prevent local configuration and API credentials from being uploaded.

### Sample Data

The application includes fictional patient, medication and symptom datasets in the `app/src/main/assets/` directory.

These datasets are used to initialise the Room Database on the first launch so that the application can be explored without manually creating every patient record.

## 📸 Application Screenshots

Screenshots of the main application screens will be added here, including:

- Patient login and home screen
- Medication and symptom tracking
- MedCoach drug information and AI-generated tips
- Clinician dashboard
- Patient Priority Radar

## 📚 Project Outcome & Reflection

This project gave me practical experience in developing a complete Android application, from local database design and user authentication to API integration and generative AI features.

One of the more challenging parts was managing data consistently between different screens while keeping the application architecture organised. Implementing MVVM, repositories, StateFlow and coroutines helped me better understand how the different components of an Android application work together.

The Patient Priority Radar was particularly interesting to develop because it allowed me to explore a feature beyond basic medication tracking, combining database queries, rule-based scoring and AI-generated summaries.

**Final assessment result: 29.2 / 30**, including full marks for the technical interview.

### Known Limitations

- The clinician dashboard's AI insights are generated from aggregate statistics rather than detailed patient-level records, limiting their usefulness.
- The clinician login uses a hardcoded demonstration access key and is not suitable for production authentication.
- The application uses fictional patient data and has not been developed or validated for real clinical use.
- Gemini-generated content is for demonstration purposes and should not be treated as professional medical advice.

## 👩‍💻 About This Project

**Course:** FIT2081 – Mobile Application Development

**University:** Monash University

**Year:** 2026

**Developer:** Khoo Lynn

This project was developed for academic and learning purposes, with a focus on Android development, database management, software architecture and API integration.
