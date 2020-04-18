package com.github.ramon.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemo {

    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
    private static final String GROUP_ID = "my-group";
    private static final String TOPIC = "my-topic";
    private static final String OFFSET_CONFIG = "earliest";
    private static Logger LOG = LoggerFactory.getLogger(ConsumerDemo.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
	properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_CONFIG);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        //subscribe consumer to our topic
        consumer.subscribe(Collections.singletonList(TOPIC));

        while(true) {
            //read the records from topic with timeout config of 100 millis
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                LOG.info("Key: " + record.key() + ", Value: " + record.value());
                LOG.info("Topic: " + record.topic() + ", Partition: " + record.partition() + ", Offset: " + record.offset());
            }
        }
    }

}
