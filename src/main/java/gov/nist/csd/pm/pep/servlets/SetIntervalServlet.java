package gov.nist.csd.pm.pep.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetIntervalServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
    {
        String inter = request.getParameter("interval");
        /*if(inter != null){
            try {
                //setInterval(Integer.parseInt(inter));

                request.getRequestDispatcher("/config.jsp?display=block&result=success&message=Interval+set").forward(request, response);
            }
            catch (Exception e) {
                request.getRequestDispatcher("/config.jsp?display=block&result=danger&message=" + e.getMessage().replaceAll(" ", "+")).forward(request, response);
            }
        }*/
        request.getRequestDispatcher("/config.jsp?display=block&result=danger&message=" + "This is not implemented yet".replaceAll(" ", "+")).forward(request, response);
    }
}