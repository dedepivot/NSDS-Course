package it.polimi.nsds.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PopularTopicConsumer {
    private static final String defaultGroupId = "group2";
    private static final String defaultTopicIncoming = "topicA";
    private static final String defaultTopicOutgoing = "topicB";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;
    private static final String offsetResetStrategy = "latest";

    private static final String defaultTopicA = "inputTopic";
    private static final String defaultTopicB = "inputTopic2";
    private static final String defaultTopicC = "inputTopic3";

    public static void main(String[] args) {
        final Properties consumerProp = new Properties();
        consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        consumerProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        consumerProp.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitIntervalMs);
        //consumerProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        final KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProp);
        consumer.subscribe(List.of(defaultTopicA, defaultTopicB, defaultTopicC));

        Map<String, Integer> hotTopic = new HashMap<>();
        hotTopic.put(defaultTopicA, 0);
        hotTopic.put(defaultTopicB, 0);
        hotTopic.put(defaultTopicC, 0);

        while (true) {
            //pull the msg
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Integer> incomingRecord : records) {
                hotTopic.compute(incomingRecord.topic(), (key, val) -> val + 1);
                Set<Map.Entry<String, Integer>> setOfEntry = hotTopic.entrySet();
                int possibleMax = 0;
                for(Map.Entry<String, Integer> entry : setOfEntry){
                    if(entry.getValue() > possibleMax){
                        possibleMax = entry.getValue();
                    }
                }
                final int max = possibleMax;
                hotTopic.entrySet().stream().filter(e -> e.getValue() == max).forEach(e-> {
                    System.out.println(e.getKey());
                });
            }
        }
    }
}
