package com.cwk.disruptor.main;

import com.cwk.disruptor.event.Trade;
import com.cwk.disruptor.event.TradePublisher;
import com.cwk.disruptor.handler.Handler1;
import com.cwk.disruptor.handler.Handler2;
import com.cwk.disruptor.handler.Handler3;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainSequenceHandle {
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

        //顺序操作
        disruptor.handleEventsWith(new Handler1()).
                handleEventsWith(new Handler2()).
                handleEventsWith(new Handler3());

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
