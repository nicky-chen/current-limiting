package com.nicky.count;

import com.nicky.guavabucket.RateLimit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Nicky_chin  --Created on 2017/12/18
 */
public class SemaphoreLimit {

    private static Semaphore semaphore = new Semaphore(5);

    public void doRequest(String threadName){
        if(semaphore.getQueueLength()>0){
            System.out.println("{thread--"+threadName+"]===当前等待排队的任务数大于0，请稍候再试...");
        }
        try {
            semaphore.acquire();
            // 处理核心逻辑
            TimeUnit.SECONDS.sleep(1);
            System.out.println("{thread--"+threadName+"]===线程可以使用");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    public static void main(String[] args) {
        SemaphoreLimit limit = new SemaphoreLimit();
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 20 ; i++) {
            int index = i;
            Thread t = new Thread(() -> {
                try {
                    latch.await();
                    Thread.sleep(100);
                    limit.doRequest("thread[" + index + "]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
        latch.countDown();
    }

}
