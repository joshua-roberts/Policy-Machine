package gov.nist.csd.pm.pep.requests;

public class SandboxRequest {
    private String source;

    public SandboxRequest() {

    }

    public SandboxRequest(String scriptName, String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
