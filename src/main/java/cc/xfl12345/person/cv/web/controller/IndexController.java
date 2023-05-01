package cc.xfl12345.person.cv.web.controller;

import cc.xfl12345.person.cv.web.ControllerConst;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
public class IndexController {

    @GetMapping(path = {"", "/", "hello-world"})
    public String helloWorld() {
        return "Hello world!";
    }

}
