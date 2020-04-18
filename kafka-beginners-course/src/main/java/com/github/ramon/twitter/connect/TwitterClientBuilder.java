package com.github.ramon.twitter.connect;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class TwitterClientBuilder {

    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String API_TOKEN = "API_TOKEN";
    private static final String API_TOKEN_SECRET = "API_TOKEN_SECRET";
    private static final String SAMPLE_EXAMPLE_CLIENT = "twitterClientSample";

    private BlockingQueue<String> queue;
    private String subject;

    public TwitterClientBuilder(BlockingQueue<String> queue, String subject) {
	this.queue = queue;
	this.subject = subject;
    }

    public BasicClient build() {
        Properties accessConfig = Configuration.retrieveAccessConfig();
        String apiKey = accessConfig.getProperty(API_KEY);
        String apiSecret = accessConfig.getProperty(API_SECRET);
        String apiToken = accessConfig.getProperty(API_TOKEN);
        String apiTokenSecret = accessConfig.getProperty(API_TOKEN_SECRET);

        Authentication auth = new OAuth1(apiKey, apiSecret, apiToken, apiTokenSecret);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        endpoint.trackTerms(Collections.singletonList(subject));

        // Create a new BasicClient. By default gzip is enabled.
        return new ClientBuilder()
		        .name(SAMPLE_EXAMPLE_CLIENT)
		        .hosts(Constants.STREAM_HOST)
		        .endpoint(endpoint)
		        .authentication(auth)
		        .processor(new StringDelimitedProcessor(queue))
		        .build();
    }

}
