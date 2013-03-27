package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 04/05/12
 * Time: 13:31
 */

@Controller
public class ShutdownController {

    private MatchesService matchService;

    @Autowired
    public void setMatchService(MatchesService matchService) {
        this.matchService = matchService;
    }

    /**
     * /shutdown can only be called from the machine tomcat is running on
     * as a security measure.
     *
     * @param request
     * @param response
     * @throws UnknownHostException
     */
    @RequestMapping("/shutdown")
    public void shutdown(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean localRequest = false;
        final String remoteIP = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteIP) || "127.0.0.1".equals(remoteIP) || "0.0.0.0".equals(remoteIP)) {
            localRequest = true;
        }
        if (!localRequest) {
            // Check just in case the external IP of the server has been passed back.
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            // Check all network interfaces for the local IP address.
            networkInterfaces:
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                Enumeration<InetAddress> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress localHost = e2.nextElement();
                    final String localIP = localHost.getHostAddress();
                    if (localIP.equals(remoteIP)) {
                        localRequest = true;
                        break networkInterfaces;
                    }
                }
            }
        }


        final PrintWriter writer = response.getWriter();
        if (localRequest) {
            // Safe to proceed - called from the host machine.
            matchService.shutdown();

            writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Shut down BerkeleyDB</title></head>" +
                    "<body><p>BerkeleyDB <b>shut down successfully</b></p></body></html>");
        } else {
            writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>FAILED to shut down BerkeleyDB</title></head>" +
                    "<body><p>BerkeleyDB <b>NOT</b> shut down.  You must shut down directly <b>from the server running this " +
                    "service</b>, using wget or curl.</p></body></html>");
        }
        writer.close();
    }

}
