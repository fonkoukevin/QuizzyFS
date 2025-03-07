package com.quizzy.quizzy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
		"com.quizzy.quizzy.websocket",
		"com.quizzy.quizzy.controller",
		"com.quizzy.quizzy.service",
		"com.quizzy.quizzy.config",
		"com.quizzy.quizzy.repository",
		"com.quizzy.quizzy.dto",
		"com.quizzy.quizzy.entity"
})


@SpringBootApplication
public class QuizzyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizzyApplication.class, args);
	}

}
