package com.itstep.coursework.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itstep.coursework.db.dao.ShortUrlsDao;
import com.itstep.coursework.db.dao.TokensDao;
import com.itstep.coursework.db.entities.ShortUrl;
import com.itstep.coursework.db.entities.ShortUrlFilters;
import com.itstep.coursework.db.models.ShortenUrlModel;
import com.itstep.coursework.services.utils.RequestHelpers;
import com.itstep.coursework.services.utils.ResponseHelpers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class ShortUrlsController {
    private final ShortUrlsDao shortUrlsDao;
    private final TokensDao tokensDao;
    private final int shortUrlIdLength = 21;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    final Gson gson = new Gson();

    @Autowired
    public ShortUrlsController(ShortUrlsDao shortUrlsDao, TokensDao tokensDao) {
        this.shortUrlsDao = shortUrlsDao;
        this.tokensDao = tokensDao;
    }

    @PostMapping(path = "${api-prefix}/shorten", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> shortenUrl(@RequestBody ShortenUrlModel model, HttpServletRequest req) {
        String missingField = model.getInvalidFields();

        if(missingField != null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Missing parameter: '%s'", missingField));
        }

        String token = RequestHelpers.getCookieValue(req, "token");
        String userId = null;

        if(token != null && !token.isEmpty()) {
            DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

            if(tokenData == null) {
                return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
            }

            userId = tokenData.getSubject();

            if (userId == null) {
                return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
            }
        }

        ShortUrl shortedUrl = shortUrlsDao.create(model, userId);

        if(shortedUrl == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save shortened url");
        }

        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, shortedUrl);
    }

    @GetMapping(path = "${api-prefix}/url/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) {
        if(id.length() != shortUrlIdLength) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid id format");
        }

        ShortUrl shortUrl = shortUrlsDao.getById(id);

        if(shortUrl == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.NOT_FOUND, String.format("URL with id '%s' not found", id));
        }

        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, shortUrl);
    }

    @PutMapping(path = "${api-prefix}/url/{id}/toggle")
    public ResponseEntity<String> toggleDeactivated(@PathVariable String id) {
        ShortUrl shortUrl = shortUrlsDao.getById(id);

        if(shortUrl == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.NOT_FOUND, String.format("URL with id '%s' not found", id));
        }

        boolean deactivated = !shortUrl.isDeactivated();

        if (!shortUrlsDao.setDeactivated(id, deactivated)) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update URL deactivation status");
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("active", !deactivated);
        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, obj);
    }

    @GetMapping(path = "${api-prefix}/url")
    public ResponseEntity<String> getUrlsFiltered(
            HttpServletRequest req,
            @RequestParam(required = false) String query,
            @RequestParam() int page,
            @RequestParam() int pageSize,
            @RequestParam(required = false, name = "after") String afterDate
    ) {
        String token = RequestHelpers.getCookieValue(req, "token");

        if(token == null || token.isEmpty()) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNAUTHORIZED, "Auth token is empty");
        }

        Date after = null;
        if(afterDate != null) {
            try {
                after = dateFormatter.parse(afterDate);
            } catch (ParseException e) {
                return ResponseHelpers.sendJsonResponse(HttpStatus.BAD_REQUEST, "Date has incorrect format");
            }
        }

        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
        }

        String userId = tokenData.getSubject();

        if (userId == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
        }

        ShortUrlFilters filters = new ShortUrlFilters(userId, query, page, pageSize, after);
        ShortUrl[] urls = shortUrlsDao.getByFilters(filters);

        int size = urls.length > pageSize ? pageSize : urls.length;
        JsonArray data = new JsonArray();
        for (int i = 0; i < size; i++) {
            data.add(gson.toJsonTree(urls[i]));
        }
        boolean hasNext = urls.length > pageSize;
        JsonObject root = new JsonObject();
        JsonObject meta = new JsonObject();
        meta.addProperty("hasNext", hasNext);
        root.add("data", data);
        root.add("meta", meta);

        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, root);
    }

    @GetMapping("${api-prefix}/url/counters")
    public ResponseEntity<String> getCounters(HttpServletRequest req) {
        String token = RequestHelpers.getCookieValue(req, "token");

        if(token == null || token.isEmpty()) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNAUTHORIZED, "Auth token is empty");
        }

        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
        }

        String userId = tokenData.getSubject();

        if (userId == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
        }

        int counterAll = shortUrlsDao.countAll(userId);
        JsonObject counters = new JsonObject();
        counters.addProperty("all", counterAll);
        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, counters);
    }

    @GetMapping(path = "/s/{id}")
    public ResponseEntity<String> redirectToUrl(@PathVariable String id) {
        if(id.length() != shortUrlIdLength) {
            return ResponseHelpers.sendTextResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid id format");
        }

        ShortUrl shortUrl = shortUrlsDao.getById(id);

        if(shortUrl == null || shortUrl.isDeactivated()) {
            return ResponseHelpers.sendTextResponse(HttpStatus.NOT_FOUND, String.format("URL with id '%s' not found", id));
        }

        shortUrlsDao.incrementRedirects(id);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl.getOriginalUrl())).build();
    }
}
