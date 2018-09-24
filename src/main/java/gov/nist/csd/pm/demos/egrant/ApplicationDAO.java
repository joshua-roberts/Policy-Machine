package gov.nist.csd.pm.demos.egrant;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.demos.egrant.Email;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    private Connection conn;

    public ApplicationDAO(DatabaseContext ctx) throws DatabaseException {
        /*try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + "localhost" + ":" + 3306 + "/" + "pmwsdb", "root", "password");
            System.out.println("Connected to MySQL");
        } catch(Exception e) {
            throw new DatabaseException(e.hashCode(), e.getMessage());
        }*/
    }

    public List<Email> getEmails(List<Long> emailIDs) throws DatabaseException {
        try {
            List<Email> emails = new ArrayList<>();
            if (emailIDs.size() == 0 ) {
                return emails;
            }
            List<Integer> attachments = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String emailIDList = "";
            Integer attachmentID = 0;
            String emailSql = "SELECT email_node_id, sender, recipient, email_timestamp, email_subject FROM email_detail WHERE email_node_id in (";
            String attachmentSql = " SELECT attachment_node_id FROM email_attachment WHERE email_node_id = ";
            for(Long emailID : emailIDs) {
                emailIDList += emailID + ",";
            }
            emailIDList = emailIDList.substring(0,emailIDList.length()-2)+")";
            emailSql+=emailIDList;
            System.out.println("emailSql is " + emailSql);
            ResultSet rs1 = stmt.executeQuery(emailSql);
            System.out.println(rs1.getFetchSize());
            Email email = new Email();
            while (rs1.next()) {
                email.setEmailNodeID(rs1.getInt(1));
                email.setSender(rs1.getString(2));
                email.setRecipient(rs1.getString(3));
                email.setTimestamp(rs1.getTimestamp(4));
                email.setEmailSubject(rs1.getString(5));
                ResultSet rs2 = stmt.executeQuery(attachmentSql);
                while (rs2.next()) {
                    attachmentID = rs1.getInt(1);
                }
                attachments.add(attachmentID);
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
            String emailSql = "INSERT INTO email_detail(email_node_id,sender,recipient,email_timestamp,email_subject,email_body) VALUES (" +
                    email.getEmailNodeID() + "," +
                    "'" + email.getSender() + "'" + "," +
                    "'" + email.getRecipient() + "'" + "," +
                    "'" + email.getTimestamp() + "'" + "," +
                    "'" + email.getEmailSubject() + "'" + "," +
                    "'" + email.getEmailBody() + "'" + ")";
            System.out.println(emailSql);
            stmt.executeUpdate(emailSql);
            String attachmentSql = " INSERT INTO email_attachment(email_node_id, attachment_node_id) VALUES (" + email.getEmailNodeID() + ",";
            if (email.getAttachments() != null) {
                for (Integer attachmentID : email.getAttachments()) {
                    stmt.executeUpdate(attachmentSql + attachmentID + ")");
                }
            }
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }
}
