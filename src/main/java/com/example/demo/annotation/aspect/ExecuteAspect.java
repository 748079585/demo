package com.example.demo.annotation.aspect;

import com.example.demo.annotation.Execute;
import com.example.demo.annotation.ExecuteType;
import com.example.demo.annotation.rejectHandle.MyIgnorePolicy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Aspect
@Configuration
public class ExecuteAspect {

    private final ConcurrentMap<String, ThreadPoolExecutor> executorCache = new ConcurrentHashMap<>();
    Lock lock = new ReentrantLock();

    @Around("@annotation(com.example.demo.annotation.Execute)")
    public void interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Execute limitAnnotation = method.getAnnotation(Execute.class);
        String name = limitAnnotation.name();   // 线程池名称

        log.info(name + "--进入--");
        ThreadPoolExecutor executorService;
        // 获取资源下的执行器，如果没有有新建一个
        if (executorCache.containsKey(name)) {
            executorService = executorCache.get(name);
        } else {  // 创建线程池，并缓存，同步进行
            lock.lock();
            if (executorCache.containsKey(name)) {
                executorService = executorCache.get(name);
            } else {
                int keepAliveTime = limitAnnotation.timeOut();   // 线程最大空闲时间（毫秒），该参数默认对核心线程无效，核心线程会一直存在
                ThreadFactory threadFactory = new NameTreadFactory(name);  // 线程创建工厂
                RejectedExecutionHandler myIgnorePolicy = new MyIgnorePolicy();  // 线程拒绝策略，异常处理器
                if (limitAnnotation.type().equals(ExecuteType.CACHED)) {
                    log.info(name + "--创建缓存线程池--");
                    // 创建一个缓存线程池
                    int cachedPoolSize = limitAnnotation.cachedPoolSize();
                    executorService = new ThreadPoolExecutor(0, cachedPoolSize,
                            keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<>(),
                            threadFactory, myIgnorePolicy);
                } else {
                    log.info(name + "--创建自定义线程池--");
                    int corePoolSize = limitAnnotation.corePoolSize(); //核心线程池大小
                    int maximumPoolSize = limitAnnotation.maxPoolSize();  // 最大线程池大小
                    int queueSize = limitAnnotation.queueSize();  // 线程池队列

                /*  LinkedBlockingQueue（无界阻塞队列），默认为Integer.MAX_VALUE。
                    如果任务提交速度持续大余任务处理速度，会造成队列大量阻塞。
                    因为队列很大，很有可能在拒绝策略前，内存溢出。是其劣势*/

                /*  当任务队列堆满了，随着任务数量的增加，会在核心线程数的基础上加开线程。
                    如果咋们配置的任务队列是无限的，那么配置的最大线程池会无效，线程数只会有核心线程数大小*/

                    /*  注意： 任务队列满后，继续向线程池提交任务会被拒绝，因此会进入拒绝策略myIgnorePolicy*/

                    // 因此，queueSize看情况设置
                    BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(queueSize);

                    executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                            keepAliveTime, TimeUnit.MILLISECONDS, queue, threadFactory, myIgnorePolicy);
                }

                executorCache.put(name, executorService);  // 放入缓存中，下次同一name的线程池，直接使用
                executorService.prestartAllCoreThreads(); // 预启动所有核心线程
            }
            lock.unlock(); //释放锁
        }

        // 提交线程任务
        executorService.submit(
                new Thread(() -> {
                    try {
                        pjp.proceed();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
        );
    }

    /**
     * 自定义线程工厂
     */
    static class NameTreadFactory implements ThreadFactory {
        String name;

        public NameTreadFactory(String name) {
            this.name = name;
        }

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, name + "-" + mThreadNum.getAndIncrement());
            System.out.println(t.getName() + " has been created");
            return t;
        }
    }


}