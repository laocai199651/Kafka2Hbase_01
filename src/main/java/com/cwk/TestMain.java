package com.cwk;

import com.cwk.utils.KafkaProperties;

public class TestMain {
    public static void main(String[] args) {
        KafkaConsumer consumerThread = new KafkaConsumer(KafkaProperties.topic);
        consumerThread.start();
    }
}
