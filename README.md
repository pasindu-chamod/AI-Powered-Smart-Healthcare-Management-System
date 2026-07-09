# 🏥 Healthcare Management System

A desktop **Healthcare Management System** built with **Java Swing** and **MySQL**, featuring role-based dashboards for Admins, Doctors, and Patients — plus an **AI-powered Symptom Checker** built on a from-scratch **Naive Bayes Classifier**.

---

## ✨ Features

- 🔐 **Role-based Authentication** — separate login/registration for Patients, Doctors, and Admins
- 📅 **Appointment Booking** — patients book appointments with doctors; doctors confirm/complete/cancel
- 💊 **Prescriptions** — doctors write prescriptions linked to a diagnosed disease and its recommended medicines
- 🧪 **Lab Reports** — track and update lab test results per patient
- 🤖 **AI Symptom Checker** — select symptoms and get a predicted disease + recommended medicines, powered by a Naive Bayes classifier trained on real disease/symptom data stored in MySQL
- 📊 **Admin Dashboard** — live system-wide statistics (patients, doctors, appointments, prescriptions, AI predictions, lab reports)
- 👨‍⚕️ **Doctor Dashboard** — per-doctor stats (appointments, distinct patients, prescriptions, lab reports)

---

## 🏗️ Architecture

The project follows a clean **layered architecture**:

```
UI Layer (Java Swing)
      ↓
Service Layer (business logic)
      ↓
DAO Layer (JDBC / SQL queries)
      ↓
MySQL Database
```

| Layer | Package | Responsibility |
|---|---|---|
| UI | `healthcare.ui.*` | Swing panels/frames for Admin, Doctor, Patient, Auth |
| Service | `healthcare.service` | Business logic (auth, appointments, prescriptions) |
| DAO | `healthcare.dao`, `healthcare.dao.impl` | Database access (JDBC) |
| Model | `healthcare.model` | Plain data classes mapped to DB tables |
| AI | `healthcare.ai` | Naive Bayes classifier for symptom-based disease prediction |
| Util | `healthcare.util` | DB connection singleton, shared UI styling helpers |

---

## 🤖 AI Symptom Checker

The AI module implements a **Naive Bayes Classifier from scratch** (no external ML library):

1. **Training data** is loaded from the `disease_symptom_training` table (18 diseases, 700+ training cases)
2. **Prior probability** `P(Disease)` and **likelihood** `P(Symptom | Disease)` are computed from counts, with **Laplace smoothing** to avoid zero-probability issues
3. Given a set of selected symptoms, the classifier applies the **MAP (Maximum A Posteriori) rule**:

   ```
   Predicted Disease = argmax over all diseases of  P(X | Disease) × P(Disease)
   ```
4. The full `P(X|Ci) × P(Ci)` comparison across every disease can be viewed via the **"Show Calculation"** button for transparency.

---

## 🗄️ Database Schema

MySQL database: `healthcare_db`

| Table | Purpose |
|---|---|
| `users` | Login credentials + role (PATIENT / DOCTOR / ADMIN) |
| `patients` | Patient profile data |
| `doctors` | Doctor profile data + approval status |
| `appointments` | Appointment bookings |
| `prescriptions` | Doctor-issued prescriptions |
| `lab_reports` | Lab test records |
| `diseases` | Disease reference data (description, severity) |
| `medicines` | Medicine reference data, linked to diseases |
| `disease_symptom_training` | Naive Bayes training dataset |
| `ai_predictions` | AI Symptom Checker prediction history |

A full setup script (`healthcare_db_setup.sql`) with schema + seed data is provided for local setup.

---

## 🛠️ Tech Stack

- **Language:** Java (Swing for GUI)
- **Database:** MySQL (via [XAMPP](https://www.apachefriends.org/))
- **JDBC Driver:** MySQL Connector/J
- **Algorithm:** Naive Bayes Classifier (custom implementation)

---

## 🚀 Getting Started

### Prerequisites
- JDK 8 or later
- MySQL Server (XAMPP recommended)
- An IDE (NetBeans / IntelliJ / Eclipse) or `javac`/`java` on the command line

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/HealthcareSystem.git
   cd HealthcareSystem
   ```

2. **Start MySQL** (e.g. via XAMPP) and create the database:
   ```bash
   mysql -u root -p < healthcare_db_setup.sql
   ```
   Or run the script's contents in phpMyAdmin's SQL tab.

3. **Configure the database connection**, if needed, in:
   ```
   src/healthcare/util/DatabaseConnection.java
   ```

4. **Add the MySQL Connector/J JAR** (`lib/mysql-connector-j-9.5.0.jar`) to your project's classpath.

5. **Compile and run**
   ```bash
   javac -cp "lib/mysql-connector-j-9.5.0.jar" -d bin src/healthcare/**/*.java src/healthcaresystem/*.java
   java -cp "bin:lib/mysql-connector-j-9.5.0.jar" healthcaresystem.HealthcareSystem
   ```
   *(On Windows, use `;` instead of `:` as the classpath separator.)*

   Or simply open the project in your IDE and run `healthcaresystem.HealthcareSystem`.

### Default Login Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Doctor | `dr.silva` | `doctor123` |
| Patient | `kasun.p` | `patient123` |

*(See the seed data script for the full list of demo accounts.)*

---

## 📁 Project Structure

```
HealthcareSystem/
├── lib/                          # External JARs (MySQL Connector/J)
├── resources/                    # Static assets (images)
├── src/
│   ├── healthcaresystem/         # Application entry point (main)
│   └── healthcare/
│       ├── ai/                   # Naive Bayes classifier
│       ├── dao/                  # DAO interfaces
│       ├── dao/impl/             # DAO implementations (JDBC/SQL)
│       ├── model/                # Data model classes
│       ├── service/               # Business logic layer
│       ├── ui/
│       │   ├── auth/             # Login/Register
│       │   ├── admin/            # Admin dashboard panels
│       │   ├── doctor/           # Doctor dashboard panels
│       │   └── patient/          # Patient dashboard panels
│       └── util/                 # DB connection, UI helpers
└── LICENSE
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
