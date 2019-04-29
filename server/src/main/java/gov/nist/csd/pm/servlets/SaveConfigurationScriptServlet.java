package gov.nist.csd.pm.servlets;

import gov.nist.csd.pm.pdp.services.ConfigurationService;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SaveConfigurationScriptServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
    {
        try {
            System.out.println(System.getProperty("user.dir"));
            ConfigurationService service = new ConfigurationService();
            String configuration = service.save();

            String configName = request.getParameter("configName");
            if(configName == null || configName.isEmpty()) {
                configName = UUID.randomUUID().toString();
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + configName +".pm\"");
            response.setContentType("application/octet-stream");

            StringBuffer sb = new StringBuffer(configuration);
            InputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            ServletOutputStream out = response.getOutputStream();

            out.write(sb.toString().getBytes(), 0, sb.toString().length());
            in.close();
            out.flush();
            out.close();

            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=success&message=Configuration+saved");
        }
        catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?display=block&result=error&message=" + e.getMessage().replaceAll(" ", "+"));
        }
    }
}
