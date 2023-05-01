package com.leyou.httpdemo;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HttpTests {

    CloseableHttpClient httpClient;

    @Before
    public void init() {
        httpClient = HttpClients.createDefault();
    }

    @Test
    public void testGet() throws IOException {
        HttpGet request = new HttpGet("http://localhost/user/1");
        String response = this.httpClient.execute(request, new BasicResponseHandler());
        System.out.println(response);
    }

    @Test
    public void testPost() throws IOException {
        HttpPost request = new HttpPost("https://www.oschina.net/search?q=java&scope=project&fromerr=N3XyaihW");
        request.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        String response = this.httpClient.execute(request, new BasicResponseHandler());
        System.out.println(response);
    }

    @Test
    public void testGetPojo() throws IOException {
        HttpGet request = new HttpGet("http://localhost/hello");
        String response = this.httpClient.execute(request, new BasicResponseHandler());
        System.out.println(response);
    }
}
