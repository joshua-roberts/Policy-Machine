package gov.nist.csd.pm.servlets;

import gov.nist.csd.pm.pip.DAOManager;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.SQLException;

public class StartupServlet extends HttpServlet {

    @Override
    public void init() {
        try {
            DAOManager.getDaoManager();
        }
        catch (DatabaseException | IOException | ClassNotFoundException | SQLException | InvalidPropertyException e) {
            e.printStackTrace();
        }
    }
}
