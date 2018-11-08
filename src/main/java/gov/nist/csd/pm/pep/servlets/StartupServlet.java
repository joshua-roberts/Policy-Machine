package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.model.exceptions.InvalidProhibitionSubjectTypeException;
import gov.nist.csd.pm.model.exceptions.LoadConfigException;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.LoaderException;

import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {

    @Override
    public void init() {
        try {
            PAP.getPAP();
        }
        catch (DatabaseException | LoadConfigException | LoaderException | InvalidProhibitionSubjectTypeException e) {
            e.printStackTrace();
        }
    }
}
