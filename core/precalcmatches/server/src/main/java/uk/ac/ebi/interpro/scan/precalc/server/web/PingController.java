package uk.ac.ebi.interpro.scan.precalc.server.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 03/05/12
 * Time: 11:31
 */
@Controller
public class PingController {


    @RequestMapping("/ping")
    public void ping(HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.println("ok");
        writer.close();
    }
}
