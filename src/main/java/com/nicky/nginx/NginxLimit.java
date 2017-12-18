package com.nicky.nginx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author Nicky_chin  --Created on 2017/12/18
 * Nginx+Lua
 */
public class NginxLimit {


    public  String sendGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in;
        try {
            URL realUrl = new URL(url + "?" + param);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void main(String[] args) throws IOException {
        final NginxLimit limit = new NginxLimit();
        final CountDownLatch latch = new CountDownLatch(1);
        final Random random = new Random(10);
        for (int i = 0; i < 20; i++) {
            final int index = i;
            Thread t = new Thread(() -> {
                try {
                    latch.await();
                    int sleepTime = random.nextInt(1000);
                    Thread.sleep(sleepTime);
                    String result = limit.sendGet("http://localhost:80/testapi", index+"");
                    System.out.println(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
        latch.countDown();
    }
}
