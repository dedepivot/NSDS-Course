package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.Record;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

// Group number:
// Group members:

// Number of partitions for inputTopic (min, max): 1, N
// Number of partitions for outputTopic1 (min, max): 1, N
// Number of partitions for outputTopic2 (min, max): 1, N

// Number of instances of Consumer1 (and groupId of each instance) (min, max):
// min: 1, group Id set from the user, max N
// Number of instances of Consumer2 (and groupId of each instance) (min, max):
// min 1 groupID different from Consumer1 or msg delivery is not guarantee (they "stole" msgs from each other if there are in the same group)

// Please, specify below any relation between the number of partitions for the topics
// and the number of instances of each Consumer
//Each partition can be consumed by only one consumer in a group, so if we have N consumer (of type 1 and 2) in a specific group at least N partition for inputTopic must be at least N

public class ConsumersXX {
    public static void main(String[] args) {
        String serverAddr = "localhost:9092";
        int consumerId = Integer.valueOf(args[0]);
        String groupId = args[1];
        if (consumerId == 1) {
            Consumer1 consumer = new Consumer1(serverAddr, groupId);
            consumer.execute();
        } else if (consumerId == 2) {
            Consumer2 consumer = new Consumer2(serverAddr, groupId);
            consumer.execute();
        }
    }

    private static class Consumer1 {
        private final int windowSize = 10;
        private final String serverAddr;
        private final String consumerGroupId;
        private static final String inputTopic = "inputTopic";
        private static final String outputTopic = "outputTopic1";

        public Consumer1(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
            // TODO: add properties if needed

            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(inputTopic));
            System.out.println("start");
            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
            producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transactionalId");
            producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            // TODO: add properties if needed

            final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);
            // TODO: add code if needed
            producer.initTransactions();
            int sum = 0;
            int counter = 0;
            while (true) {
                producer.beginTransaction();
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
                for (final ConsumerRecord<String, Integer> record : records) {
                    System.out.println(record.value());
                    sum += record.value();
                    counter++;
                    if (counter >= windowSize) {
                        System.out.println("Sum of " + counter + " elements is " + sum);
                        producer.send(new ProducerRecord<>(outputTopic, "sum", sum));

                        final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
                        for (final TopicPartition partition : records.partitions()) {
                            final List<ConsumerRecord<String, Integer>> partitionRecords = records.records(partition);
                            final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                            map.put(partition, new OffsetAndMetadata(lastOffset + 1));
                        }
                        producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
                        sum = 0;
                        counter = 0;
                    }
                }
                producer.commitTransaction();
            }
        }
    }

    private static class Consumer2 {
        private final String serverAddr;
        private final String consumerGroupId;

        private static final String inputTopic = "inputTopic";
        private static final String outputTopic = "outputTopic1";

        private static final boolean autoCommit = true;
        private static final int autoCommitIntervalMs = 15000;
        private static final int windwSize = 10;

        private static final String offsetResetStrategy = "latest";

        public Consumer2(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

            // TODO: add properties if needed
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
            consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());


            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(inputTopic));

            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

            // TODO: add properties if needed

            // final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);

            // TODO: add code if needed
            Map<String, Integer> map = new HashMap<>();
            Map<String, Integer>  countMap = new HashMap<>();

            while (true) {
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
                for (final ConsumerRecord<String, Integer> record : records) {
                    // TODO: add code to process records
                    String key = record.key();
                    int value = record.value();
                    map.compute(key, (k, v) -> v == null ? value : v + value);
                    countMap.compute(key, (k, v) -> v == null ? 1 : v + 1);
                    int maxcount = countMap.values().stream().max(Integer::compareTo).orElse(0);
//                        countMap.entrySet()
//                                .stream()
//                                .forEach(System.out::println);
//                        System.out.println("\n");

                    if (maxcount >= windwSize) {
                        String maxKey = countMap.keySet().stream().filter(e -> e.equals(key)).findFirst().get();
                        map.entrySet()
                                .stream()
                                .filter(e -> e.getKey().equals(maxKey))
                                .forEach(System.out::println);
                        map.remove(maxKey);
                        countMap.remove(maxKey);
                    }

                }
            }
        }
    }
}