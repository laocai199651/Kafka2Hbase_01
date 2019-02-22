package com.dsgdata.disruptor.handler;

import com.dsgdata.disruptor.event.Trade;
import com.lmax.disruptor.WorkHandler;

import java.util.UUID;

public class TradeHandler2 implements WorkHandler<Trade> {
    @Override
    public void onEvent(Trade event) throws Exception {
        //这里做具体的消费逻辑
        event.setId(UUID.randomUUID().toString());//简单生成下ID
        System.out.println(event.getId());
    }
}
