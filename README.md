# forumInsta


modelisation sur redit

On commence par lire le skill Redis si tu en as un... mais ici on est sur de la modélisation pure, donc on y va directement.

---

## Modélisation Redis de ton forum

### 1. Compteurs d'IDs

```
INCR user:next_id      → génère un nouvel ID utilisateur
INCR forum:next_id     → génère un nouvel ID forum
INCR post:next_id      → génère un nouvel ID post
```

---

### 2. Utilisateur

```
HSET user:{id}
    id          1
    username    "alice"
    email       "alice@mail.com"
    password    "hash_bcrypt"
```

Pour retrouver un user par email (à la connexion) :
```
SET user:email:{email} {id}
# ex: SET user:email:alice@mail.com 1
```

---

### 3. Forum

```
HSET forum:{id}
    id          1
    title       "Général"
    description "Forum de discussion générale"
    created_by  {user_id}
    created_at  "2026-05-05T10:00:00"
```

Index de tous les forums (pour les lister) :
```
LPUSH forums {id}
```

---

### 4. Post

```
HSET post:{id}
    id          1
    title       "Mon premier post"
    content     "Bonjour tout le monde"
    forum_id    {forum_id}
    created_by  {user_id}
    created_at  "2026-05-05T10:05:00"
```

Index des posts d'un forum (pour les lister) :
```
LPUSH forum:{forum_id}:posts {post_id}
```

---

## Vue d'ensemble des clés

| Clé | Type | Contenu |
|---|---|---|
| `user:next_id` | String | Compteur auto-increment |
| `user:{id}` | Hash | Données utilisateur |
| `user:email:{email}` | String | ID de l'utilisateur |
| `forum:next_id` | String | Compteur auto-increment |
| `forum:{id}` | Hash | Données du forum |
| `forums` | List | Liste de tous les forum IDs |
| `post:next_id` | String | Compteur auto-increment |
| `post:{id}` | Hash | Données du post |
| `forum:{id}:posts` | List | Liste des post IDs d'un forum |

---

## Exemple de flux complet

**Connexion :**
```
GET user:email:alice@mail.com  → "1"
HGETALL user:1                 → { id, username, email, password }
# vérifier le mot de passe côté application
```

**Créer un post :**
```
INCR post:next_id              → 5
HSET post:5 title "..." content "..." forum_id 2 created_by 1 ...
LPUSH forum:2:posts 5
```

**Lister les posts d'un forum :**
```
LRANGE forum:2:posts 0 -1      → [5, 4, 3, 2, 1]
HGETALL post:5
HGETALL post:4
...
```

---
