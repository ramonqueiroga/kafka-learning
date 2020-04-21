package com.github.ramon.twitter.connect;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Configuration {

    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
    private static final String REGEX    = "=";
    private static final String FILENAME = "keys.txt";
    private static final String ALL = "all";
    private static final String MAX_IN_FLIGHT_REQUESTS = "5";
    private static final String COMPRESSION = "lz4";

    static Properties retrieveAccessConfig() {
        InputStream inputStream = readFile();
        Scanner scanner = new Scanner(inputStream);

        Properties properties = new Properties();
        while(scanner.hasNext()) {
            String[] splitedLine = scanner.nextLine().split(REGEX);
            properties.put(splitedLine[0], splitedLine[1]);
        }

        return properties;
    }

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
        return properties;
    }

    private static InputStream readFile() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(FILENAME);
    }

}
