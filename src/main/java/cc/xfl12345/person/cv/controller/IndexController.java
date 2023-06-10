package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
public class IndexController {

    @GetMapping(path = {"", "/"})
    public void indexPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("./index.html");
    }

}
