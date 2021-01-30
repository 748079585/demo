package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {

    User findUserById(Long id);

    Long save(User user);

    void test();

    void test2();

    void test3();
}
