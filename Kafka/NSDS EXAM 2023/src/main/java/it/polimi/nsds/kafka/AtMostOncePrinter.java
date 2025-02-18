package it.polimi.nsds.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AtMostOncePrinter {
    private static final String defaultGroupId = "group1";
    private static final String topic = "inputTopic";
    private static final String serverAddr = "localhost:9092";
    private static final int threshold = 500;
    
    public static void main(String[] args) {
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        final Properties consumerProp = new Properties();
        consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        //Allow the consumer to read only COMMITED msgs
        consumerProp.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        final KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProp);
        consumer.subscribe(Collections.singletonList(topic));

        final Properties producerProp = new Properties();
        producerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        //Set EOS property for the producer
        producerProp.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "forwarderTransactionalId");
        //Must be idempotent for the transaction to be enable
        producerProp.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));
        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProp);

        producer.initTransactions();
        while (true) {
            //pull the msg
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            //just use commit.sync() to do exactly this. ATOMICALLY COMMIT YOUR CURRENT OFFSET
            //begin transaction
            producer.beginTransaction();
            //For each PARTITION of the OutgoingTopic, store the next offset that will be used in the next transaction
            //Do that with a Map
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            //FOR ALL partitions present in the records that u pulled
            for (final TopicPartition partition : records.partitions()) {
                //Create a list of all the record of a given partition
                //Usually this list is composed of a single element after a PULL, but when u JOIN the list is composted of a multitude of element
                //When u "join - earliest" is guarantee of being a list of element | When u "join-latest" can be a list or a single element
                final List<ConsumerRecord<String, Integer>> partitionRecords = records.records(partition);
                //take the OFFSET of last msg of the given partition
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                //update the new offset that will be used
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }
            //Update the offset of each partition
            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            //close the transition
            producer.commitTransaction();

            for (final ConsumerRecord<String, Integer> incomingRecord : records) {
                if(incomingRecord.value()>=threshold){
                    System.out.println("Key: " + incomingRecord.key() + "\tValue > Threshold: " + incomingRecord.value());
                    System.out.println("offset: " + incomingRecord.offset());
                }
            }
        }
    }
}
