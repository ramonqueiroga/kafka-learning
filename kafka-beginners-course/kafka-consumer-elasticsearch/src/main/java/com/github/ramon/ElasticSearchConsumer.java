package com.github.ramon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.github.ramon.ElasticSearchClient.createClient;

public class ElasticSearchConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchConsumer.class);
    private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
    private static final String TWITTER_GROUP = "twitter-group";
    private static final String OFFSET = "earliest";
    private static final String TOPIC = "twitter-messages";
    private static final String INDEX = "twitter";
    private static final String ID_STR = "id_str";

    public static void main(String[] args) throws IOException {
        KafkaConsumer<String, String> consumer = createConsumer();
        RestHighLevelClient client = createClient();

        while(true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
            int batchCount = consumerRecords.count();
            LOG.info("Received " +  batchCount + " records");

            BulkRequest bulkRequest = new BulkRequest();
            for (ConsumerRecord<String, String> record : consumerRecords) {
                String twitterMessage = record.value();
                //This guarantee the idempotent process. With the configured id we never duplicate the message in the elasticsearch if the consumer reads the same message again.
                String twitterId = extractFieldValue(twitterMessage, ID_STR);

                //if we dont inform a type in the IndexRequest, the default created type is "_doc", so you will find the data in "twitter/_docs/{id}
                IndexRequest request = new IndexRequest()
                                .index(INDEX)
                                .id(twitterId)
                                .source(twitterMessage, XContentType.JSON);

                bulkRequest.add(request);
            }

            if(batchCount > 0) {
                LOG.info("Sending batch messages");
                client.bulk(bulkRequest, RequestOptions.DEFAULT);
                LOG.info("Commiting offset");
                consumer.commitSync();
            }
        }

        //close the client gracefully
        //client.close();
    }

    private static String extractFieldValue(String jsonMessage, String field) {
        JsonElement jsonElement = JsonParser.parseString(jsonMessage);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.get(field).getAsString();
    }

    private static KafkaConsumer<String, String> createConsumer() {
        Properties consumerProperties = new Properties();
        consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, TWITTER_GROUP);
        consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET);
        //batches the poll messages in 10 by 10. Every processed batch, the offset is commited to the "__consumer_offsets"
        consumerProperties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        consumerProperties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProperties);
        kafkaConsumer.subscribe(Collections.singletonList(TOPIC));
        return kafkaConsumer;
    }

}
