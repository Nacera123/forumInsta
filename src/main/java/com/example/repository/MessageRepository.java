package com.example.repository;

import com.example.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Message save(Message message) {
        String id = String.valueOf(redisTemplate.opsForValue().increment("message:next_id"));
        message.setId(id);
        message.setCreatedAt(LocalDateTime.now().toString());

        Map<String, String> fields = new HashMap<>();
        fields.put("id",        message.getId());
        fields.put("content",   message.getContent());
        fields.put("forumId",   message.getForumId());
        fields.put("creator", message.getCreator());
        fields.put("createdAt", message.getCreatedAt());

        redisTemplate.opsForHash().putAll("message:" + id, fields);
        redisTemplate.opsForList().leftPush("forum:" + message.getForumId() + ":messages", id);

        return message;
    }

    public Optional<Message> findById(String id) {
        Map<Object, Object> fields = redisTemplate.opsForHash().entries("message:" + id);
        if (fields.isEmpty()) return Optional.empty();
        return Optional.of(mapToMessage(fields));
    }

    public List<Message> findByForumId(String forumId) {
        List<String> ids = redisTemplate.opsForList().range("forum:" + forumId + ":messages", 0, -1);
        if (ids == null) return Collections.emptyList();
        return ids.stream()
                  .map(this::findById)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .collect(Collectors.toList());
    }

    // ── Likes ──────────────────────────────────────────────────────────────
    public boolean like(String messageId, String userId) {
        Long added = redisTemplate.opsForSet().add("message:" + messageId + ":likes", userId);
        return added != null && added > 0;
    }

    public boolean unlike(String messageId, String userId) {
        Long removed = redisTemplate.opsForSet().remove("message:" + messageId + ":likes", userId);
        return removed != null && removed > 0;
    }

    public long getLikeCount(String messageId) {
        Long count = redisTemplate.opsForSet().size("message:" + messageId + ":likes");
        return count != null ? count : 0;
    }

    public boolean hasLiked(String messageId, String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("message:" + messageId + ":likes", userId));
    }

    private Message mapToMessage(Map<Object, Object> fields) {
        Message message = new Message();
        message.setId((String)        fields.get("id"));
        message.setContent((String)   fields.get("content"));
        message.setForumId((String)   fields.get("forumId"));
        message.setCreator((String)    fields.get("creator"));
        message.setCreatedAt((String) fields.get("createdAt"));
        return message;
    }
}