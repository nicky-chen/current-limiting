package com.nicky.guavabucket;

import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Nicky_chin  --Created on 2017/12/17
 * 平滑限流接口请求数
 */
public class RateLimit {

    public static RateLimiter rateLimiter = RateLimiter.create(9); //TPS为10

    /*
    * 令牌桶 tryAcquire() 获取令牌
    * 漏桶  rateLimiter.acquire(10)); 透支令牌
    * */
    public void doRequest(String threadName) {
        //检测有没有可用的令牌，结果马上返回
        boolean isAcquired = rateLimiter.tryAcquire();
        if (isAcquired) {
            System.err.println(threadName + ":下单成功");
        } else {
            System.out.println(threadName + ":当前下单人数过多，请稍后再试");
        }
    }

    public static void main(String[] args) throws IOException {
        RateLimit limit = new RateLimit();
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 50 ; i++) {
            int index = i;
            Thread t = new Thread(() -> {
                try {
                    latch.await();
                    Thread.sleep(1000);
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
