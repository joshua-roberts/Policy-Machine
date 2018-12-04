package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.common.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.common.exceptions.LoadConfigException;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.common.exceptions.DatabaseException;


import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {

    @Override
    public void init() {
        try {
            PAP.getPAP();
        }
        catch (DatabaseException | LoadConfigException | InvalidProhibitionSubjectTypeException e) {
            e.printStackTrace();
        }
    }
}
