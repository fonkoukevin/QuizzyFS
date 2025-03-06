package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.UserDto;
import com.quizzy.quizzy.dto.UserRequestDTO;
import com.quizzy.quizzy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
            JwtAuthenticationToken jwt,
            @RequestBody UserRequestDTO request) {

        if (jwt == null) {
            logger.error("❌ JWT is null. The request is unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getToken().getClaim("sub");
        logger.info("✅ Received Firebase UID: {}", uid);

        if (request.username() == null || request.username().isBlank()) {
            logger.error("❌ Username is missing in the request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        userService.saveUser(uid, request.username());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(JwtAuthenticationToken jwt) {
        if (jwt == null) {
            logger.error("❌ JWT is null. The request is unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getToken().getSubject();
        String email = jwt.getToken().getClaim("email");

        logger.info("✅ Received Firebase UID: {}, Email: {}", uid, email);

        Optional<UserDto> userEntityOptional = userService.findUserById(uid); // ✅ CORRECTION ICI

        if (userEntityOptional.isEmpty()) {
            logger.error("❌ User not found in the database for UID: {}", uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserDto user = userEntityOptional.get();

        return ResponseEntity.ok(Map.of(
                "uid", user.uid(),
                "email", email,
                "username", user.username()
        ));
    }
}
