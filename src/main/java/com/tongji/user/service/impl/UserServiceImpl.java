package com.tongji.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import com.tongji.user.service.UserService;
import com.tongji.user.mapper.UserMapper;
import com.tongji.user.domain.User;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * Find user by phone number.
     *
     * @param phone Phone number.
     * @return User Optional.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return Optional.ofNullable(userMapper.findByPhone(phone));
    }

    /**
     * Find user by email.
     *
     * @param email Email address.
     * @return User Optional.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.findByEmail(email));
    }

    /**
     * Find user by ID.
     *
     * @param id User ID.
     * @return User Optional.
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(long id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    /**
     * Check if phone number exists.
     *
     * @param phone Phone number.
     * @return Whether exists.
     */
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userMapper.existsByPhone(phone);
    }

    /**
     * Check if email exists.
     *
     * @param email Email address.
     * @return Whether exists.
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * Create user, write creation and update time and persist.
     *
     * @param user User entity to create.
     * @return Persisted user entity.
     */
    @Transactional
    public User createUser(User user) {
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    /**
     * Update user password hash and write update time.
     *
     * @param user User entity (must contain ID and new passwordHash).
     */
    @Transactional
    public void updatePassword(User user) {
        user.setUpdatedAt(Instant.now());
        userMapper.updatePassword(user.getId(), user.getPasswordHash());
    }
}
