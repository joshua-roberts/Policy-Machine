package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pip.db.DatabaseContext;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetConnectionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
    {
        String host = request.getParameter("host");
        String port = request.getParameter("port");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String schema = request.getParameter("schema");

        try {
            PAP.getPAP(new DatabaseContext(host, Integer.valueOf(port), username, password, schema));

            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=success&message=Database+connection+successful");
        }
        catch (PMException e) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+"));
        }
    }
}