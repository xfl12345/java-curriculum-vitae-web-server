package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.pojo.database.MeetHr;
import cc.xfl12345.person.cv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
@RequestMapping("db/user")
public class UserDataController {

    protected UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("id/by-phone-number")
    public Long getHrIdByPhoneNumber(String phoneNumber) {
        return userService.getHrIdByPhoneNumber(phoneNumber);
    }

    @GetMapping("by-phone-number")
    public List<MeetHr> getHrInfoByPhoneNumber(String phoneNumber) {
        return userService.getHrInfoByPhoneNumber(phoneNumber);
    }

    @GetMapping("all")
    public List<MeetHr> getAllHrInfo() {
        return userService.getAllHrInfo();
    }

    @GetMapping("count")
    public long getHrInfoCount() {
        return userService.getHrInfoCount();
    }

    @GetMapping("by-id")
    public MeetHr getHrInfoById(Long id) {
        return userService.getHrInfoById(id);
    }

    @DeleteMapping("by-id")
    public boolean deleteHrInfoById(Long id) {
        return userService.deleteHrInfoById(id);
    }

    @PutMapping("by-id")
    public boolean updateHrInfoById(@RequestBody MeetHr meetHr) {
        return userService.updateHrInfoById(meetHr);
    }

    @PutMapping("")
    public boolean addHrInfo(@RequestBody MeetHr meetHr) {
        return userService.addHrInfo(meetHr);
    }

}
