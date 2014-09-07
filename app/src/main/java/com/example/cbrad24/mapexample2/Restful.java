package com.example.cbrad24.mapexample2;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by Cbrad24 on 7/09/2014.
 */
public class Restful {

    public void sendLocation() {
        String urlToSendRequest = "https://example.net";
        String targetDomain = "example.net";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpHost targetHost = new HttpHost(targetDomain, 80, "http");

        HttpPost httpPost = new HttpPost(urlToSendRequest);

        httpPost.addHeader("Content-Type", "application/xml");

        StringEntity entity = new StringEntity("<input>test</input>", "UTF-8");
        entity.setContentType("application/xml");
        httpPost.setEntity(entity);

        HttpResponse response = httpClient.execute(targetHost, httpPost);

        Reader r = new InputStreamReader(response.getEntity().getContent());
    }
}
