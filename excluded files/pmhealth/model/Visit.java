package gov.nist.csd.pm.demos.ndac.pmhealth.model;

import java.io.Serializable;

public class Visit implements Serializable{
    int          visitId;
    int          patientId;
    String       admissionDate;
    String       dischargeDate;
    String       reason;
    String       result;
    VisitNote       notes;
    String       diagnosis;
    String       treatment;
    Vitals       vitals;
    Prescription prescription;

    public Visit() {

    }

    public Visit(int visitId, int patientId, String admissionDate,
                 String dischargeDate, String reason, String result,
                 VisitNote note, String diagnosis, String treatment,
                 Vitals vitals, Prescription prescription) {
        this.visitId = visitId;
        this.patientId = patientId;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.reason = reason;
        this.result = result;
        this.notes = note;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.vitals = vitals;
        this.prescription = prescription;
    }

    public int getVisitId() {
        return visitId;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    public String getReason() {
        return reason;
    }

    public String getResult() {
        return result;
    }

    public VisitNote getNotes() {
        return notes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public Vitals getVitals() {
        return vitals;
    }

    public Prescription getPrescription() {
        return prescription;
    }
}
