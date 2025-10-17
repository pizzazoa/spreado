package com.example.spreado.domain.user.core.repository;

import com.example.spreado.domain.user.core.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public Optional<User> findById(Long id) {
        return em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findByName(String name) {
        return em.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public void storeHashedRefreshToken(Long id, String hashed) {
        em.createQuery("UPDATE User u SET u.refreshToken = :hashed WHERE u.id = :id")
                .setParameter("hashed", hashed)
                .setParameter("id", id)
                .executeUpdate();
    }
}
