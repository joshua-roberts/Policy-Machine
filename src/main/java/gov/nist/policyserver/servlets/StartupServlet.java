package gov.nist.policyserver.servlets;

import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.exceptions.DatabaseException;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.SQLException;

public class StartupServlet extends HttpServlet {

    @Override
    public void init() {
        DAOManager daoManager = null;
        try {
          daoManager = DAOManager.getDaoManager();
          daoManager.Initialize();
        }
        catch (DatabaseException | IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
