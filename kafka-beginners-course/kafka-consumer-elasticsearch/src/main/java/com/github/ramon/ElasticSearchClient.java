package com.github.ramon;

import com.github.ramon.common.CredentialUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Properties;

public class ElasticSearchClient {
    private static final int PORT = 443;
    private static final String SCHEME = "https";
    private static final String FILENAME = "credentials.txt";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String HOSTNAME = "HOSTNAME";

    public static RestHighLevelClient createClient() {
        Properties properties = CredentialUtil.retrieveAccessConfig(FILENAME);
        String username = properties.getProperty(USERNAME);
        String password = properties.getProperty(PASSWORD);
        String hostname = properties.getProperty(HOSTNAME);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClient = RestClient.builder(new HttpHost(hostname, PORT, SCHEME))
		        .setHttpClientConfigCallback(builder -> builder.setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(restClient);
    }
}
