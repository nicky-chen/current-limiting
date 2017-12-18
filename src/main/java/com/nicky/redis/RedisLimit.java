package com.nicky.redis;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

/**
 * @author Nicky_chin  --Created on 2017/12/18
 *Redis+Lua实现 分布式限流
 */
public class RedisLimit {

    //限流大小
    private static final String req_limit = "6";

    private static final String unit_time = "2";

    private static final String key = "ip:localhost";

    private static final Jedis jedis;

    static {
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(200);
        config.setMaxWaitMillis(5000);
        final JedisPool jedisPool = new JedisPool(config, "localhost", 6379, 10000, "123456");
        jedis = jedisPool.getResource();
    }

    private /*synchronized*/ boolean acquire() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "\\src\\main\\resources", "redis-limit.lua");
        String luaScript = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return (Long) jedis.eval(luaScript, Lists.newArrayList(key), Lists.newArrayList(unit_time, req_limit)) == 1;
    }

    public void doRequest(final String threadName) throws IOException {
        //检测有没有可用的令牌，结果马上返回
        boolean isAcquired = acquire();
        if (isAcquired) {
            System.err.println(threadName + ":请求通过");
        } else {
            System.out.println(threadName + ":当前请求数量过多，请稍后再试");
        }
    }

    public static void main(final String[] args) throws Exception{

        final RedisLimit limit = new RedisLimit();
        final CountDownLatch latch = new CountDownLatch(1);
        final ThreadFactory factory = Executors.privilegedThreadFactory();
        final int size = Runtime.getRuntime().availableProcessors();
        //需要根据QPS设置参数
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size * 2, 15, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(30), factory);
        //线程池的拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        for (int i = 1; i <= 38; i++) {
            final int index = i;
            Runnable task = () -> {
                try {
                    latch.await();
                    TimeUnit.MILLISECONDS.sleep(20);
                    limit.doRequest("thread[" + index + "]");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            };
            executor.submit(task);
        }
        latch.countDown();
        executor.shutdown();
        TimeUnit.SECONDS.sleep(5);
        System.exit(0);
    }

}
