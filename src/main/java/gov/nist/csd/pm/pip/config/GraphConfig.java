package gov.nist.csd.pm.pip.config;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.graph.NodeType;

import java.io.*;
import java.sql.*;
import java.util.*;

public class GraphConfig {
    private Connection conn;
    int numGroups;
    int numUsers;
    int numObjects;
    int numCols;
    private String config;
    private FileWriter fw;

    public GraphConfig(int ua, int u, int o, int c){
        numGroups = ua;
        numUsers = u;
        numObjects = o;
        numCols = c;
        config = "";
    }

    /**
     * For Josh use only.  Creates a script for PmSandbox
     * @throws IOException
     */
    public void runSandboxScript() throws IOException {
        File scriptFile = new File("C:\\Users\\jnr6\\Documents\\Neo4j\\examples\\PMSandbox\\scripts\\demo1.pm");
        fw = new FileWriter(scriptFile);

        createNodeS("22", "RBAC", "PC");
        createNodeS("32", "EMP_PC", "PC");
        createNodeS(numGroups+1+"3", "Employees", "UA");
        createAssignmentS(Integer.parseInt(numGroups+1+"3"), 32);
        createNodeS(numGroups+1+"5", "Employees1", "OA");
        createAssignmentS(Integer.parseInt(numGroups+1+"5"), 32);
        createAssociationS(Integer.valueOf(numGroups+1+"3"), Integer.valueOf(numGroups+1+"5"), "['r' 'w']");

        for(int i = 1; i <= numCols; i++){
            createNodeS(numGroups+1+"5"+i+"555","col_"+i+"_5555555555","OA");
            createAssignmentS(Integer.valueOf(numGroups+1+"5"+i+"555"),Integer.valueOf(numGroups+1+"5"));
        }

        //System.out.println("Building User attributes...");
        for(int i = 1; i <= numGroups; i++){
            createNodeS(i+"3","Grp_" + i + "_UA","UA");
            createAssignmentS(Integer.valueOf(i+"3"), 22);

            for(int j = 1; j <= numUsers; j++){
                String id = String.valueOf(i) + String.valueOf(j) + "4";
                createNodeS(id,"u_" + i + "_" + j,"U");
                createAssignmentS(Integer.valueOf(id), Integer.valueOf(i+"3"));
                createAssignmentS(Integer.valueOf(id), Integer.parseInt(numGroups+1+"3"));
            }
        }

        //System.out.println("Building Objects...");

        double total = numGroups * numUsers * numObjects;

        int c = 0;
        for(int i = 1; i <= numGroups; i++){
            createNodeS(i+"5","Grp_" + i + "_OA","OA");
            createAssignmentS(Integer.valueOf(i+"5"), 22);
            createAssociationS(Integer.valueOf(i+"3"), Integer.valueOf(i+"5"), "['r']");

            for(int j = 1; j <= numUsers; j++){
                String id = String.valueOf(i) + String.valueOf(j) + "45";
                createNodeS(id,"er_" + i + "_"+j,"OA");
                createAssignmentS(Integer.valueOf(i+"5"), Integer.valueOf(id));


                for(int k = 1; k <= numObjects; k++){
                    createNodeS(id+k,"er_" + i + "_"+j+"_"+k,"O");
                    createAssignmentS(Integer.valueOf(id+k+""),Integer.valueOf(id));
                    c++;
                    double cur = (c/total);
                    int percent = (int) (cur * 100.0);
                    //System.out.println(percent + "%");
                }


                for(int x = 1; x <= numCols; x++){
                    createAssignmentS(Integer.valueOf(id+x),Integer.valueOf(numGroups+1+"5"+x+"555"));
                }
            }
        }
        fw.flush();
        fw.close();
    }
    public void createNodeS(String id, String name, String type) throws IOException {
        fw.append("node(" + id + "," + name + "," + type + ")\n");
        fw.flush();
    }

    public void createAssignmentS(int child, int parent) throws IOException {
        fw.append("assign(" + child + "," + parent  + ")\n");
        fw.flush();
    }

    public void createAssociationS(int child, int parent, String ops) throws IOException {
        fw.append("associate(" + child + "," + parent + "," + ops + ")\n");
        fw.flush();
    }

    /**
     * Creates a configuration in MySQL
     */
    public void runSql() throws DatabaseException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pmwsdb", "root", "password");
        }catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        deleteNodes();

        createNode("22", "RBAC", "PC");
        createAssignment(1, 22);
        createNode("32", "EMP_PC", "PC");
        createAssignment(1, 32);
        createNode(numGroups+1+"3", "Employees1", "UA");
        createAssignment(32, Integer.parseInt(numGroups+1+"3"));
        createNode(numGroups+1+"5", "Employees2", "OA");
        createAssignment(32, Integer.parseInt(numGroups+1+"5"));
        HashSet<String> ops = new HashSet<>(Arrays.asList("File read", "File write"));
        createAssociation(Integer.valueOf(numGroups+1+"5"), Integer.valueOf(numGroups+1+"3"), ops, true);

        for(int i = 1; i <= numCols; i++){
            createNode(numGroups+1+"5"+i+"555","col_"+i+"_5555555555","OA");
            createAssignment(Integer.valueOf(numGroups+1+"5"+i+"555"),Integer.valueOf(numGroups+1+"5"));
        }

        System.out.println("Building User attributes...");
        for(int i = 1; i <= numGroups; i++){
            createNode(i+"3","Grp_" + i + "_UA","UA");
            createAssignment(22, Integer.valueOf(i+"3"));

            for(int j = 1; j <= numUsers; j++){
                String id = String.valueOf(i) + String.valueOf(j) + "4";
                createNode(id,"u_" + i + "_" + j,"U");
                createAssignment(Integer.valueOf(i+"3"), Integer.valueOf(id));
                createAssignment(Integer.parseInt(numGroups+1+"3"), Integer.valueOf(id));
            }
        }

        System.out.println("Building Objects...");

        double total = numGroups * numUsers * numObjects;

        int c = 0;

        for(int i = 1; i <= numGroups; i++){
            createNode(i+"5","Grp_" + i + "_OA","OA");
            createAssignment(22, Integer.valueOf(i+"5"));
            createAssociation(Integer.valueOf(i+"5"), Integer.valueOf(i+"3"), ops, true);

            for(int j = 1; j <= numUsers; j++){
                String id = String.valueOf(i) + String.valueOf(j) + "45";
                createNode(id,"er_" + i + "_"+j,"OA");
                createAssignment(Integer.valueOf(i+"5"), Integer.valueOf(id));


                for(int k = 1; k <= numObjects; k++){
                    createNode(id+k,"er_" + i + "_"+j+"_"+k,"O");
                    createAssignment(Integer.valueOf(id), Integer.valueOf(id+k+""));
                    c++;
                    double cur = (c/total);
                    int percent = (int) (cur * 100.0);
                    System.out.println(percent + "%");
                }


                for(int x = 1; x <= numCols; x++){
                    createAssignment(Integer.valueOf(numGroups+1+"5"+x+"555"), Integer.valueOf(id+x));
                }
            }
        }
    }

    public void deleteNodes(){
        try{
            Statement stmt = conn.createStatement();
            stmt.execute("delete from node where node_id > 7");
            stmt.execute("delete from assignment");
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createNode(String id, String name, String type){
        try{
            Statement stmt = conn.createStatement();
            stmt.execute("insert into node values ("+id+", '"+ NodeType.toNodeTypeID(type)+"', '"+name+"', '');");
        }catch(SQLException | InvalidNodeTypeException e) {
            e.printStackTrace();
        }
    }


    public void createAssignment(int start, int end){
        try {
            CallableStatement stmt = conn.prepareCall("{call create_assignment(?,?,?)}");
            stmt.setInt(1, (int) start);
            stmt.setInt(2, (int) end);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createAssociation(int oa, int ua, HashSet<String> operations, boolean inherit){
        try {
            String ops = String.join(", ", operations);
            CallableStatement stmt = conn.prepareCall("{call create_association(?,?,?,?)}");
            stmt.setLong(1, ua);
            stmt.setLong(2, oa);
            stmt.setString(3, ops);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a configuration and writes in csv format to a file
     */
    public void runCsv(){
        System.out.println("Building config...");
        addLine("id,name,type");
        addLine("22,test_pc,PC");
        addLine("32,test_emp_pc,PC");
        addLine(numGroups+1+"3,Employees1,UA");
        addLine(numGroups+1+"5,Employees2,OA");

        System.out.println("Building Object attributes");
        for(int i = 1; i <= numCols; i++){
            System.out.println((double)i/numCols);
            addLine(numGroups+1+"5"+i+"555,col_"+i+"_5555555555,OA");
        }

        System.out.println("Building User attributes...");
        for(int i = 1; i <= numGroups; i++){
            System.out.println((double)i/numCols);

            addLine(i+"3,Grp_" + i + "_UA,UA");

            for(int j = 1; j <= numUsers; j++){
                System.out.println((double)j/numCols);

                String id = String.valueOf(i) + String.valueOf(j) + "4";
                addLine(id+",u_" + i + "_" + j + ",U");
            }
        }

        System.out.println("Building Objects...");

        double total = numGroups * numUsers * numObjects;

        int c = 0;
        for(int i = 1; i <= numGroups; i++){
            addLine(i+"5,Grp_" + i + "_OA,OA");

            for(int j = 1; j <= numUsers; j++){
                String id = String.valueOf(i) + String.valueOf(j) + "45";
                addLine(id+",er_" + i + "_"+j+",OA");

                for(int k = 1; k <= numObjects; k++){
                    addLine(id+k+",er_" + i + "_"+j+"_"+k+",O");
                    c++;
                    double cur = (c/total);
                    double percent = (cur * 100.0);
                    System.out.println(percent + "%");
                }
            }
        }

        System.out.println("\nWriting to file..");
        File f = new File("C:\\Users\\jnr6\\Documents\\Neo4j\\default.graphdb\\import\\test.csv");
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (fw != null) {
                fw.write(config);
                fw.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(config);
    }

    int count = 0;
    public void runCsvGcs(){
        File f = new File("C:\\Users\\jnr6\\Documents\\Neo4j\\default.graphdb\\import\\test.csv");
        FileWriter fw = null;

        System.out.println("Building config...");
        addLine("id,name,org_type,type");
        addLine("999999990,gcs pc,PC,PC");

        System.out.println("\nWriting to file..");
        try {
            fw = new FileWriter(f, false);
            fw.write(config);
            fw.flush();
            config = "";
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //org
        for(int i = 1; i <= numGroups; i++) {
            addLine("991" + String.format("%03d", i).replaceAll("0", "1") + ",ORG-" + i + ",ORG,OA");
            addLine("995" + String.format("%03d", i).replaceAll("0", "1") + ",ORG-" + i + ",ORG,UA");

            //folders
            for (int j = 1; j <= numUsers; j++) {
                addLine("992" + String.format("%03d", i).replaceAll("0", "1") + String.format("%03d", j).replaceAll("0", "1") + ",Folder-" + i + "-" + j + ",FOLDER,OA");
                addLine("995" + String.format("%03d", i).replaceAll("0", "1") + String.format("%03d", j).replaceAll("0", "1") + ",Folder-" + i + "-" + j + ",FOLDER,UA");

                //projects
                for (int k = 1; k <= numUsers; k++) {
                    addLine("993" + String.format("%03d", i).replaceAll("0", "1") + String.format("%03d", j).replaceAll("0", "1") + String.format("%03d", k).replaceAll("0", "1") + ",Project-" + i + "-" + j + "-" + k + ",PROJECT,OA");
                    addLine("995" + String.format("%03d", i).replaceAll("0", "1") + String.format("%03d", j).replaceAll("0", "1") + String.format("%03d", k).replaceAll("0", "1") + ",Project-" + i + "-" + j + "-" + k + ",PROJECT,UA");

                    //project resources
                    for(int o = 1; o <= numObjects; o++) {
                        addLine("994" + String.format("%03d", i).replaceAll("0", "1") + String.format("%03d", j).replaceAll("0", "1") + String.format("%03d", k).replaceAll("0", "1") + String.format("%03d", o).replaceAll("0", "1") + ",ProjectResource-" + i + "-" + j + "-" + k + "-" + o + ",PR,OA\n");
                        System.out.println(count++);
                    }
                }

                System.out.println("\nWriting to file..");
                try {
                    fw.append(config);
                    fw.flush();
                    config = "";
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addLine(String csv){
        this.config += csv + "\n";
    }

    public String getConfig(){
        return config;
    }

    /**
     * Uses the cypher file to import bulk data via a csv file
     */
    public void processCsv(){
        try {
            System.out.println("getting connection");
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            conn = DriverManager.getConnection("jdbc:neo4j:http://localhost:7474", "neo4j", "root");


            System.out.println(System.getProperty("user.dir"));
            Scanner sc = new Scanner(new File("src/main/java/gov/nist/policyserver/config/org.cypher"));

            sc.useDelimiter(";");
            while (sc.hasNext()) {
                String s = sc.next();
                System.out.println(s);
                PreparedStatement stmt = conn.prepareStatement(s);
                stmt.executeQuery();

            }
        }
        catch (FileNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void doMedrec(){
        Connection conn;
        try {
            System.out.println("getting connection");
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            conn = DriverManager.getConnection("jdbc:neo4j:http://localhost:7474", "neo4j", "root");


            System.out.println(System.getProperty("user.dir"));
            Scanner sc = new Scanner(new File("src/main/java/gov/nist/policyserver/config/medrec.cypher"));

            sc.useDelimiter(";");
            while (sc.hasNext()) {
                String s = sc.next();
                System.out.println(s);
                PreparedStatement stmt = conn.prepareStatement(s);
                stmt.executeQuery();

            }
        }
        catch (FileNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        GraphConfig c = new GraphConfig(10, 100, 5, 0);
        c.runCsvGcs();
        c.processCsv();
    }
}
