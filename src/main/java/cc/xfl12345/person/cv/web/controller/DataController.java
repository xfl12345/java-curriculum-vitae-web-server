package cc.xfl12345.person.cv.web.controller;

import cc.xfl12345.person.cv.web.ControllerConst;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.SessionFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
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
