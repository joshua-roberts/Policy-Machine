package gov.nist.csd.pm.demos.ndac.pep;

import java.util.*;

public class NDACResponse {
    List<NDACResult> results;

    public NDACResponse(){
        results = new ArrayList<>();
    }

    public NDACResponse(List<NDACResult> results) {
        this.results = results;
    }

    public void addResult(NDACResult result) {
        this.results.add(result);
    }

    public List<NDACResult> getResults() {
        return results;
    }

    public void setResults(List<NDACResult> results) {
        this.results = results;
    }

    static class NDACResult {
        String user;
        List<String> attributes;
        String permittedSQL;
        SQLResults data;
        double time;

        public NDACResult() {}

        public NDACResult(String user, List<String> attributes, String permittedSQL, SQLResults data, double time) {
            this.user = user;
            this.attributes = attributes;
            this.permittedSQL = permittedSQL;
            this.data = data;
            this.time = time;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<String> attributes) {
            this.attributes = attributes;
        }

        public String getPermittedSQL() {
            return permittedSQL;
        }

        public void setPermittedSQL(String permittedSQL) {
            this.permittedSQL = permittedSQL;
        }

        public SQLResults getData() {
            return data;
        }

        public void setData(SQLResults data) {
            this.data = data;
        }

        public double getTime() {
            return time;
        }

        public void setTime(double time) {
            this.time = time;
        }
    }

    static class SQLResults {
        HashSet<String>               columns;
        List<HashMap<String, String>> results;

        public SQLResults() {

        }

        public SQLResults(HashSet<String> columns, List<HashMap<String, String>> results) {
            if(results != null) {
                this.results = results;
            } else {
                this.results = new ArrayList<>();
            }

            if(columns != null) {
                this.columns = columns;
            } else {
                this.columns = new HashSet<>();
            }
        }

        public SQLResults(HashSet<String> columns, HashMap<String, String>[] results) {
            this.columns = columns;
            this.results = new ArrayList<>();
            this.results.addAll(Arrays.asList(results));
        }

        public HashSet<String> getColumns() {
            return columns;
        }

        public void setColumns(HashSet<String> columns) {
            this.columns = columns;
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
}
