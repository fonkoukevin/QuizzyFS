package com.quizzy.quizzy.service;


import com.quizzy.quizzy.entity.User;
import com.quizzy.quizzy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(String uid, String username) {
        if (!userRepository.existsById(uid)) { //  Vérifie si l'utilisateur existe déjà
            User user = new User();
            user.setUid(uid);
            user.setUsername(username);
            userRepository.save(user);
        }
    }

    public Optional<User> findUserById(String uid) {
        return userRepository.findById(uid);
    }
}
