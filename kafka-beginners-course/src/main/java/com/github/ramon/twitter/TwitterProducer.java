package com.github.ramon.twitter;

import com.github.ramon.twitter.connect.Configuration;
import com.github.ramon.twitter.connect.TwitterClientBuilder;
import com.twitter.hbc.httpclient.BasicClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterProducer.class);
    private static final String TOPIC = "twitter-messages";
    private static final int CAPACITY = 1000;

    public static void main(String[] args) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(Configuration.retrieveProducerConfig());
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(CAPACITY);

        TwitterClientBuilder twitterClientBuilder = new TwitterClientBuilder(queue, "ohmygod");
        BasicClient client = twitterClientBuilder.build();
        client.connect();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("stopping application");
            LOG.info("shutting down client twitter");
            client.stop();
            LOG.info("closing kafka producer");
            producer.close();

        }));

        while(!client.isDone()) {
            String message = null;
            try {
                message = queue.poll(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if(message != null) {
                LOG.info("Captured message: " + message);
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
                producer.send(record, (metadata, e) -> {
                    if(e != null) {
                        LOG.error("Something happen with the message!");
                    }
                });
                producer.flush();
            } else {
                LOG.info("The message is null");
            }
        }
    }

}
