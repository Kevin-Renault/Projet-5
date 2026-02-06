-- Thèmes
DELETE FROM topic
where id < 10;
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
DELETE FROM mdd_user
where id < 10;
INSERT INTO mdd_user (id, username, email, password)
VALUES (
        1,
        'kevin',
        'kevin.renault@example.com',
        '$2a$10$iZOH0ffmjqjSWZxwUf8MkeJ6E9PvsaFJ0ssQRtoM5IboemlW1hTl2' -- Password-123_1
    ),
    (
        2,
        'alice',
        'alice.doe@example.com',
        '$2a$10$d2.CdQPcQ6JaqV6Ae2mEx.ByUgJ6Dr4aY4Qvtb8qIiIChepweVYIq'
    ),
    (
        3,
        'bob',
        'bob.smith@example.com',
        '$2a$10$rdihBFp.DLQuXHgIkLYpB.gw4H/jrbof.S2iq0x5I.5ty8Kz.vDry' -- Password-123_3
    ),
    (
        4,
        'charlie',
        'charlie.brown@example.com',
        '$2a$10$CR4mb.Lj8jQXK81EV8TNvub/4McFhd03uL74z1Xueu70rzs1lbfL.' -- Password-123_4
    ),
    (
        5,
        'david',
        'david.wilson@example.com',
        '$2a$10$4GXoy/7oI7JynF1viEwqJu.q2RVmjSBO2ImT2R.1O22PpbxsyJnFu' -- Password-123_5
    );
-- Abonnements
DELETE FROM user_topic_subscription
where true;
INSERT INTO user_topic_subscription (user_id, topic_id)
VALUES (1, 1),
    (1, 2),
    (2, 3),
    (3, 4);
-- Articles
DELETE FROM article
where id < 10;
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
DELETE FROM article_comment
where id < 10;
INSERT INTO article_comment (id, content, author_id, article_id)
VALUES (1, 'Merci pour cet article très complet !', 2, 1),
    (2, 'Je vais tester ça dès ce week-end.', 3, 1),
    (
        3,
        'As-tu un exemple de code pour illustrer ?',
        2,
        2
    );