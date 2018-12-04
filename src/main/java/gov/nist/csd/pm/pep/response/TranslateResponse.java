package gov.nist.csd.pm.pep.response;

public class TranslateResponse {
    String sql;

    public TranslateResponse(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
