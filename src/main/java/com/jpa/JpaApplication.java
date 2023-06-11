package com.jpa;

import com.jpa.ch1.dao.MemberService;
import com.jpa.ch1.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpaApplication {

    public static void main(String[] args) {


        SpringApplication.run(JpaApplication.class, args);
    }

}
