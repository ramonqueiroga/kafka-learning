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
        return properties;
    }

    private static InputStream readFile() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(FILENAME);
    }

}
