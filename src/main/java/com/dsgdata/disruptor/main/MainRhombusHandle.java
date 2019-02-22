package com.dsgdata.disruptor.main;

import com.dsgdata.disruptor.event.Trade;
import com.dsgdata.disruptor.event.TradePublisher;
import com.dsgdata.disruptor.handler.HandlerBranch1;
import com.dsgdata.disruptor.handler.HandlerBranch2;
import com.dsgdata.disruptor.handler.HandlerBranch3;
import com.dsgdata.disruptor.handler.HandlerBranch4;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainRhombusHandle {
    public static void main(String[] args) throws InterruptedException {
        long beginTime=System.currentTimeMillis();
        int bufferSize=1024;
        ExecutorService executor= Executors.newFixedThreadPool(8);
        Disruptor<Trade> disruptor = new Disruptor<Trade>(new EventFactory<Trade>() {
            @Override
            public Trade newInstance() {
                return new Trade();
            }
        }, bufferSize, executor, ProducerType.SINGLE, new BusySpinWaitStrategy());

        EventHandler handlerBranch1 =new HandlerBranch1();
        EventHandler handlerBranch2 =new HandlerBranch2();
        EventHandler handlerBranch3 =new HandlerBranch3();

        //disruptor.handleEventsWith(new HandlerBranch1()).then(new HandlerBranch2(),new HandlerBranch3());
        disruptor.handleEventsWith(handlerBranch1).then(handlerBranch2,handlerBranch3).then(new HandlerBranch4());
        //disruptor.after(handlerBranch1).then(handlerBranch2);
        //disruptor.after(handlerBranch1).then(handlerBranch3);
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
