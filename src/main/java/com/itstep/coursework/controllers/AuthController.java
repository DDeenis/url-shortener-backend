package com.itstep.coursework.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.itstep.coursework.db.dao.TokensDao;
import com.itstep.coursework.db.dao.UsersDao;
import com.itstep.coursework.db.dto.UserDto;
import com.itstep.coursework.db.entities.User;
import com.itstep.coursework.db.models.LoginFormModel;
import com.itstep.coursework.db.models.RegistrationFormModel;
import com.itstep.coursework.services.jwt.JwtService;
import com.itstep.coursework.services.utils.RequestHelpers;
import com.itstep.coursework.services.utils.ResponseHelpers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final UsersDao usersDao;
    private final TokensDao tokensDao;
    private final JwtService jwtService;

    private final int cookieMaxAge = 24 * 60 * 60;

    @Autowired
    public AuthController(UsersDao usersDao, TokensDao tokensDao, JwtService jwtService) {
        this.usersDao = usersDao;
        this.tokensDao = tokensDao;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "${api-prefix}/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> register(@RequestBody RegistrationFormModel formModel) {
        String missingField = formModel.getInvalidFields();

        if(missingField != null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Missing parameter: '%s'", missingField));
        }

        boolean isUsernameAvailable = usersDao.isUsernameAvailable(formModel.getUsername());

        if(!isUsernameAvailable) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.CONFLICT, "Username is already registered");
        }

        boolean isUserCreated = usersDao.create(formModel);

        if(!isUserCreated) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }

        User createdUser = usersDao.getByUsername(formModel.getUsername());

        if(createdUser == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }

        String token = tokensDao.create(createdUser.getId());

        if(token == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create token");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", String.format("token=%s; Path=/; SameSite=Strict; Secure; HttpOnly; Max-Age=%d;", token, cookieMaxAge));
        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, headers, new UserDto(createdUser));
    }

    @PostMapping(path = "${api-prefix}/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody LoginFormModel formModel) {
        String missingField = formModel.getInvalidFields();

        if(missingField != null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Missing parameter: '%s'", missingField));
        }

        User user = usersDao.getByUsername(formModel.getUsername());

        if(user == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        }

        boolean isPasswordValid = usersDao.verifyPassword(user, formModel.getPassword());

        if(!isPasswordValid) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        }

        String token = tokensDao.getByUserId(user.getId());

        if(token == null) {
            token = tokensDao.create(user.getId());
        }

        if(token == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create token");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", String.format("token=%s; Path=/; SameSite=Strict; Secure; HttpOnly; Max-Age=%d;", token, cookieMaxAge));
        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, headers, new UserDto(user));
    }

    @PostMapping(path = "${api-prefix}/logout")
    public ResponseEntity logout(HttpServletRequest req) {
        String token = RequestHelpers.getCookieValue(req, "token");
        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData != null) {
            tokensDao.remove(tokenData.getId());
        }

        return ResponseEntity.noContent().header("Set-Cookie", "token=none; Path=/; HttpOnly; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT").build();
    }
}
