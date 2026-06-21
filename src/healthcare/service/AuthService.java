package healthcare.service;

import healthcare.dao.*;
import healthcare.dao.impl.*;
import healthcare.model.*;

public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();

    private static User currentUser;
    private static Patient currentPatient;
    private static Doctor currentDoctor;
    private String lastError = null;

    public boolean login(String username, String password, String role) {
        lastError = null;

        try {
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                lastError = "Username not found.";
                System.out.println("LOGIN FAIL: Username '" + username + "' not found");
                return false;
            }

            if (!user.getPassword().equals(password)) {
                lastError = "Wrong password.";
                System.out.println("LOGIN FAIL: Wrong password for '" + username + "'");
                return false;
            }

            if (!user.getRole().equals(role)) {
                lastError = "Wrong role. This user is a " + user.getRole() + ".";
                System.out.println("LOGIN FAIL: Role mismatch. User is " + user.getRole() + ", tried " + role);
                return false;
            }

            if (!user.isActive()) {
                lastError = "Account is disabled.";
                System.out.println("LOGIN FAIL: Account disabled for '" + username + "'");
                return false;
            }

            currentUser = user;

            if ("PATIENT".equals(role)) {
                currentPatient = patientDAO.getPatientByUserId(user.getUserId());
                if (currentPatient == null) {
                    lastError = "Patient profile not found for this user.";
                    System.out.println("LOGIN FAIL: No patient record for user_id=" + user.getUserId());
                    currentUser = null;
                    return false;
                }
                System.out.println("LOGIN OK: Patient '" + currentPatient.getFullName() + "' (ID=" + currentPatient.getPatientId() + ")");
            }

            if ("DOCTOR".equals(role)) {
                currentDoctor = doctorDAO.getDoctorByUserId(user.getUserId());
                if (currentDoctor == null) {
                    lastError = "Doctor profile not found for this user.";
                    System.out.println("LOGIN FAIL: No doctor record for user_id=" + user.getUserId());
                    currentUser = null;
                    return false;
                }
                if (!currentDoctor.isApproved()) {
                    lastError = "Your account is pending admin approval.";
                    System.out.println("LOGIN FAIL: Doctor not approved");
                    currentUser = null;
                    currentDoctor = null;
                    return false;
                }
                System.out.println("LOGIN OK: Doctor '" + currentDoctor.getFullName() + "' (ID=" + currentDoctor.getDoctorId() + ")");
            }

            if ("ADMIN".equals(role)) {
                System.out.println("LOGIN OK: Admin '" + username + "'");
            }

            return true;

        } catch (Exception e) {
            lastError = "Database error: " + e.getMessage();
            System.err.println("LOGIN ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerPatient(String username, String password,
        String fullName, String email, String phone,
        String dob, String gender, String bloodGroup) {
        try {
            if (userDAO.usernameExists(username)) {
                lastError = "Username already exists.";
                return false;
            }

            User user = new User(username, password, "PATIENT");
            if (!userDAO.addUser(user)) {
                lastError = "Failed to create user account.";
                return false;
            }

            User saved = userDAO.getUserByUsername(username);
            if (saved == null) {
                lastError = "Failed to retrieve created user.";
                return false;
            }

            Patient patient = new Patient();
            patient.setUserId(saved.getUserId());
            patient.setFullName(fullName);
            patient.setEmail(email);
            patient.setPhone(phone);
            patient.setDateOfBirth(dob);
            patient.setGender(gender);
            patient.setBloodGroup(bloodGroup);

            boolean success = patientDAO.addPatient(patient);
            if (success) {
                System.out.println("REGISTER OK: Patient '" + fullName + "'");
            } else {
                lastError = "Failed to create patient profile.";
            }
            return success;
        } catch (Exception e) {
            lastError = "Registration error: " + e.getMessage();
            System.err.println("REGISTER ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerDoctor(String username, String password,
        String fullName, String email, String phone,
        String specialization, String licenseNumber) {
        try {
            if (userDAO.usernameExists(username)) {
                lastError = "Username already exists.";
                return false;
            }

            User user = new User(username, password, "DOCTOR");
            if (!userDAO.addUser(user)) {
                lastError = "Failed to create user account.";
                return false;
            }

            User saved = userDAO.getUserByUsername(username);
            if (saved == null) {
                lastError = "Failed to retrieve created user.";
                return false;
            }

            Doctor doctor = new Doctor();
            doctor.setUserId(saved.getUserId());
            doctor.setFullName(fullName);
            doctor.setEmail(email);
            doctor.setPhone(phone);
            doctor.setSpecialization(specialization);
            doctor.setLicenseNumber(licenseNumber);

            boolean success = doctorDAO.addDoctor(doctor);
            if (success) {
                System.out.println("REGISTER OK: Doctor '" + fullName + "'");
            } else {
                lastError = "Failed to create doctor profile.";
            }
            return success;
        } catch (Exception e) {
            lastError = "Registration error: " + e.getMessage();
            System.err.println("REGISTER ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        currentUser = null;
        currentPatient = null;
        currentDoctor = null;
        lastError = null;
        System.out.println("LOGOUT OK");
    }

    public String getLastError() {
        return lastError;
    }

    public static User getCurrentUser() { return currentUser; }
    public static Patient getCurrentPatient() { return currentPatient; }
    public static Doctor getCurrentDoctor() { return currentDoctor; }
}