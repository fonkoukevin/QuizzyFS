

package com.quizzy.quizzy.service;

import com.quizzy.quizzy.dto.UserDto;
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
        userRepository.findById(uid).orElseGet(() -> {
            User user = new User(uid, username);
            return userRepository.save(user);
        });
    }

    public Optional<UserDto> findUserById(String uid) {
        return userRepository.findById(uid)
                .map(user -> new UserDto(user.getUid(), "", user.getUsername()));
    }
}
