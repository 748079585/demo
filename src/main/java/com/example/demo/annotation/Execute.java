package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 执行器，并发任务，一般用于service层方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Execute {

    /** 线程池的名字，可以唯一，可以共用
     */
    String name() default "executeService";

    /** 最大线程数
     */
    int maxPoolSize() default 20;

    /** 核心线程数
     */
    int corePoolSize() default 0;

    /** 线程队列，线程队列满后，再次线程池提交任务，会抛出RejectedExecutionException 异常
     */
    int queueSize() default Integer.MAX_VALUE;

    /** 超时时间，空闲的非核心线程，60秒后自动清除
     */
    int timeOut() default 60;

    /**
     * 线程池类型，默认缓存型
     */
    ExecuteType type() default ExecuteType.CACHED;

    /**  缓存线程池大小   (只适用于缓存线程池)  type() 为 ExecuteType.CACHED
     *   default Integer.MAX_VALUE : 相当于 ExecutorService 的newCachedThreadPool()；
     *   最好不变，不然，如配置10，但是有超过10个的并发任务，超过的任务会被拒绝，RejectedExecutionException 异常
     */
    int cachedPoolSize() default Integer.MAX_VALUE;
}
