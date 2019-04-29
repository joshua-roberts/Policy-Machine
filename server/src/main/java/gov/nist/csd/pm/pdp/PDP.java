package gov.nist.csd.pm.pdp;

import gov.nist.csd.pm.epp.EPP;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.pap.PAP;

public class PDP {

    private static PDP pdp;

    public static void init(EPP epp, PAP pap) {
        pdp = new PDP(epp, pap);
    }

    public static PDP getPDP() {
        if (pdp == null) {
            throw new IllegalStateException("PDP is not initialized. Initialize the PDP with PDP.init(...).");
        }

        return pdp;
    }

    private EPP epp;
    private PAP pap;

    public PDP(EPP epp, PAP pap) {
        this.epp = epp;
        this.pap = pap;
    }

    public EPP getEpp() {
        return epp;
    }

    public PAP getPAP() {
        return pap;
    }
}