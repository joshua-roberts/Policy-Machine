package gov.nist.csd.pm.servlets;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.ConfigurationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ResetServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        try {
            ConfigurationService service = new ConfigurationService();
            service.reset();

            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=success&message=Data+reset+successfully");
        }
        catch (PMException e) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+"));
        }
    }
}