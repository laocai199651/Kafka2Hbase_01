package com.dsgdata.disruptor.handler;

import com.dsgdata.disruptor.event.Trade;
import com.lmax.disruptor.WorkHandler;

public class Trade_Handler2_01 implements WorkHandler<Trade> {
    @Override
    public void onEvent(Trade event) throws Exception {
        //这里做具体的消费逻辑
        //event.setId(UUID.randomUUID().toString());//简单生成下ID
        //System.out.println(event.getId());
        System.out.println(Thread.currentThread().getThreadGroup().getName()+"-"+Thread.currentThread().getName());
    }
}
