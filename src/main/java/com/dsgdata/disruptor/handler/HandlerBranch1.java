package com.dsgdata.disruptor.handler;

import com.dsgdata.disruptor.event.Trade;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

import java.util.UUID;

public class HandlerBranch1 implements EventHandler<Trade>, WorkHandler<Trade> {

    @Override
    public void onEvent(Trade event, long sequence, boolean endOfBatch) throws Exception {
        this.onEvent(event);
    }

    @Override
    public void onEvent(Trade event) throws Exception {
        event.setName("h1-"+ UUID.randomUUID());
        event.setPrice(17.0);
        System.out.println("HandlerBranch1: set name and Name"+event.getName());
    }

}
