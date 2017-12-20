package com.nicky;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.locks.ReentrantLock;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrentLimitingApplicationTests {

	@Test
	public void contextLoads() {
	    int a = (-1 << 29 + 1) & ~(1 << 29 -1) ;
        System.out.println(a);
        ReentrantLock lock = new ReentrantLock();
    }

}
