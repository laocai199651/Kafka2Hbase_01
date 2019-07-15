package com.cwk.disruptor;

import com.cwk.disruptor.event.Order;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainMultiPAndC {


    private static Logger logger = LoggerFactory.getLogger(MainMultiPAndC.class);

    public static void main(String[] args) throws Exception {
        //创建ringBuffer
        RingBuffer<Order> ringBuffer =
                RingBuffer.create(ProducerType.MULTI,
                        () -> new Order()
                        ,
                        1024 * 1024,
                        new LiteTimeoutBlockingWaitStrategy(6000, TimeUnit.MILLISECONDS));

        SequenceBarrier barriers = ringBuffer.newBarrier();

        Consumer[] consumers = new Consumer[3];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Consumer("c" + i);
        }

        WorkerPool<Order> workerPool =
                new WorkerPool<Order>(ringBuffer,
                        barriers,
                        new IntEventExceptionHandler(),
                        consumers);

        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        workerPool.start(service);

        final CountDownLatch latch = new CountDownLatch(1);

        ArrayList<Producer> producers = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Producer p = new Producer(ringBuffer);
            producers.add(p);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        List<String> strings = Arrays.asList("a", "b", "v");
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.addAll(strings);
        arrayList.addAll(strings);
        arrayList.addAll(strings);
        arrayList.addAll(strings);

        arrayList.forEach(string -> {
            executorService.submit(() ->
            {
               /* try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                for (int j = 0; j < 10; j++) {
                    producers.get(j%producers.size()).onData(string + UUID.randomUUID().toString());
                    logger.info("发布成功");
                }
            });
        });

        AtomicInteger atomicInteger = new AtomicInteger(0);

        new Thread(() -> {
            while (true) {
                //System.out.println(workerPool.isRunning());
                //workerPool.drainAndHalt();
                System.out.println(ringBuffer.getCursor() + "    " + Util.getMinimumSequence(workerPool.getWorkerSequences()));
                try {
                    Thread.sleep(2000);
                    if (atomicInteger.addAndGet(1) >= 3&&executorService.isShutdown()) {
                        System.exit(0);
                        break;
                    }
                    executorService.shutdownNow();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        // Thread.sleep(2000);
        // System.out.println("---------------开始生产-----------------");
        //latch.countDown();
        // Thread.sleep(5000);
        System.out.println("总数:" + consumers[0].getCount());

        // System.out.println("disruptor状态    " + ringBuffer.getBufferSize());


        //workerPool.halt();
        //System.out.println("disruptor状态    " + workerPool.isRunning());
        //workerPool.drainAndHalt();
        //service.shutdown();
        //System.out.println("disruptor 结束");
        //System.exit(0);
    }

    static class IntEventExceptionHandler implements ExceptionHandler {
        public void handleEventException(Throwable ex, long sequence, Object event) {
        }

        public void handleOnStartException(Throwable ex) {
        }

        public void handleOnShutdownException(Throwable ex) {
        }
    }
}
