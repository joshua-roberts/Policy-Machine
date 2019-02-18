package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.common.exceptions.*;

import gov.nist.csd.pm.pdp.services.ConfigurationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ResetServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        try {
            ConfigurationService service = new ConfigurationService();
            service.reset();

            request.getRequestDispatcher("/index.jsp?display=block&result=success&message=Data+reset+successfully").forward(request, response);
        }
        catch (PMException e) {
            request.getRequestDispatcher("/index.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+")).forward(request, response);
        }
    }
}