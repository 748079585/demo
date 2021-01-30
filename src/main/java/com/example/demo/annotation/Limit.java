package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 限制器，限制并发数，一般用于controller层方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Limit {

    /** 资源的名字，必须唯一
     * @return String
     */
    String name();

    /** 限制并发数
     *
     * @return int
     */
    int concurrent();

    /** 超时时间（毫秒）
     * > 0: 一直请求资源，超时时间为timeOut，超时返回服务器爆满;
     * = 0: 请求一次资源，没获取到直接返回 服务器爆满;
     * < 0: 一直请求资源，直到中断退出.
     *
     * @return int
     */
    int timeOut() default -1;

}