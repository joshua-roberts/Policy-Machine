package gov.nist.csd.pm.epp.obligations;

public class EvrRequest {
    private String source;

    public EvrRequest() {

    }

    public EvrRequest(String scriptName, String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
