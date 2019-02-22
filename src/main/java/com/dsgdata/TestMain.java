package com.dsgdata;

import com.dsgdata.utils.KafkaProperties;

public class TestMain {
    public static void main(String[] args) {
        KafkaConsumer consumerThread = new KafkaConsumer(KafkaProperties.topic);
        consumerThread.start();
    }
}
