package com.dsgdata;

import com.dsgdata.utils.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerNew {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaProperties.kafkaServerURL+":"+KafkaProperties.kafkaServerPort);
        props.put("zookeeper.connect",KafkaProperties.zkConnect);
        props.put("group.id", KafkaProperties.groupId+"sdasd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "10000");
        props.put("auto.offset.reset",KafkaProperties.autoOffsetReset1);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<Object, Object> kafkaConsumer = new KafkaConsumer(props);
        //kafkaConsumer.close();
        kafkaConsumer.subscribe(Collections.singletonList("topic-quick-ack"));
        while (true) {
            ConsumerRecords<Object, Object> consumerRecords = kafkaConsumer.poll(1000);
            for (ConsumerRecord<Object, Object> consumerRecord : consumerRecords) {
                System.out.println("received data：" + (consumerRecord.key() == null ? "" : (consumerRecord.key() + ":")) + consumerRecord.value());
            }
        }
    }
}
