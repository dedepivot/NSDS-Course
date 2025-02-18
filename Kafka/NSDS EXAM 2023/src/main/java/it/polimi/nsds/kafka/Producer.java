package it.polimi.nsds.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.Future;

public class Producer {
    private static final String defaultTopicA = "inputTopic";
    private static final String defaultTopicB = "inputTopic2";
    private static final String defaultTopicC = "inputTopic3";
    private static final int numMessages = 100000;
    private static final int waitBetweenMsgs = 500;
    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        final List<String> topics = new ArrayList<>();
        Collections.addAll(topics, defaultTopicA, defaultTopicB, defaultTopicC);
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

        //create a kafkaProducer<T, U> where T = type of the key, U = type of the value
        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(props);
        final Random r = new Random();
        for (int i = 0; i < numMessages; i++) {
            final String key = "Key" + r.nextInt(1000);
            final Integer value = r.nextInt(1000);
            String topic = topics.get(value % 3);
            System.out.println(
                    "Topic: " + topic +
                            "\tKey: " + key +
                            "\tValue: " + value
            );

            //create a record that contains: TOPIC - KEY - VALUE
            final ProducerRecord<String, Integer> record = new ProducerRecord<>(topic, key, value);
            //send the record to kafka
            final Future<RecordMetadata> future = producer.send(record);

            try {
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}
