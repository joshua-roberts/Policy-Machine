package gov.nist.csd.pm.pep.requests;

import java.util.HashMap;

public class UpdateNodeRequest {
    String                  name;
    HashMap<String, String> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
}
