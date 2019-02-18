package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pap.db.DatabaseContext;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

public class SetConnectionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
    {
        String database = request.getParameter("database");
        String host = request.getParameter("host");
        String port = request.getParameter("port");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String schema = request.getParameter("schema");

        try {
            PAP.getPAP(new DatabaseContext(DatabaseContext.toEnum(database), host, Integer.valueOf(port), username, password, schema));

            request.getRequestDispatcher("/index.jsp?display=block&result=success&message=Database+connection+successful").forward(request, response);
        }
        catch (PMException e) {
            request.getRequestDispatcher("/index.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+")).forward(request, response);
        }
    }
}