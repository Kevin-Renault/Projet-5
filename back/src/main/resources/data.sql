-- Thèmes
DELETE FROM topic;
INSERT INTO topic (id, name, description)
VALUES (
        1,
        'Java',
        'Discussions autour du langage Java et de son écosystème'
    ),
    (
        2,
        'Angular',
        'Framework front-end Angular et ses bonnes pratiques'
    ),
    (
        3,
        'Spring Boot',
        'Développement d''APIs avec Spring Boot'
    ),
    (
        4,
        'DevOps',
        'Intégration continue, déploiement et outils DevOps'
    );
-- Utilisateurs
DELETE FROM mdd_user;
INSERT INTO mdd_user (id, username, email, password)
VALUES (
        1,
        'kevin',
        'kevin.renault@example.com',
        '$2a$10$IXh5EVr1YfmtMkOf1jSqNOwScIk56NfvYB4dif7owE2tHPY86GEKy'
    ),
    (
        2,
        'alice',
        'alice.doe@example.com',
        '$2a$10$IXh5EVr1YfmtMkOf1jSqNOwScIk56NfvYB4dif7owE2tHPY86GEKy'
    ),
    (
        3,
        'bob',
        'bob.smith@example.com',
        '$2a$10$IXh5EVr1YfmtMkOf1jSqNOwScIk56NfvYB4dif7owE2tHPY86GEKy'
    );
-- Abonnements
DELETE FROM user_topic_subscription;
INSERT INTO user_topic_subscription (user_id, topic_id)
VALUES (1, 1),
    (1, 2),
    (2, 3),
    (3, 4);
-- Articles
DELETE FROM article;
INSERT INTO article (id, title, content, author_id, topic_id)
VALUES (
        1,
        'Introduction à Spring Boot 3',
        'Spring Boot 3 apporte des nouveautés comme la compatibilité avec Java 21...',
        1,
        3
    ),
    (
        2,
        'Les bonnes pratiques Angular',
        'Découvrez comment structurer une application Angular de manière scalable...',
        1,
        2
    ),
    (
        3,
        'Java 21 : les nouvelles fonctionnalités',
        'Tour d''horizon des nouveautés de Java 21, comme les Virtual Threads...',
        2,
        1
    );
-- Commentaires
DELETE FROM article_comment;
INSERT INTO article_comment (id, content, author_id, article_id)
VALUES (1, 'Merci pour cet article très complet !', 2, 1),
    (2, 'Je vais tester ça dès ce week-end.', 3, 1),
    (
        3,
        'As-tu un exemple de code pour illustrer ?',
        2,
        2
    );