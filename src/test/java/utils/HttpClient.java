package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Http клиент для тестирования сервлетов
 */
public class HttpClient {
    private final java.net.http.HttpClient httpClient;

    public HttpClient() {
        httpClient = java.net.http.HttpClient.newBuilder()
                .build();
    }

    public ResponseDto getRequest(String url, Map<String, String> headers) {
        HttpRequest request = buildRequest(HttpMethod.GET, url, null, headers);
        return sendRequest(request);
    }

    public ResponseDto deleteRequest(String url, Map<String, String> headers) {
        HttpRequest request = buildRequest(HttpMethod.DELETE, url, null, headers);
        return sendRequest(request);
    }

    public ResponseDto postRequest(String url, String requestBody, Map<String, String> headers) {
        HttpRequest request = buildRequest(HttpMethod.POST, url, requestBody, headers);
        return sendRequest(request);
    }

    private ResponseDto sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseDto(response.body(), response.headers().map(), response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildRequest(HttpMethod httpMethod, String url, String requestBody, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url));

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        if (httpMethod == HttpMethod.POST) {
            if (requestBody == null) {
                builder.POST(HttpRequest.BodyPublishers.ofString(""));
            } else {
                builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            }
        } else if (httpMethod == HttpMethod.GET) {
            builder.GET();
        } else if (httpMethod == HttpMethod.DELETE) {
            builder.DELETE();
        }

        return builder.build();
    }
}
