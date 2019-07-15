package com.cwk;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    public class LongEvent {
        private long value;
        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public class LongEventFactory implements EventFactory {
        @Override
        public Object newInstance() {
            return new LongEvent();
        }
    }

    /**
     * Callback interface to be implemented for processing events as they become available in the {@link RingBuffer}
     *
     * 当发布者将事件发布到{@link RingBuffer}时调用。{@link BatchEventProcessor}将分批从{@link RingBuffer}读取消息，
     * 其中批处理是所有可用的事件，无需等待任何新事件的到来。这对于需要执行I/O等较慢操作的事件处理程序非常有用，
     * 因为它们可以将来自多个事件的数据分组到单个操作中。实现应该确保在endOfBatch为true时始终执行操作，
     * 因为该消息与下一个消息之间的时间是渐近的。
     *
     */
    public class LongEventHandler implements EventHandler<LongEvent> {
        @Override
        /**
         *
         *  @param event      published to the {@link RingBuffer}
         *  @param sequence   of the event being processed
         *  @param endOfBatch flag to indicate if this is the last event in a batch from the {@link RingBuffer}
         *  @throws Exception if the EventHandler would like the exception handled further up the chain.
         */
        public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
            System.out.println(longEvent.getValue());
        }
    }

    public class LongEventProducer {
        private final RingBuffer<LongEvent> ringBuffer;
        public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        /**
         * onData用来发布事件，每调用一次就发布一次事件事件
         * 它的参数会通过事件传递给消费者
         *
         * @param bb
         */
        public void onData(ByteBuffer bb) {
            //可以把ringBuffer看做一个事件队列，那么next就是得到下面一个事件槽
            long sequence = ringBuffer.next();
            try {
                //用上面的索引取出一个空的事件用于填充
                LongEvent event = ringBuffer.get(sequence);// for the sequence
                event.setValue(bb.getLong(0));
            } finally {
                //发布事件
                ringBuffer.publish(sequence);
            }
        }
    }
    public class LongEventProducerWithTranslator {
        //一个translator可以看做一个事件初始化器，publicEvent方法会调用它
        //填充Event
        private  final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
                new EventTranslatorOneArg<LongEvent, ByteBuffer>() {
                    public void translateTo(LongEvent event, long sequence, ByteBuffer bb) {
                        event.setValue(bb.getLong(0));
                    }
                };
        private final RingBuffer<LongEvent> ringBuffer;
        public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        public void onData(ByteBuffer bb) {
            ringBuffer.publishEvent(TRANSLATOR, bb);
        }
    }

    @Test
    public void testDisrupter() throws InterruptedException {
        // Executor that will be used to construct new threads for consumers
        Executor executor = Executors.newCachedThreadPool();
        // The factory for the event
        LongEventFactory factory = new LongEventFactory();
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;
        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory, bufferSize, executor);
        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());
        // Start the Disruptor, starts all threads running
        disruptor.start();
        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        LongEventProducerWithTranslator longEventProducerWithTranslator = new LongEventProducerWithTranslator(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l<100; l++) {


            bb.putLong(0, l);

            longEventProducerWithTranslator.onData(bb);

//            producer.onData(bb);
//            Thread.sleep(1000);
        }
    }

}
