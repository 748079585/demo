package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/testBoot")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("getUser/{id}")
    public User GetUser(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @PostMapping("save")
    public Long save() {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setUserName("老王");
        user.setPassWord("123");
        return userService.save(user);
    }

    @PostMapping("batch/save")
    public String batchSave() {
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setId(System.currentTimeMillis());
            user.setUserName("老王" + i);
            user.setPassWord("123" + i);
            userService.save(user);
        }
        return "success";
    }

    @PostMapping("test")
    public void test() {
        for (int i = 0; i < 30; i++) {
            new Thread(() -> userService.test()).start();
        }
    }

    @PostMapping("test2")
    public void test2() {
        for (int i = 0; i < 22; i++) {
            if(i == 8 || i ==17){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("等待5秒！！！-------------");
            }
            userService.test2();
        }
    }

    @PostMapping("test3")
    public void test3() {
        for (int i = 0; i < 30; i++) {
            userService.test3();
        }
    }


}