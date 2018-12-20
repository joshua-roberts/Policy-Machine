package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.PAP;

import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {

    @Override
    public void init() {
        try {
            PAP.getPAP();
        }
        catch (PMException e) {
            e.printStackTrace();
        }
    }
}
