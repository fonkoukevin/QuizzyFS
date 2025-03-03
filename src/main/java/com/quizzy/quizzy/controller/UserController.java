package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.UserRequestDTO;
import com.quizzy.quizzy.entity.User;
import com.quizzy.quizzy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> registerUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserRequestDTO request) { // ✅ Use DTO

        if (jwt == null) {
            logger.error("❌ JWT is null. The request is unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getClaim("sub"); // ✅ Extract Firebase UID
        logger.info("✅ Received Firebase UID: {}", uid);

        String username = request.getUsername(); // ✅ Extract correct field

        if (username == null || username.isBlank()) {
            logger.error("❌ Username is missing in the request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        userService.saveUser(uid, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            logger.error("❌ JWT is null. The request is unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ Extract UID & Email from Firebase Token
        String uid = jwt.getSubject(); // Firebase UID
        String email = jwt.getClaim("email"); // Firebase Email

        logger.info("✅ Received Firebase UID: {}, Email: {}", uid, email);

        // ✅ Retrieve the user from the database
        Optional<User> userEntityOptional = userService.findUserById(uid);

        if (userEntityOptional.isEmpty()) {
            logger.error("❌ User not found in the database for UID: {}", uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userEntityOptional.get();

        System.out.println(Map.of(
                "uid", uid,
                "email", email,
                "username", user.getUsername()
        ));

        // ✅ Return User Data
        return ResponseEntity.ok(Map.of(
                "uid", uid,
                "email", email,
                "username", user.getUsername()
        ));
    }

}
