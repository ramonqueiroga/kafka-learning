package com.github.ramon.twitter.tutorial1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static final String TOPIC = "first_topic";
    private static final Logger LOG = LoggerFactory.getLogger(ProducerDemoWithCallback.class);
    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";

    public static void main(String[] args) {
        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        for(int i = 0; i < 10; i++) {
            //create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, "Producer with callback: " + i);
            producer.send(record, (metadata, e) -> {
                if(e == null) {
                    LOG.info("Received metadata.  + \n" +
                                             "Topic: " + metadata.topic() + "\n" +
                                             "Partition: " + metadata.partition() + "\n" +
                                             "Offset: " + metadata.offset() + "\n" +
                                             "Timestamp: " + metadata.timestamp());
                } else {
                    LOG.error("An error has ocurred: " + e.getMessage());
                }
            });

            producer.flush();
        }

        producer.close();
    }

}
