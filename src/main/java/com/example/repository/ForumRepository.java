package com.example.repository;

import com.example.model.Forum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ForumRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Forum save(Forum forum) {
        String id = String.valueOf(redisTemplate.opsForValue().increment("forum:next_id"));
        forum.setId(id);
        forum.setCreatedAt(LocalDateTime.now().toString());
        forum.setVisits(0);

        Map<String, String> fields = new HashMap<>();
        fields.put("id",          forum.getId());
        fields.put("title",       forum.getTitle());
        fields.put("creator",     forum.getCreator());
        fields.put("createdAt",   forum.getCreatedAt());
        fields.put("visits",      "0");

        redisTemplate.opsForHash().putAll("forum:" + id, fields);
        redisTemplate.opsForList().leftPush("forums", id);

        return forum;
    }

    public Optional<Forum> findById(String id) {
        Map<Object, Object> fields = redisTemplate.opsForHash().entries("forum:" + id);
        if (fields.isEmpty()) return Optional.empty();
        return Optional.of(mapToForum(fields));
    }

    public List<Forum> findAll() {
        List<String> ids = redisTemplate.opsForList().range("forums", 0, -1);
        if (ids == null) return Collections.emptyList();
        return ids.stream()
                  .map(this::findById)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .collect(Collectors.toList());
    }

    public long incrementVisits(String id) {
        return redisTemplate.opsForHash().increment("forum:" + id, "visits", 1);
    }

    private Forum mapToForum(Map<Object, Object> fields) {
        Forum forum = new Forum();
        forum.setId((String)          fields.get("id"));
        forum.setTitle((String)        fields.get("title"));
        forum.setCreator((String)      fields.get("creator"));
        forum.setCreatedAt((String)    fields.get("createdAt"));
        forum.setVisits(Long.parseLong((String) fields.get("visits")));
        return forum;
    }
}