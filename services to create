## AuthService.java

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }
}
```

---

## ForumService.java

```java
@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumRepository forumRepository;

    public List<Forum> getAllForums() {
        return forumRepository.findAll();
    }

    public Forum getForumById(String id) {
        forumRepository.incrementVisits(id);
        return forumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum introuvable"));
    }

    public Forum createForum(String title, String description, String userId) {
        Forum forum = new Forum();
        forum.setTitle(title);
        forum.setDescription(description);
        forum.setCreatedBy(userId);
        return forumRepository.save(forum);
    }
}
```

---

## PostService.java

```java
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ForumRepository forumRepository;

    public List<Post> getPostsByForum(String forumId) {
        forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum introuvable"));
        return postRepository.findByForumId(forumId);
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post introuvable"));
    }

    public Post createPost(String title, String content, String forumId, String userId) {
        forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum introuvable"));

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setForumId(forumId);
        post.setCreatedBy(userId);
        return postRepository.save(post);
    }

    public long likePost(String postId, String userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post introuvable"));

        if (postRepository.hasLiked(postId, userId)) {
            postRepository.unlike(postId, userId);
        } else {
            postRepository.like(postId, userId);
        }

        return postRepository.getLikeCount(postId);
    }
}
```

---

## JwtUtil.java

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

Les services sont prêts. On passe aux **Controllers** (les routes HTTP) ?
