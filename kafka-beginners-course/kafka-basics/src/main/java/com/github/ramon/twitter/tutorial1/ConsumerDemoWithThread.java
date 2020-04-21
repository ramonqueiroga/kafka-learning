package com.github.ramon.twitter.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThread {

    private static Logger LOG = LoggerFactory.getLogger(ConsumerDemoWithThread.class);
    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
    private static final String GROUP_ID = "my-group-2";
    private static final String TOPIC = "my-topic";
    private static final String OFFSET_CONFIG = "earliest";

    private ConsumerDemoWithThread() {}

    public static void main(String[] args) {
        new ConsumerDemoWithThread().run();
    }

    private void run() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_CONFIG);

        //latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        //create a runnable to the thread
        Runnable consumerRunnable = new ConsumerRunnable(latch, properties, TOPIC);

        //start the thread
        Thread thread = new Thread(consumerRunnable);
        thread.start();

        //add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Caught shutdown hook");
            ((ConsumerRunnable) consumerRunnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Application has exited");
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Application got interrupted", e);
        } finally {
            LOG.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {
        private Logger LOG = LoggerFactory.getLogger(ConsumerRunnable.class);
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;


        public ConsumerRunnable(CountDownLatch latch, Properties properties, String topic) {
            this.latch = latch;
            this.consumer = new KafkaConsumer<>(properties);
            this.consumer.subscribe(Collections.singletonList(topic));
        }

        @Override
        public void run() {
            try {
                while(true) {
                    //read the records from topic with timeout config of 100 millis
                    ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        LOG.info("Key: " + record.key() + ", Value: " + record.value());
                        LOG.info("Topic: " + record.topic() + ", Partition: " + record.partition() + ", Offset: " + record.offset());
                    }
                }
            } catch (WakeupException ex) {
                LOG.info("Received the shutdown signal!");
            } finally {
                this.consumer.close();
                this.latch.countDown();
            }
        }

        public void shutdown() {
            //wakeup is a especial method to interrupt the consumer.poll()
            //it will throw an exception called WakeUpException
            this.consumer.wakeup();

        }
    }

}
