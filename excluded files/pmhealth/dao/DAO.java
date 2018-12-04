package gov.nist.csd.pm.demos.ndac.pmhealth.dao;

import com.google.gson.Gson;
import gov.nist.csd.pm.demos.ndac.pmhealth.model.*;
import gov.nist.csd.pm.pep.response.ApiResponse;

import java.beans.PropertyVetoException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class DAO {
    public static final String PM_HEALTH_DB = "pm_health";

    private static DAO dao;
    public static DAO getDao() throws PropertyVetoException, ClassNotFoundException, SQLException {
        if(dao == null) {
            dao = new DAO();
        }
        return dao;
    }

    public String getSessionUsername(String user) throws SQLException, IOException {
        String sql = "select username from users join sessions on users.user_id = sessions.user_id where sessions.session_id = '" + user + "'";

        SqlResults results = execute(sql);
        return results.get(0).get("username");
    }

    public String getPatientUsername(long patientId) throws IOException, SQLException {
        String sql = "select username from users join patient_info on patient_info.user_id = users.user_id where patient_info.patient_id = " + patientId;
        SqlResults results = execute(sql);

        return results.get(0).get("username");
    }

    public synchronized SqlResults executeTest(String sql) throws SQLException, IOException {
        System.out.println("executing " + sql);

        SqlRequest request = new SqlRequest(sql, "localhost", 3306, "root", "root", "employee_record");

        HttpURLConnection connection = null;
        //Create connection to proxy in policy machine
        URL url = new URL("http://localhost:8080/pm/api/proxy");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(request);
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();


        //Get Response
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();


        json = response.toString();

        gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(json, ApiResponse.class);
        String entity = String.valueOf(apiResponse.getEntity());

        System.out.println("response entity = " + entity);

        try {
            gson = new Gson();
            HashMap<String, String>[] sqlResults = gson.fromJson(entity, HashMap[].class);
            return new SqlResults(sqlResults);
        }catch (Exception e) {
            return new SqlResults(new ArrayList<>());
        }
    }


    public synchronized SqlResults execute(String sql) throws SQLException, IOException {
        System.out.println("executing " + sql);

        SqlRequest request = new SqlRequest(sql, "localhost", 3306, "root", "root", "pm_health");

        HttpURLConnection connection = null;
        //Create connection to proxy in policy machine
        URL url = new URL("http://localhost:8080/pm/api/proxy");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json");

        //create json request string
        Gson gson = new Gson();
        String json = gson.toJson(request);
        connection.setRequestProperty("Content-Length",
                Integer.toString(json.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(json);
        wr.close();


        //Get Response
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();


        json = response.toString();

        gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(json, ApiResponse.class);
        String entity = String.valueOf(apiResponse.getEntity());

        System.out.println("response entity = " + entity);

        try {
            gson = new Gson();
            HashMap<String, String>[] sqlResults = gson.fromJson(entity, HashMap[].class);
            return new SqlResults(sqlResults);
        }catch (Exception e) {
            return new SqlResults(new ArrayList<>());
        }
    }

    static class SqlResults {
        List<HashMap<String, String>> results;

        public SqlResults(List<HashMap<String, String>> results) {
            if(results != null) {
                this.results = results;
            } else {
                this.results = new ArrayList<>();
            }
        }

        public SqlResults(HashMap<String, String>[] results) {
            this.results = new ArrayList<>();
            this.results.addAll(Arrays.asList(results));
            System.out.println("results: " + this.results);
        }

        public List<HashMap<String, String>> getResults() {
            return results;
        }

        public void setResults(List<HashMap<String, String>> results) {
            if(results != null) {
                this.results = results;
            } else {
                this.results = new ArrayList<>();
            }
        }

        public HashMap<String, String> get(int i) {
            return results.get(i);
        }
    }

    private String buildSql(String user, String sql) {
        return "/*user=" + user + "*/" + sql;
    }

    public List<String> getLinks(String user) throws SQLException, IOException {
        SqlResults results = execute(buildSql(user, "select links.name from links where links.type='link'"));

        List<String> links = new ArrayList<>();
        if(results != null) {
            for (HashMap<String, String> row : results.getResults()) {
                links.add(row.get("name"));
            }
        }

        return links;
    }

    public String getHome(String user) throws SQLException, IOException {
        SqlResults results = execute(buildSql(user, "select links.name from links where links.type='home'"));

        String home = null;;
        if(results != null) {
            for (HashMap<String, String> row : results.getResults()) {
                home = (row.get("name"));
            }
        }

        return home;
    }

    public List<String> getActions(String user) throws SQLException, IOException {
        SqlResults results = execute(buildSql(user, "select links.name from links where links.type='action'"));

        List<String> actions = new ArrayList<>();
        if(results != null) {
            for (HashMap<String, String> row : results.getResults()) {
                actions.add(row.get("name"));
            }
        }

        return actions;
    }

    public void getUsers() {}

    public User getUser(String username) throws SQLException, IOException {
        String sql = "select user_id, username, password from users where username='" + username + "'";

        SqlResults results = execute(sql);

        User user = null;
        if(!results.getResults().isEmpty()) {
            user = new User(Integer.valueOf(results.get(0).get("user_id")), results.get(0).get("username"), results.get(0).get("password"));
        }

        return user;
    }

    public void createSession(int userId, String user) throws SQLException, IOException {
        String sql = "insert into sessions (user_id, session_id) values (" + userId + ", '" + user + "')";
        execute(sql);
    }

    public void deleteSession(String user) {
    }

    public List<Record> getRecords(String user) throws SQLException, IOException {
        String sql = "select patient_info.patient_id, patient_info.name, patient_info.dob, patient_info.gender, patient_info.ssn, " +
                "patient_info.race, patient_info.marital_status, patient_info.cell_phone, patient_info.work_phone, " +
                "patient_info.home_phone, patient_info.email, patient_info.address from patient_info";

        SqlResults results = execute(buildSql(user, sql));

        List<Record> records = new ArrayList<>();
        if(results != null) {
            if (!results.getResults().isEmpty()) {
                for (HashMap<String, String> row : results.getResults()) {
                    System.out.println(row);
                    records.add(new Record(Integer.valueOf(row.get("patient_id")), row.get("name"), row.get("dob"),
                            row.get("gender"), row.get("ssn"), row.get("race"),
                            row.get("marital_status"), row.get("cell_phone"), row.get("work_phone"),
                            row.get("home_phone"), row.get("email"), row.get("address")));
                }
            }
        }

        return records;
    }

    public Record getRecord(int patientId, String user) throws SQLException, IOException {
        String sql = "select patient_info.patient_id, patient_info.name, patient_info.dob, patient_info.gender, patient_info.ssn, " +
                "patient_info.race, patient_info.marital_status, patient_info.cell_phone, patient_info.work_phone, " +
                "patient_info.home_phone, patient_info.email, patient_info.address from patient_info where patient_id = " + patientId;

        SqlResults results = execute(buildSql(user, sql));

        Record record = null;
        if(!results.getResults().isEmpty()) {
            HashMap<String, String> row = results.get(0);
            System.out.println(row);
            record = new Record(Integer.valueOf(row.get("patient_id")), row.get("name"), row.get("dob"),
                    row.get("gender"), row.get("ssn"), row.get("race"),
                    row.get("marital_status"), row.get("cell_phone"), row.get("work_phone"),
                    row.get("home_phone"), row.get("email"), row.get("address"));
        }

        return record;
    }

    public Vitals getLastVitals(int patientId, String user) throws SQLException, IOException {
        String sql = "select max(visits.visit_id), visits.admission_date, height, weight, temperature, pulse, blood_pressure from vitals join visits on visits.visit_id = vitals.visit_id where visits.patient_id = " + patientId;

        SqlResults results = execute(buildSql(user, sql));

        Vitals vitals = null;
        if (!results.getResults().isEmpty()) {
            HashMap<String, String> row = results.get(0);
            vitals = new Vitals(row.get("admission_date"), (row.get("height") != null ? Integer.valueOf(row.get("height")) : -1),
                    (row.get("weight") != null ? Integer.valueOf(row.get("weight")) : -1), (row.get("temperature") != null ? Double.valueOf(row.get("temperature")) : -1),
                    (row.get("pulse") != null ? Integer.valueOf(row.get("pulse")) : -1), row.get("blood_pressure"));
        }

        return vitals;
    }

    public List<Visit> getVisits(int patientId, String user) throws SQLException, IOException {
        String sql = "select visits.visit_id, visits.patient_id, visits.admission_date, visits.discharge_date, " +
                "visits.reason, visits.result, diagnoses.diagnosis, treatments.treatment, vitals.height, vitals.weight, " +
                "vitals.temperature, vitals.pulse, vitals.blood_pressure, visit_notes.visit_note_id, visit_notes.note," +
                "prescriptions.prescription_id, prescriptions.medicine, prescriptions.dosage, prescriptions.duration" +
                " from visits " +
                "left outer join diagnoses on visits.visit_id = diagnoses.visit_id " +
                "left outer join treatments on visits.visit_id = treatments.visit_id " +
                "left outer join vitals on visits.visit_id = vitals.visit_id " +
                "left outer join visit_notes on visits.visit_id = visit_notes.visit_id " +
                "left outer join prescriptions on visits.visit_id = prescriptions.visit_id " +
                "where visits.patient_id = " + patientId;

        SqlResults results = execute(buildSql(user, sql));

        List<Visit> visits = new ArrayList<>();
        if(!results.getResults().isEmpty()) {
            for (HashMap<String, String> row : results.getResults()) {
                visits.add(new Visit(Integer.valueOf(row.get("visit_id")), Integer.valueOf(row.get("patient_id")),
                        row.get("admission_date"), row.get("discharge_date"),
                        row.get("reason"), row.get("result"),
                        new VisitNote(Integer.valueOf(row.get("visit_note_id")), row.get("note")), row.get("diagnosis"),
                        row.get("treatment"),
                        new Vitals(
                                row.get("admission_date"),
                                (row.get("height") != null ? Integer.valueOf(row.get("height")) : -1),
                                (row.get("weight") != null ? Integer.valueOf(row.get("weight")) : -1),
                                (row.get("temperature") != null ? Double.valueOf(row.get("temperature")) : -1),
                                (row.get("pulse") != null ? Integer.valueOf(row.get("pulse")) : -1),
                                row.get("blood_pressure")
                        ),
                        new Prescription(
                                Integer.valueOf(row.get("prescription_id")),
                                row.get("medicine"),
                                row.get("dosage"),
                                row.get("duration")
                        ))
                );
            }
            for(Visit visit : visits) {
                System.out.println(visit.getPrescription().getMedicine());
            }
        }

        return visits;
    }

    public void updateRecord(int patientId, Record data, String user) throws SQLException, IOException {
        String sql = "update patient_info set ";
        String sets = "";
        if(data.getName() != null) {
            sets += "name='" + data.getName() + "'";
        }
        if(data.getDob() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "dob='" + data.getDob() + "'";
        }
        if(data.getSsn() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "ssn='" + data.getSsn() + "'";
        }
        if(data.getGender() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "gender='" + data.getGender() + "'";
        }
        if(data.getRace() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "race='" + data.getRace() + "'";
        }
        if(data.getMaritalStatus() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "maritalStatus='" + data.getMaritalStatus() + "'";
        }
        if(data.getCellPhone() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "cellPhone='" + data.getCellPhone() + "'";
        }
        if(data.getWorkPhone() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "workPhone='" + data.getWorkPhone() + "'";
        }
        if(data.getHomePhone() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "homePhone='" + data.getHomePhone() + "'";
        }
        if(data.getEmail() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "email='" + data.getEmail() + "'";
        }
        if(data.getAddress() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "address='" + data.getAddress() + "'";
        }

        sql += sets + " where patient_id=" + patientId;

        System.out.println(sql);

        execute(buildSql(user, sql));
    }

    public Visit updateVisit(int patientId, int visitId, Visit data, String user) throws SQLException, IOException {
        String sql = "update visits set ";
        String sets = "";
        if(data.getAdmissionDate() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "admission_date=now()";
        }
        if(data.getDischargeDate() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "discharge_date=now()";
        }
        if(data.getReason() != null) {
            sets += (sets.isEmpty() ? "" : ", ") + "reason='" + data.getReason() + "'";
        }

        if(!sets.isEmpty()) {
            sql += sets + " where visit_id=" + visitId;
            execute(buildSql(user, sql));
        }

        //vitals
        if(data.getVitals() != null) {
            sql = "update vitals set ";
            sets += "";
            if (data.getVitals().getHeight() > 0) {
                sets += (sets.isEmpty() ? "" : ", ") + "height=" + data.getVitals().getHeight();
            }
            if (data.getVitals().getWeight() > 0) {
                sets += (sets.isEmpty() ? "" : ", ") + "weight=" + data.getVitals().getWeight();
            }
            if (data.getVitals().getTemperature() > 0) {
                sets += (sets.isEmpty() ? "" : ", ") + "temperature=" + data.getVitals().getTemperature();
            }
            if (data.getVitals().getPulse() > 0) {
                sets += (sets.isEmpty() ? "" : ", ") + "pulse=" + data.getVitals().getPulse();
            }
            if (data.getVitals().getBloodPressure() != null) {
                sets += (sets.isEmpty() ? "" : ", ") + "blood_pressure='" + data.getVitals().getBloodPressure() + "'";
            }

            if(!sets.isEmpty()) {
                sql += sets + " where visit_id = " + visitId;
                execute(buildSql(user, sql));
            }
        }

        //diagnoses
        if(data.getDiagnosis() != null) {
            sql = "update diagnoses set diagnosis='" + data.getDiagnosis() + "' where visit_id = " + visitId;
            execute(buildSql(user, sql));
        }

        //treatments
        if(data.getTreatment() != null) {
            sql = "update treatments set treatment='" + data.getTreatment() + "' where visit_id = " + visitId;
            execute(buildSql(user, sql));
        }

        //notes
        if(data.getNotes() != null) {
            sql = "update notes set note='" + data.getNotes() + "' where visit_id = " + visitId;
            execute(buildSql(user, sql));
        }

        if(data.getPrescription() != null) {
            sql = "update prescriptions set ";
            sets += "";
            if (data.getPrescription().getMedicine() != null) {
                sets += (sets.isEmpty() ? "" : ", ") + "medicine='" + data.getPrescription().getMedicine() + "'";
            }
            if (data.getPrescription().getDosage() != null) {
                sets += (sets.isEmpty() ? "" : ", ") + "dosage='" + data.getPrescription().getDosage() + "'";
            }
            if (data.getPrescription().getDuration() != null) {
                sets += (sets.isEmpty() ? "" : ", ") + "duration='" + data.getPrescription().getDuration() + "'";
            }

            if(!sets.isEmpty()) {
                sql += sets + " where visit_id = " + visitId;
                execute(buildSql(user, sql));
            }
        }

        return getVisit(visitId, user);
    }

    private Visit getVisit(int visitId, String user) throws SQLException, IOException {
        String sql = "select visits.visit_id, visits.patient_id, visits.admission_date, visits.discharge_date, " +
                "visits.reason, visits.result, diagnoses.diagnosis, treatments.treatment, vitals.height, vitals.weight, " +
                "vitals.temperature, vitals.pulse, vitals.blood_pressure,  visit_notes.visit_note_id, visit_notes.note, " +
                "prescriptions.prescription_id, prescriptions.medicine, prescriptions.dosage, prescriptions.duration " +
                "from visits " +
                "left outer join diagnoses on visits.visit_id = diagnoses.visit_id " +
                "left outer join treatments on visits.visit_id = treatments.visit_id " +
                "left outer join vitals on visits.visit_id = vitals.visit_id " +
                "left outer join visit_notes on visits.visit_id = visit_notes.visit_id " +
                "left outer join prescriptions on visits.visit_id = prescriptions.visit_id " +
                "where visits.visit_id = " + visitId;

        SqlResults results = execute(buildSql(user, sql));

        if(!results.getResults().isEmpty()) {
            HashMap<String, String> row = results.get(0);
            return new Visit(Integer.valueOf(row.get("visit_id")), Integer.valueOf(row.get("patient_id")),
                    row.get("admission_date"), row.get("discharge_date"),
                    row.get("reason"), row.get("result"),
                    new VisitNote(Integer.valueOf(row.get("visit_note_id")), row.get("note")), row.get("diagnosis"),
                    row.get("treatment"),
                    new Vitals(
                            row.get("admission_date"),
                            (row.get("height") != null ? Integer.valueOf(row.get("height")) : -1),
                            (row.get("weight") != null ? Integer.valueOf(row.get("weight")) : -1),
                            (row.get("temperature") != null ? Double.valueOf(row.get("temperature")) : -1),
                            (row.get("pulse") != null ? Integer.valueOf(row.get("pulse")) : -1),
                            row.get("blood_pressure")
                    ),
                    new Prescription(
                            Integer.valueOf(row.get("prescription_id")),
                            row.get("medicine"),
                            row.get("dosage"),
                            row.get("duration")
                    )
            );
        }

        return null;
    }

    public Patient getPatient(String user) throws SQLException, IOException {
        /*String sql = "SELECT sessions.user_id, patient_info.patient_id FROM sessions join patient_info on " +
                "patient_info.user_id = sessions.user_id where session_id = '" + user + "';";*/
        String sql = "select users.user_id, patient_info.patient_id from patient_info join users on " +
                "users.user_id=patient_info.user_id where username='" + user + "'";
        SqlResults results = execute(sql);

        Patient patient = null;
        if(!results.getResults().isEmpty()) {
            HashMap<String, String> row = results.get(0);
            patient = new Patient(Integer.valueOf(row.get("user_id")), Integer.valueOf(row.get("patient_id")));
        }

        return patient;
    }

    public Visit createVisit(int patientId, String user) throws SQLException {
        String sql = "insert into visits (patient_id, admission_date) values(" + patientId + ", now())";
        //TODO execute

        sql = "select max(visit_id), admission_date from visits";
        //TODO execute

        Visit visit = new Visit();
        return visit;
    }

    public List<Medicine> getAllMeds(String user) throws SQLException, IOException {
        String sql = "select medicines.med_id, medicines.name, medicines.dosage from medicines";
        List<Medicine> meds = new ArrayList<>();
        SqlResults results = execute(buildSql(user, sql));
        if(!results.getResults().isEmpty()) {
            for (HashMap<String, String> row : results.getResults()) {
                meds.add(new Medicine(Integer.valueOf(row.get("med_id")), row.get("name"), row.get("dosage")));
            }
        }

        return meds;
    }

    public String getMedicines(int patientId, String user) {
        return null;
    }

    public int getVisitNotesId(int visitId) throws IOException, SQLException {
        String sql = "select visit_note_id from visit_notes where visit_id =" + visitId;

        SqlResults results = execute(sql);

        if(results != null) {
            if (!results.getResults().isEmpty()) {
                HashMap<String, String> row = results.get(0);
                return Integer.parseInt(row.get("visit_note_id"));
            }
        }
        return -1;
    }
}
