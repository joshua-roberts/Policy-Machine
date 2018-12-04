package gov.nist.csd.pm.pep.servlets;

import gov.nist.csd.pm.common.exceptions.*;

import gov.nist.csd.pm.pdp.services.ConfigurationService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

public class LoadConfigurationScriptServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        DiskFileItemFactory factory = new DiskFileItemFactory();

        ServletContext servletContext = this.getServletConfig().getServletContext();
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);

        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> items = upload.parseRequest(request);
            System.out.println(items);
            for(FileItem item : items) {
                if (!item.isFormField()) {
                    InputStream uploadedStream = item.getInputStream();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(uploadedStream, writer, "UTF-8");
                    String config = writer.toString();

                    ConfigurationService service = new ConfigurationService();
                    service.load(config);
                }
            }
            request.getRequestDispatcher("/config.jsp?display=block&result=success&message=Configuration+loaded+successfully").forward(request, response);
        }
        catch ( FileUploadException | DatabaseException | InvalidAssignmentException |
                MissingPermissionException | NoIDException | InvalidAssociationException |
                NullNameException | SessionDoesNotExistException | InvalidNodeTypeException |
                NullTypeException | NullNodeException | HashingUserPasswordException |
                NodeNotFoundException | LoadConfigException | InvalidProhibitionSubjectTypeException e) {
            request.getRequestDispatcher("/config.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+")).forward(request, response);
        }
    }
}