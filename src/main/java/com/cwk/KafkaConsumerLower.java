package com.cwk;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.cluster.BrokerEndPoint;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 根据指定的topic,partiton,offset 获取数据
 */
public class KafkaConsumerLower {

    public static void main(String[] args) {
        //相关参数
        ArrayList<String> brokers = new ArrayList<String>();
        brokers.add("192.168.23.121");
        //brokers.add("cdh02");
        brokers.add("192.168.23.123");
        
        int port=9092;
        String topic="topic-dsg003";
        int partition=0;
        long offset=6;

        KafkaConsumerLower consumerLower = new KafkaConsumerLower();
        consumerLower.getData(brokers,port,topic,partition,offset);

    }

    private BrokerEndPoint findLeader(List<String> brokers, int port, String topic, int partition){
        for (String broker : brokers) {
            //获取分区leader的consumer对象
            SimpleConsumer getLeader = new SimpleConsumer(broker, port, 10000, 1024 * 4, "testdsg005");
            //主题元数据信息请求
            TopicMetadataRequest topicMetadataRequest = new TopicMetadataRequest(Collections.singletonList(topic));
            //获取主题元数据返回值
            TopicMetadataResponse topicMetadataResponse = getLeader.send(topicMetadataRequest);
            //解析返回值
            List<TopicMetadata> topicMetadata = topicMetadataResponse.topicsMetadata();
            for (TopicMetadata topicMetadatum : topicMetadata) {
                //获取多个分区的元数据信息
                List<PartitionMetadata> partitionMetadata = topicMetadatum.partitionsMetadata();
                for (PartitionMetadata partitionMetadatum : partitionMetadata) {
                    if (partitionMetadatum.partitionId()==partition)
                    return partitionMetadatum.leader();
                }
            }
        }

        return null;
    }

    private void getData(List<String> brokers, int port, String topic, int partition,long offset){

        BrokerEndPoint leader = findLeader(brokers, port, topic, partition);
        if (leader==null)
            return;
        SimpleConsumer getData = new SimpleConsumer(leader.host(), port, 1000, 1024 * 4, "testdsg005");
        FetchRequest fetchRequest = new FetchRequestBuilder().addFetch(topic, partition, offset, 1024 * 5).build();
        FetchResponse fetchResponse = getData.fetch(fetchRequest);
        ByteBufferMessageSet messageAndOffsets = fetchResponse.messageSet(topic, partition);
        for (MessageAndOffset messageAndOffset : messageAndOffsets) {
            long offset1 = messageAndOffset.offset();
            ByteBuffer payload = messageAndOffset.message().payload();
            byte[] bytes = new byte[payload.limit()];
            payload.get(bytes);
            System.out.println("receiving data:"+new String(bytes));
        }

    }

}
