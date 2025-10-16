package com.example.spreado.domain.user.application;

import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.domain.user.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.createUser(email)));

        if (user.getName() == null && name != null)
            user.setName(name);

        return user;
    }

    @Transactional
    public void storeHashedRefreshToken(Long id, String hashed) {
        userRepository.storeHashedRefreshToken(id, hashed);
    }
}
