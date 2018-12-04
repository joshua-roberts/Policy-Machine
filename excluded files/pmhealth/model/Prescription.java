package gov.nist.csd.pm.demos.ndac.pmhealth.model;

import java.io.Serializable;

public class Prescription implements Serializable {
    int prescriptionId;
    String medicine;
    String dosage;
    String duration;

    public Prescription(Integer prescriptionId, String name, String dosage, String duration) {
        this.prescriptionId = prescriptionId;
        this.medicine = name;
        this.dosage = dosage;
        this.duration = duration;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String name) {
        this.medicine = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
