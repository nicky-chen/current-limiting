package com.nicky.count;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Nicky_chin  --Created on 2017/12/17
 * 接口层面根据请求数并发量控制
 */
public class AtomicLimit {

    private static AtomicInteger requestCount;

    public void doRequest(String threadName) {
        //并发请求数量竞争
        if (requestCount.decrementAndGet() < 0) {
            System.out.println(threadName + ":请求过多，请稍后再尝试");
        } else {
            System.err.println(threadName + ":您的请求已受理");
        }
    }

    public static void main(final String[] args) throws Exception {

        final AtomicLimit atomicLimit = new AtomicLimit();
        requestCount = new AtomicInteger(-20);
        //1秒后所有线程同时启动
        final CountDownLatch latch = new CountDownLatch(1);
        //线程池
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        final RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 15, 50L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(400), threadFactory, handler);

        for (int i = 1; i <= 50; i++) {
            final int index = i;
            Runnable task = () -> {
                try {
                    latch.await();
                    //处理请求、
                    atomicLimit.doRequest("thread[" + index + "]");
                } catch (InterruptedException e) {
                    e.getStackTrace();
                }

            };
            executor.submit(task);
        }
        latch.countDown();
        executor.shutdown();
    }

}
