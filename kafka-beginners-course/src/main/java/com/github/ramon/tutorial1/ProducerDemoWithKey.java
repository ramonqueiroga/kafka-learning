package com.github.ramon.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKey {

    private static final String TOPIC = "my-topic";
    private static final Logger LOG = LoggerFactory.getLogger(ProducerDemoWithKey.class);
    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";

    public static void main(String[] args) throws InterruptedException {
        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        for(int i = 0; i < 10; i++) {
            //create a producer record
            String message = "Producer with key " + i;
            String key = "id_" + i;
            LOG.info("Key: " + key);

            ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, key, message);
            producer.send(record, (metadata, e) -> {
                if(e == null) {
                    LOG.info("Received metadata.  + \n" +
                                             "Topic: " + metadata.topic() + "\n" +
                                             "Partition: " + metadata.partition() + "\n" +
                                             "Offset: " + metadata.offset() + "\n" +
                                             "Timestamp: " + metadata.timestamp());
                } else {
                    LOG.error("An error has occurred: " + e.getMessage());
                }
            });

            Thread.sleep(1000);
            producer.flush();
        }

        producer.close();
    }

}
