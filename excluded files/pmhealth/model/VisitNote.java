package gov.nist.csd.pm.demos.ndac.pmhealth.model;

public class VisitNote {
    int visitNoteId;
    String note;

    public VisitNote(int visitNoteId, String note) {
        this.visitNoteId = visitNoteId;
        this.note = note;
    }

    public int getVisitNoteId() {
        return visitNoteId;
    }

    public void setVisitNoteId(int visitNoteId) {
        this.visitNoteId = visitNoteId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}