package ru.ssau.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
        //System.out.println("JWT Secret is: " + System.getenv("JWT_SECRET"));
    }

}
