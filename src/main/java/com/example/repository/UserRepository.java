package com.example.repository;

import com.example.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public User save(User user) {
        String id = String.valueOf(redisTemplate.opsForValue().increment("user:next_id"));
        user.setId(id);

        Map<String, String> fields = new HashMap<>();
        fields.put("id",        user.getId());
        fields.put("username",  user.getUsername());
        fields.put("password",  user.getPassword());

        redisTemplate.opsForHash().putAll("user:" + id, fields);
        redisTemplate.opsForValue().set("user:username:" + user.getUsername(), id);

        return user;
    }

    public Optional<User> findById(String id) {
        Map<Object, Object> fields = redisTemplate.opsForHash().entries("user:" + id);
        if (fields.isEmpty()) return Optional.empty();
        return Optional.of(mapToUser(fields));
    }

    public Optional<User> findByUsername(String username) {
        String id = redisTemplate.opsForValue().get("user:username:" + username);
        if (id == null) return Optional.empty();
        return findById(id);
    }

    public boolean existsByUsername(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("user:username:" + username));
    }

    private User mapToUser(Map<Object, Object> fields) {
        User user = new User();
        user.setId((String)        fields.get("id"));
        user.setUsername((String)  fields.get("username"));
        user.setPassword((String)  fields.get("password"));
        return user;
    }
}