package com.cwk.disruptor.handler;

import com.cwk.disruptor.event.Trade;
import com.lmax.disruptor.EventHandler;

import java.util.UUID;

public class TradeHandler1 implements EventHandler<Trade> {
    @Override
    public void onEvent(Trade event, long sequence, boolean endOfBatch) throws Exception {
        //这里做具体的消费逻辑
        event.setId(UUID.randomUUID().toString());//简单生成下ID
        System.out.println(event.getId());
    }
}
