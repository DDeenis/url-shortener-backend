package com.itstep.coursework.services.utils;

import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelpers {
    private static final Gson gson = new Gson();

    public static <T> ResponseEntity<String> sendJsonResponse(HttpStatus status, T body) {
        return ResponseEntity
                .status(status)
                .header("Content-Type", "application/json")
                .body(gson.toJson(body));
    }

    public static <T> ResponseEntity<String> sendTextResponse(HttpStatus status, String body) {
        return ResponseEntity
                .status(status)
                .header("Content-Type", "text/plain")
                .body(body);
    }

    public static <T> ResponseEntity<String> sendJsonResponse(HttpStatus status, HttpHeaders headers, T body) {
        if(!headers.containsKey("Content-Type")) {
            headers.add("Content-Type", "application/json");
        }

        return ResponseEntity
                .status(status)
                .headers(headers)
                .body(gson.toJson(body));
    }
}
