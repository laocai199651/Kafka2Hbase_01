package com.dsgdata.disruptor.main;

import com.dsgdata.disruptor.event.Trade;
import com.dsgdata.disruptor.handler.TradeHandler2;
import com.dsgdata.disruptor.handler.Trade_Handler2_01;
import com.lmax.disruptor.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用WorkerPool消息处理器。
 */
public class Main2 {

    public static void main(String[] args) throws InterruptedException {
        int BUFFER_SIZE=1024;
        int THREAD_NUMBERS=4;

        EventFactory<Trade> eventFactory = new EventFactory<Trade>() {
            public Trade newInstance() {
                return new Trade();
            }
        };

        RingBuffer<Trade> ringBuffer = RingBuffer.createSingleProducer(eventFactory, BUFFER_SIZE);

        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBERS);

        WorkHandler<Trade> handler = new TradeHandler2();
        Trade_Handler2_01 handler2_01 = new Trade_Handler2_01();
        WorkerPool<Trade> workerPool = new WorkerPool<Trade>(ringBuffer, sequenceBarrier, new IgnoreExceptionHandler(), handler,handler2_01);

        workerPool.start(executor);

        //下面这个生产10个数据
        for(int i=0;i<10;i++){
            long seq=ringBuffer.next();
            ringBuffer.get(seq).setPrice(Math.random()*9999);
            ringBuffer.publish(seq);
        }

        Thread.sleep(10000);
        workerPool.halt();
        executor.shutdown();
    }


}
