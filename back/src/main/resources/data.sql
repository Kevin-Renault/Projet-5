-- Données de test pour un MVP
-- Thèmes
INSERT INTO topic (name, description)
VALUES (
        'Java',
        'Discussions autour du langage Java et de son écosystème'
    ),
    (
        'Angular',
        'Framework front-end Angular et ses bonnes pratiques'
    ),
    (
        'Spring Boot',
        'Développement d''APIs avec Spring Boot'
    ),
    (
        'DevOps',
        'Intégration continue, déploiement et outils DevOps'
    );
-- Utilisateurs
INSERT INTO user (username, email, password)
VALUES (
        'kevin',
        'kevin.renault@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrYV5eLm8g7J5Z8x5Q0Bn1z3X4y7W6v2'
    ),
    -- Mot de passe : "password123" (à hasher en BCrypt en vrai)
    (
        'alice',
        'alice.doe@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrYV5eLm8g7J5Z8x5Q0Bn1z3X4y7W6v2'
    ),
    (
        'bob',
        'bob.smith@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrYV5eLm8g7J5Z8x5Q0Bn1z3X4y7W6v2'
    );
-- Abonnements
INSERT INTO user_topic_subscription (user_id, topic_id)
VALUES (1, 1),
    -- Kevin s'abonne à Java
    (1, 2),
    -- Kevin s'abonne à Angular
    (2, 3),
    -- Alice s'abonne à Spring Boot
    (3, 4);
-- Bob s'abonne à DevOps
-- Articles
INSERT INTO article (title, content, author_id, topic_id)
VALUES (
        'Introduction à Spring Boot 3',
        'Spring Boot 3 apporte des nouveautés comme la compatibilité avec Java 21...',
        1,
        3
    ),
    (
        'Les bonnes pratiques Angular',
        'Découvrez comment structurer une application Angular de manière scalable...',
        1,
        2
    ),
    (
        'Java 21 : les nouvelles fonctionnalités',
        'Tour d''horizon des nouveautés de Java 21, comme les Virtual Threads...',
        2,
        1
    );
-- Commentaires
INSERT INTO comment (content, author_id, article_id)
VALUES ('Merci pour cet article très complet !', 2, 1),
    ('Je vais tester ça dès ce week-end.', 3, 1),
    (
        'As-tu un exemple de code pour illustrer ?',
        2,
        2
    );