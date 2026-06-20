package com.hollis.llm.springai.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-06-16 16:44
 **/
@RestController
@RequestMapping("/stream")
public class StreamController {

    private static final String API_KEY = "sk-ws-H.REHIDMR.CNbB.MEUCICLPGNjORqo690TPxr5hdFwUhFdIQek0Wz3lakGdPKZvAiEAnaolf593IuZY27Wo0PcTkFjJO8FjmXU7El9hdGtovLk";
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @RequestMapping("/fakeStream")
    public String fakeStream() {
        String requestBody = """
                {
                    "model": "qwen-plus",
                    "messages": [
                        {
                            "role": "system",
                            "content": "You are a helpful assistant."
                        },
                        {
                            "role": "user",
                            "content": "你好，介绍下JAVA？"
                        }
                    ],
                    "stream": true
                }
                """;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("X-DashScope-SSE", "enable")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body();
    }

    @RequestMapping("/sse")
    public SseEmitter sse() {
        SseEmitter sseEmitter = new SseEmitter(60_000L);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    sseEmitter.send("Message" + i);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            } finally {
                sseEmitter.complete();
            }

        });
        return sseEmitter;
    }


    @GetMapping("/sse/streaming")
    public ResponseEntity<StreamingResponseBody> chat() {
        StreamingResponseBody body = outputStream -> {
            for (int i = 0; i < 10; i++) {
                String data = "data chunk " + i + "\n";
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                try {
                    Thread.sleep(500); // 模拟延迟
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(body);
    }

    @GetMapping(value = "/sse/flux")
    public Flux<String> fluxStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> "Stream element - " + seq);
    }
}
