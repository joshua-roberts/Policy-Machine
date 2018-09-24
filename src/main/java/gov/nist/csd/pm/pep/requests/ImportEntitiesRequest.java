package gov.nist.csd.pm.pep.requests;

import java.util.HashMap;

public class ImportEntitiesRequest {
    String kind;
    HashMap<String, Object>[] entities;

    public HashMap<String, Object>[] getEntities() {
        return entities;
    }

    public String getKind() {
        return kind;
    }
}
