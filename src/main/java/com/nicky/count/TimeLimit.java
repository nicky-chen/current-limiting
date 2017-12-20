package com.nicky.count;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Nicky_chin  --Created on 2017/12/17
 * 时间窗口进行并发控制
 */
public class TimeLimit {

    private static final long limit = 5; //限流数

    public static volatile Boolean exit = false;

    /*用Guava的Cache来存储计数器，利用秒数作为Key，Value代表这一秒有多少个请求，
    这样就限制了一秒内的并发数。另外过期时间设置为两秒，保证一秒内的数据是存在的*/
    private LoadingCache<Long, AtomicLong> counter = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS)
            .build(new CacheLoader<Long, AtomicLong>() {

                @Override
                public AtomicLong load(Long aLong) throws Exception {
                    return new AtomicLong(0);
                }
            });

    public void doRequest(String threadName) throws ExecutionException {
        long currentSecond = LocalDateTime.now().getSecond();
        if (counter.get(currentSecond).incrementAndGet() > limit) {
            System.out.println(threadName + ":请求过多，请稍后再尝试");
        } else {
            System.err.println(threadName + ":您的请求已受理");
        }
    }

    public static void main(String[] args) throws Exception {

        final TimeLimit timeLimit = new TimeLimit();
        final CountDownLatch latch = new CountDownLatch(1);
        final ThreadFactory factory = Executors.defaultThreadFactory();
        final RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(2), factory, handler);
        List<Future> list = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            final int index = i;
            Runnable task = () -> {
                while (!exit) {
                    try {
                        latch.await();
                        timeLimit.doRequest("thread[" + index + "]");
                        Thread.sleep(100);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            };
            executor.submit(task);
        }
        latch.countDown();
        TimeUnit.SECONDS.sleep(5);
        exit = true;
        executor.shutdown();
    }

}
