package com.example.demo.annotation.aspect;

import com.example.demo.annotation.Limit;
import com.example.demo.bean.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Aspect
@Configuration
public class LimitAspect {

    @Pointcut("@annotation(com.example.demo.annotation.Limit)")
    private void cutMethod() {
    }

    Lock lock = new ReentrantLock();
    private final ConcurrentMap<String, Semaphore> semaphoreConcurrentMap = new ConcurrentHashMap<>();

    @Around("cutMethod()")
    public Object interceptor(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Limit limitAnnotation = method.getAnnotation(Limit.class);
        int timeOut = limitAnnotation.timeOut();   // 超时时间
        String name = limitAnnotation.name();   // 资源名称

        // Semaphore，采用信号量表示资源数目，多线程并发抢占资源。
        Semaphore semaphore;
        //  ConcurrentMap并发访问，获取资源名name 下的 资源Semaphore
        if (semaphoreConcurrentMap.containsKey(name)) {
            semaphore = semaphoreConcurrentMap.get(name);
        } else {// 创建limitCount 个资源，同步创建
            log.info("抢锁，创建资源");
            lock.lock();
            if (!semaphoreConcurrentMap.containsKey(name)) {
                int limitCount = limitAnnotation.concurrent();  // 限制同时访问量
                log.info("创建：{} 放入 {} 个资源的semaphore", name, limitCount);
                semaphoreConcurrentMap.put(name, new Semaphore(limitCount));
            }
            semaphore = semaphoreConcurrentMap.get(name);
            lock.unlock();
        }
        if (timeOut < 0) { // 如果
            try {
                //从此信号量获取许可，阻塞直到可用，直到线程被中断。
                //如果有许可证，则获取许可证并立即返回，从而将可用许可证的数量减少一个。
                semaphore.acquire();
            } catch (InterruptedException e) {
                log.error(Thread.currentThread().getName() + "中断返回");
                return ApiResult.failure("服务器已中断，请稍后再试");
            }
        } else if (timeOut == 0) {
            // 请求一次，成功立即返回，值为true，将可用许可证的数量减少一个。
            // 如果没有可用的许可，则此方法将立即返回false值。
            if (!semaphore.tryAcquire()) {
                log.error(Thread.currentThread().getName() + "服务器已爆满,请稍后再试");
                return ApiResult.failure("服务器已爆满，请稍后再试");
            }
        } else {
            try {
                // 如果在给定的等待时间内可用，并且当前线程尚未中断，则从此信号量获取许可。
                if (!semaphore.tryAcquire(timeOut, TimeUnit.MILLISECONDS)) {
                    log.info(Thread.currentThread().getName() + "获取资源超时");
                    return ApiResult.failure("服务器已爆满，请稍后再试");
                }
            } catch (InterruptedException e) {
                log.error(Thread.currentThread().getName() + "中断返回");
                return ApiResult.failure("服务器已中断，请稍后再试");
            }
        }
        log.info(name + "--进入--列队后,等待线程数：" + semaphore.getQueueLength());
        Object proceed;
        try {
            proceed = pjp.proceed();   // 执行任务
        } catch (Throwable throwable) {
            return ApiResult.failure("服务器内部错误！");
        }
        log.info(name + "退出--列队--前,等待线程数：" + semaphore.getQueueLength());
        // 添加一个许可证，从而可能释放一个正在阻塞的获取者
        semaphore.release();
        return proceed;
    }

//    private static final String LOCK = "lock";
//
//    ThreadLocal<Map> local = new ThreadLocal<>();
//
//    private final ConcurrentMap<String, BlockingQueue<String>> cacheMap = new ConcurrentHashMap<>();

//    @Around("cutMethod()")
//    public Object Around(ProceedingJoinPoint pjp) {
//        BlockingQueue<String> queue;
//        MethodSignature signature = (MethodSignature) pjp.getSignature();
//        Method method = signature.getMethod();
//        Limit limitAnnotation = method.getAnnotation(Limit.class);
//        int limitCount = limitAnnotation.count();  // 限制同时访问量
//        int timeOut = limitAnnotation.timeOut();   // 超时时间
//        String name = limitAnnotation.name();   // 资源名称 ，为每个资源分配一个 阻塞队列
//
//        if (cacheMap.containsKey(name)) {   // 有，之前取出这个 任务队列
//            queue = cacheMap.get(name);
//        } else {
//            queue = new LinkedBlockingDeque<>(limitCount); // 否则，新建一个size 为 limitCount的队列
//            cacheMap.put(name, queue);
//        }
//
//        if (limitAnnotation.timeOut() <= 0) {
//            try {
//                queue.put(LOCK);  // 添加，满时阻塞
//            } catch (InterruptedException e) {
//                log.error(Thread.currentThread().getName() + "中断返回");
//                return null;
//            }
//        } else {
//            try {
//                if (!queue.offer(LOCK, timeOut, TimeUnit.MILLISECONDS)) {   // 添加，满时阻塞，超时返回false，超时单位毫秒
//                    log.info(Thread.currentThread().getName() + "超时返回");
//                    return null;
//                }
//            } catch (InterruptedException e) {
//                log.error(Thread.currentThread().getName() + "中断返回");
//                return null;
//            }
//        }
//
//        Object proceed = null;
//        try {
//            proceed = pjp.proceed();   // 执行任务
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        queue.poll(); // 出队一个
//
//        return proceed;
//    }

}