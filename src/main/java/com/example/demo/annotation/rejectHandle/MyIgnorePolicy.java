package com.example.demo.annotation.rejectHandle;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**  线程池拒绝策略，一般是因为线程池满了，不接受新任务
 *      默认拒绝策略是AbortPolicy，会抛出 RejectedExecutionException
 */
public class MyIgnorePolicy implements RejectedExecutionHandler {

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        doLog(r, e);
    }

    private void doLog(Runnable r, ThreadPoolExecutor e) {
        // 可做日志记录等
        System.err.println(r.toString() + "-- rejected");
        System.out.println("completedTaskCount: " + e.getCompletedTaskCount());
    }
}