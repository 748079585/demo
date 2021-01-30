package com.example.demo.service.impl;

import com.example.demo.annotation.Execute;
import com.example.demo.annotation.ExecuteType;
import com.example.demo.annotation.Limit;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Long save(User user) {

        return userMapper.insert(user);
    }

    @Override
    public User findUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 限制器
     * Limit 注解正常情况用于controller层方法，由于难以做到高并发请求调用接口，放于此处测试，controller层开线程模拟
     * 注：springboot 对每一个http 请求都会开一个线程
     */
    @Limit(name = "test", concurrent = 10, timeOut = 10000)
    @Override
    public void test() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("test完成任务");
    }

    /**
     * 执行器
     * 内部方法，进行异步任务调用，创建一个自定义线程池，线程池名test2
     * type: ExecuteType.FIXED，固定线程池，自定义配置
     * corePoolSize： 核心线程 2 ， 线程池启动后，会初始化2个线程
     * maxPoolSize： 最大线程为 4，线程池满后，继续向线程池提交任务，线程池会扩容，
     *              任务也会提交失败，同时也会出现RejectedExecution异常
     * queueSize： 线程池大小为 8，线程池满后，继续向线程池提交任务，
     *            任务会提交失败，同时也会出现RejectedExecution异常
     */
    @Execute(name = "test2", corePoolSize = 2, maxPoolSize = 4, queueSize = 8, type = ExecuteType.FIXED)
    @Override
    public void test2() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread() + "test2完成任务----");
    }

    /**
     * 执行器
     * 服务器内部，进行异步任务调用，
     */
    @Execute(name = "test3", cachedPoolSize = 10, type = ExecuteType.CACHED)
    @Override
    public void test3() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread() + "test2完成任务");
    }
}
