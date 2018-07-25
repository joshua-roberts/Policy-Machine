package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.model.applications.Email;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    private Connection conn;

    public ApplicationDAO() throws DatabaseException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + "localhost" + ":" + 3306 + "/" + "pmwsdb", "root", "password");
            System.out.println("Connected to MySQL");
        }catch(Exception e){
            throw new DatabaseException(e.hashCode(), e.getMessage());
        }
    }

    public List<Email> getEmails(List<Long> emailIds) throws DatabaseException {
        try {
            List<Email> emails = new ArrayList<>();
            if (emailIds.size() == 0 ) {
                return emails;
            }
            List<Integer> attachments = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String emailIdList = "";
            Integer attachmentId = 0;
            String emailSql = "SELECT object_node_id, sender, recipient, `timestamp`, email_subject FROM email_detail WHERE object_node_id in (";
            String attachmentSql = " SELECT attachment_node_id FROM email_attachment WHERE object_node_id = ";
            for(Long emailId : emailIds) {
                emailIdList += emailId + ",";
            }
            emailIdList = emailIdList.substring(0,emailIdList.length()-2)+")";
            emailSql+=emailIdList;
            ResultSet rs1 = stmt.executeQuery(emailSql);
            Email email = new Email();
            while (rs1.next()) {
                email.setEmailNodeId(rs1.getInt(1));
                email.setSender(rs1.getString(2));
                email.setRecipient(rs1.getString(3));
                email.setTimestamp(rs1.getTimestamp(4));
                email.setEmailSubject(rs1.getString(5));
                ResultSet rs2 = stmt.executeQuery(attachmentSql);
                while (rs2.next()) {
                    attachmentId = rs1.getInt(1);
                }
                attachments.add(attachmentId);
                email.setAttachments(attachments);
                emails.add(email);
            }
            return emails;
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }


    public void saveEmail(Email email) throws DatabaseException {
        try {
            Statement stmt = conn.createStatement();
            String emailSql = "INSERT INTO email_detail(email_node_id,sender,recipient,timestamp,email_subject,email_body) VALUES (" +
                    email.getEmailNodeId() + "," +
                    email.getSender() + "," +
                    email.getRecipient() + "," +
                    email.getTimestamp() + "," +
                    email.getEmailSubject() + "," +
                    email.getEmailBody() + ")";
            stmt.executeUpdate(emailSql);
            String attachmentSql = " INSERT INTO email_attachment(email_node_id, attachment_node_id) VALUES (" + email.getEmailNodeId() + ",";
            for(Integer attachmentId: email.getAttachments()) {
                stmt.executeUpdate(attachmentSql+attachmentId+")");
            }
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }
}
