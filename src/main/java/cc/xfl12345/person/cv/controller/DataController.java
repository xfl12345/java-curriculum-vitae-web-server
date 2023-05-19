package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.sql.SQLException;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
@Slf4j
public class DataController {

    @PostConstruct
    public void init() throws SQLException {
        log.debug("6666666666666666666666666");
    }


}
