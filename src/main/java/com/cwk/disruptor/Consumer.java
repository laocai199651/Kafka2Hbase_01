package com.cwk.disruptor;

import com.cwk.disruptor.event.Order;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements WorkHandler<Order> {


    private static Logger logger = LoggerFactory.getLogger(Consumer.class);

    private String consumerId;

    private static AtomicInteger count = new AtomicInteger(0);

    public Consumer(String consumerId){
        this.consumerId = consumerId;
    }
    @Override
    public void onEvent(Order order) throws Exception {
        System.out.println("当前线程"+Thread.currentThread().getName()+" - pool -"+Thread.currentThread().getThreadGroup().getName()+" 当前消费者: " + this.consumerId + "，消费信息：" + order.getId());
        count.incrementAndGet();
    }

    public int getCount(){
        return count.get();
    }
}
