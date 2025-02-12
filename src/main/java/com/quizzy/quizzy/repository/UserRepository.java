package com.quizzy.quizzy.repository;

import com.quizzy.quizzy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
