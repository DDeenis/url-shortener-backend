package com.itstep.coursework.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@RestController
public class HomeController {
    @RequestMapping(value = "/")
    public String index(HttpServletResponse resp) {
        URL indexFile = this.getClass().getClassLoader().getResource("static/index.html");

        if(indexFile == null) {
            resp.setStatus(404);
            resp.addHeader("Content-Type", "text/plain");
            return "Not found";
        }

        try {
            resp.setStatus(200);
            resp.addHeader("Content-Type", "text/html; charset=utf-8");
            return readFromInputStream(indexFile.openStream());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            resp.setStatus(500);
            resp.addHeader("Content-Type", "text/plain");
            return "Server error";
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
