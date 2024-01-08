package com.itstep.coursework.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.itstep.coursework.db.dao.TokensDao;
import com.itstep.coursework.db.dao.UsersDao;
import com.itstep.coursework.db.dto.UserDto;
import com.itstep.coursework.db.entities.User;
import com.itstep.coursework.db.models.ChangePasswordFormModel;
import com.itstep.coursework.db.models.EditProfileFormModel;
import com.itstep.coursework.services.utils.RequestHelpers;
import com.itstep.coursework.services.utils.ResponseHelpers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UsersDao usersDao;
    private final TokensDao tokensDao;

    private final int cookieMaxAge = 24 * 60 * 60;

    @Autowired
    public UserController(UsersDao usersDao, TokensDao tokensDao) {
        this.usersDao = usersDao;
        this.tokensDao = tokensDao;
    }

    @GetMapping(path = "${api-prefix}/profile")
    public ResponseEntity<String> getProfileInfo(HttpServletRequest req) {
        String token = RequestHelpers.getCookieValue(req, "token");
        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
        }

        String userId = tokenData.getSubject();

        if (userId == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
        }

        User user = usersDao.getById(userId);

        if(user == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.NOT_FOUND, String.format("User with id '%s' not found", userId));
        }

        UserDto userDto = new UserDto(user);
        return ResponseHelpers.sendJsonResponse(HttpStatus.OK, userDto);
    }

    @PutMapping(path = "${api-prefix}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateProfile(@RequestBody EditProfileFormModel formModel, HttpServletRequest req) {
        String invalidField = formModel.getInvalidFields();

        if(invalidField != null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.BAD_REQUEST, String.format("Missing field: '%s'", invalidField));
        }

        String token = RequestHelpers.getCookieValue(req, "token");
        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
        }

        String userId = tokenData.getSubject();

        if (userId == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
        }

        boolean isSuccess = usersDao.update(formModel, userId);

        if(!isSuccess) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
        }

        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "${api-prefix}/profile/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordFormModel formModel, HttpServletRequest req) {
        String invalidFields = formModel.getInvalidFields();

        if(invalidFields != null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.BAD_REQUEST, String.format("Missing field: '%s'", invalidFields));
        }

        String token = RequestHelpers.getCookieValue(req, "token");
        DecodedJWT tokenData = tokensDao.verifyAndDecode(token);

        if(tokenData == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Token is invalid");
        }

        String userId = tokenData.getSubject();

        if (userId == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "Unable to retrieve user id");
        }

        User user = usersDao.getById(userId);

        if (user == null) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.FORBIDDEN, "User not found");
        }

        if (!usersDao.verifyPassword(user, formModel.getCurrentPassword())) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.CONFLICT, "Passwords don't match");
        }

        boolean isSuccess = usersDao.updatePassword(user, formModel.getNewPassword());

        if(!isSuccess) {
            return ResponseHelpers.sendJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
        }

        tokensDao.removeAllForUser(userId);
        String newToken = tokensDao.create(userId);

        return ResponseEntity
                .noContent()
                .header("Set-Cookie", String.format("token=%s; Path=/; SameSite=Strict; Secure; HttpOnly; Max-Age=%d;", newToken, cookieMaxAge))
                .build();
    }
}
