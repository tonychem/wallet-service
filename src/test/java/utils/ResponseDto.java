package utils;

import java.util.List;
import java.util.Map;

public record ResponseDto(String responseBody, Map<String, List<String>> headers, int statusCode) {
}
