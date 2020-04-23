package com.github.ramon.twitter.connect;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class Configuration {

    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
    private static final String ALL = "all";
    private static final String MAX_IN_FLIGHT_REQUESTS = "5";
    private static final String COMPRESSION = "lz4";

    public static Properties retrieveProducerConfig() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //Configuring a safe producer. This config ensures that the producer will not duplicate information on resend the message to kafka
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, ALL);
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, MAX_IN_FLIGHT_REQUESTS);

        //configuring compression to the producer. This config is only producer level, no broker or consumer need to configure compression.
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION);

        //high throughput producer
        //linger producer wait configured milliseconds before send the messages to kafka (allows agroup messages in batch)
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        //the max size in KB that producer allows before send the message to kafka. If the max size is fulfilled before the linger configured ms, the message is sent to kafka
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, String.valueOf(32*1024));

        return properties;
    }

    private static InputStream readFile() {
        try {
            URL url = ClassLoader.getSystemResources("keys.txt").nextElement();
            return url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
