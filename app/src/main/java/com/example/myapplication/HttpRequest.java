package com.example.myapplication;

import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String baseUrl = "";

    public String sendRequest(String requestUrl, String method, Map<String, String> headers, String jsonParams) throws IOException {
        String fullUrl = baseUrl + requestUrl;

        Headers.Builder headersBuilder = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        // 创建请求体
        RequestBody body = RequestBody.create(jsonParams, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(fullUrl)
                .headers(headersBuilder.build())
                .method(method, body)
                .build();


        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body
            return response.body().string();
        }
    }
}