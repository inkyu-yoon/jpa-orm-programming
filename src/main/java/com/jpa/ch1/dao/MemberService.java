package com.jpa.ch1.dao;

import com.jpa.ch1.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class MemberService {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            logic(em);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();

        }
    }
    public static void logic(EntityManager em) {

        Member member = Member.builder()
                .age(10)
                .id(1L)
                .build();

        em.persist(member);


        Member foundMember = em.find(Member.class, 1L);
        System.out.println(foundMember);

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        System.out.println(members.size());

        em.remove(member);
    }
}
