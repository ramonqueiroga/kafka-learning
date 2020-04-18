package com.github.ramon.twitter;

import com.github.ramon.twitter.connect.Configuration;
import com.github.ramon.twitter.connect.TwitterClientBuilder;
import com.twitter.hbc.httpclient.BasicClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    private static final String TOPIC = "twitter-messages";
    private static final int CAPACITY = 10000;

    public static void main(String[] args) throws InterruptedException {
        KafkaProducer<String, String> producer = new KafkaProducer<>(Configuration.retrieveProducerConfig());
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(CAPACITY);

        TwitterClientBuilder twitterClientBuilder = new TwitterClientBuilder(queue);
        BasicClient client = twitterClientBuilder.build();
        client.connect();

        for (int i = 0; i < 1000; i++) {
            if(client.isDone()) {
                System.out.print("The client is inactive! " + client.getExitEvent().getMessage());
            }

            String message = queue.poll(5000, TimeUnit.MILLISECONDS);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
            producer.send(record);
            producer.flush();
        }

        client.stop();
        producer.close();

    }

}
