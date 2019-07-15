package com.cwk.disruptor.main;

import com.cwk.disruptor.event.Trade;
import com.cwk.disruptor.event.TradePublisher;
import com.cwk.disruptor.handler.*;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiMain {
    public static void main(String[] args) throws InterruptedException {
        long beginTime=System.currentTimeMillis();
        int bufferSize=1024;
        ExecutorService executor= Executors.newFixedThreadPool(8);
        Disruptor<Trade> disruptor = new Disruptor<Trade>(new EventFactory<Trade>() {
            @Override
            public Trade newInstance() {
                return new Trade();
            }
        }, bufferSize, /*executor*/Executors.defaultThreadFactory(), ProducerType.SINGLE, new BusySpinWaitStrategy());
        //六边形操作.
        Handler1 h1 = new Handler1();
        Handler2 h2 = new Handler2();
        Handler3 h3 = new Handler3();
        Handler4 h4 = new Handler4();
        Handler5 h5 = new Handler5();
        disruptor.handleEventsWith(h1, h2);
        disruptor.after(h1).handleEventsWith(h4);
        disruptor.after(h2).handleEventsWith(h5);
        disruptor.after(h4, h5).handleEventsWith(h3);
        disruptor.start();//启动
        CountDownLatch latch=new CountDownLatch(1);
        //生产者准备
        executor.submit(new TradePublisher(latch, disruptor));
        latch.await();//等待生产者完事.
        disruptor.shutdown();
        executor.shutdown();
        System.out.println("总耗时:"+(System.currentTimeMillis()-beginTime));
    }
}
