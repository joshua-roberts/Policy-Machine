package gov.nist.csd.pm.demos.egrant;

import java.sql.Timestamp;
import java.util.List;

public class EmailRequest {
    private int emailNodeID;
    private String emailBody;
    private String emailSubject;
    private String recipient;
    private String sender;
    private Timestamp timestamp;
    private List<Integer> attachments;

    public int getEmailNodeID() {
        return emailNodeID;
    }

    public void setEmailNodeID(int emailNodeID) {
        this.emailNodeID = emailNodeID;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<Integer> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Integer> attachments) {
        this.attachments = attachments;
    }
}