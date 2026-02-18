SET FOREIGN_KEY_CHECKS = 0;
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
    ) ON DUPLICATE KEY
UPDATE description =
VALUES(description);
-- Utilisateurs
INSERT INTO mdd_user (username, email, password)
VALUES (
        'kevin',
        'kevin.renault@example.com',
        '$2a$10$iZOH0ffmjqjSWZxwUf8MkeJ6E9PvsaFJ0ssQRtoM5IboemlW1hTl2'
    ),
    (
        'alice',
        'alice.doe@example.com',
        '$2a$10$d2.CdQPcQ6JaqV6Ae2mEx.ByUgJ6Dr4aY4Qvtb8qIiIChepweVYIq'
    ),
    (
        'bob',
        'bob.smith@example.com',
        '$2a$10$rdihBFp.DLQuXHgIkLYpB.gw4H/jrbof.S2iq0x5I.5ty8Kz.vDry'
    ),
    (
        'charlie',
        'charlie.brown@example.com',
        '$2a$10$CR4mb.Lj8jQXK81EV8TNvub/4McFhd03uL74z1Xueu70rzs1lbfL.'
    ),
    (
        'david',
        'david.wilson@example.com',
        '$2a$10$4GXoy/7oI7JynF1viEwqJu.q2RVmjSBO2ImT2R.1O22PpbxsyJnFu'
    ) ON DUPLICATE KEY
UPDATE email =
VALUES(email),
    password =
VALUES(password);
-- Abonnements
DELETE FROM user_topic_subscription;
INSERT INTO user_topic_subscription (user_id, topic_id)
SELECT u.id,
    t.id
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'Java'
UNION ALL
SELECT u.id,
    t.id
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'Angular'
UNION ALL
SELECT u.id,
    t.id
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'alice'
    AND t.name = 'Spring Boot'
UNION ALL
SELECT u.id,
    t.id
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'bob'
    AND t.name = 'DevOps';
-- Articles
DELETE FROM article;
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT 'Java 21 : Les Virtual Threads expliqués',
    'Les Virtual Threads, introduits dans Java 21, permettent de gérer un grand nombre de tâches concurrentes avec une faible consommation de mémoire. Contrairement aux threads classiques, ils sont légers et gérés par la JVM, ce qui les rend idéaux pour les applications nécessitant une haute concurrence, comme les serveurs web. Leur principal avantage est de simplifier le code asynchrone sans bloquer les ressources système. Ils sont particulièrement utiles pour les opérations d''entrée/sortie, où les threads traditionnels peuvent devenir un goulot d''étranglement. Cette innovation réduit la complexité du code tout en améliorant les performances.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'Java';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT 'Les Records en Java : Une révolution pour les DTOs',
    'Les Records,
        apparus en Java 16,
        offrent une syntaxe concise pour créer des classes immuables.Ils éliminent le besoin d’écrire manuellement les méthodes equals(),
        hashCode() et toString(),
        tout en garantissant l’immuabilité des données.Cela les rend parfaits pour les objets de transfert de données (DTOs) ou les valeurs immuables.Leur utilisation simplifie le code et réduit les erreurs,
        tout en améliorant la lisibilité.Les Records sont idéaux pour les réponses API ou les objets de configuration.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'alice'
    AND t.name = 'Java';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT 'Pattern Matching en Java: Une nouvelle façon de coder ',
    'Le Pattern Matching, amélioré dans Java 21, permet d''écrire du code plus expressif et concis. Grâce aux switch expressions et à l''amélioration de l''opérateur instanceof, les développeurs peuvent maintenant manipuler les objets de manière plus intuitive. Cette fonctionnalité réduit les casts manuels et les vérifications de type répétitives, rendant le code plus lisible et moins sujet aux erreurs. Elle est particulièrement utile pour les structures de données complexes ou les hiérarchies de classes.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'bob'
    AND t.name = 'Java';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT 'Les Modules Java : Pour une meilleure architecture',
    'Les modules,
        introduits en Java 9,
        permettent de structurer les applications en composants indépendants avec des dépendances explicites.Cela améliore l’encapsulation,
        la maintenabilité et la sécurité des applications.Les modules facilitent également la réutilisation du code et la gestion des dépendances,
        ce qui est essentiel pour les grandes applications ou les bibliothèques partagées.Ils aident à éviter les conflits de noms et à clarifier les dépendances entre les différentes parties d’une application.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'charlie'
    AND t.name = 'Java';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Optimisation des performances avec JMH ',
    'Java Microbenchmark Harness (JMH) est un outil puissant pour mesurer les performances du code Java.Il permet d’éviter les pièges courants des benchmarks,
        comme les optimisations prématurées du compilateur JIT.En utilisant JMH,
        les développeurs peuvent identifier les goulots d’étranglement et optimiser les parties critiques de leurs applications.Cet outil est indispensable pour tout projet où les performances sont un enjeu majeur.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'david'
    AND t.name = 'Java';
-- Articles pour le topic Angular
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Angular 17: Les innovations majeures ',
    'Angular 17 introduit des améliorations significatives,
        comme les nouveaux contrôles de flux @if et @for,
        qui remplacent avantageusement les directives * ngIf et * ngFor.Cette version met également l’accent sur les composants standalone,
        qui simplifient la création d’applications sans nécessiter de modules NgModule.Les performances ont été optimisées,
        et le nouveau compilateur réduit le temps de chargement des applications.Ces changements rendent Angular plus moderne et plus facile à utiliser.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'Angular';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Architecture Angular: Bonnes pratiques ',
    'Une architecture bien conçue est essentielle pour créer des applications Angular évolutives.Il est recommandé de séparer les composants en "smart" et "dumb",
        d’utiliser des services pour la logique métier et de centraliser la gestion d’état avec des outils comme NgRx.Une structure de projet claire,
        avec des modules fonctionnels bien définis,
        facilite la maintenance et l’évolution de l’application.Ces pratiques permettent de gérer la complexité et d’améliorer la collaboration au sein des équipes.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'alice'
    AND t.name = 'Angular';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Angular Material: Des interfaces modernes et accessibles ',
    'Angular Material est une bibliothèque de composants UI qui permet de créer des interfaces utilisateur professionnelles et accessibles.Elle offre une large gamme de composants prêts à l’emploi,
        comme des tableaux avancés,
        des dialogues modaux et des formulaires stylisés.Ces composants sont conçus pour être responsives et accessibles,
        ce qui améliore l’expérience utilisateur.Leur utilisation garantit une cohérence visuelle et une meilleure productivité pour les développeurs.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'bob'
    AND t.name = 'Angular';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Formulaires réactifs en Angular: Guide pratique ',
    'Les formulaires réactifs en Angular offrent un contrôle total sur la validation et la gestion des données.Ils permettent de créer des formulaires complexes avec une validation en temps réel et une gestion fine des erreurs.Contrairement aux formulaires template - driven,
        les formulaires réactifs sont plus flexibles et mieux adaptés aux applications dynamiques.Ils sont idéaux pour les formulaires multi - étapes ou ceux nécessitant une validation avancée.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'charlie'
    AND t.name = 'Angular';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Optimisation des performances en Angular ',
    'Pour améliorer les performances d’une application Angular,
        il est conseillé d’utiliser la stratégie de détection de changements OnPush,
        de charger les modules de manière paresseuse et d’éviter les fuites de mémoire.L’utilisation de trackBy dans les boucles * ngFor et l’optimisation des requêtes HTTP avec des interceptors peuvent également réduire significativement les temps de chargement.Ces techniques sont essentielles pour offrir une expérience utilisateur fluide.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'david'
    AND t.name = 'Angular';
-- Articles pour le topic Spring Boot
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Spring Boot 3: Migration et avantages ',
    'Spring Boot 3 apporte des changements majeurs,
        comme le passage de javax.* à jakarta.*,
        une meilleure gestion des propriétés et un nouveau système de logging.Cette version est conçue pour fonctionner avec Java 17 et offre des améliorations de performances et de sécurité.La migration vers Spring Boot 3 permet de bénéficier d’un support long terme et des dernières innovations du framework.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'Spring Boot';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Sécurité avec Spring Security 6 et JWT ',
    'Spring Security 6 simplifie la configuration de la sécurité et introduit des améliorations pour la gestion des tokens JWT.Les tokens JWT permettent de sécuriser les APIs de manière stateless,
        ce qui est idéal pour les applications modernes.Il est recommandé d’utiliser des cookies HttpOnly pour stocker les tokens et de limiter leur durée de validité.Une bonne implémentation de la sécurité est cruciale pour protéger les données sensibles.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'alice'
    AND t.name = 'Spring Boot';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Spring Data JPA: Bonnes pratiques ',
    'Spring Data JPA facilite l’accès aux bases de données relationnelles en réduisant la quantité de code nécessaire.Pour optimiser les performances,
        il est conseillé d’utiliser des requêtes personnalisées,
        d’éviter les problèmes de N + 1 avec des jointures ou des graphes d’entités,
        et de limiter les données retournées avec des projections.Ces pratiques améliorent les performances et la maintenabilité des applications.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'bob'
    AND t.name = 'Spring Boot';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Déploiement de Spring Boot avec Docker ',
    'Docker permet de conteneuriser les applications Spring Boot pour un déploiement simplifié et reproductible.En utilisant des images légères et en configurant correctement les variables d’environnement,
        les développeurs peuvent déployer leurs applications dans différents environnements sans modification.Docker compose est particulièrement utile pour gérer les services dépendants,
        comme les bases de données.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'charlie'
    AND t.name = 'Spring Boot';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Monitoring avec Spring Boot Actuator ',
    'Spring Boot Actuator fournit des endpoints pour surveiller et gérer les applications en production.Il permet de vérifier l’état de santé de l’application,
        de collecter des métriques et d’accéder à des informations système.Ces fonctionnalités sont essentielles pour le débogage et l’optimisation des performances.Actuator peut être personnalisé pour exposer uniquement les informations nécessaires.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'david'
    AND t.name = 'Spring Boot';
-- Articles pour le topic DevOps
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' CI / CD avec GitHub Actions ',
    'GitHub Actions permet d’automatiser les pipelines de build,
        de test et de déploiement directement depuis un dépôt GitHub.Cet outil est facile à configurer et s’intègre parfaitement avec l’écosystème GitHub.Il permet de définir des workflows pour exécuter des tests,
        construire des artefacts et déployer des applications à chaque modification du code.L’automatisation des processus CI / CD améliore la qualité du logiciel et accélère les livraisons.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'kevin'
    AND t.name = 'DevOps';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Docker et Kubernetes pour le déploiement ',
    'Kubernetes est une plateforme puissante pour orchestrer des conteneurs Docker à grande échelle.Elle permet de gérer automatiquement le déploiement,
        la mise à l’échelle et la disponibilité des applications.Les bonnes pratiques incluent l’utilisation de sondes de santé,
        la configuration des ressources et la gestion des secrets.Kubernetes est idéal pour les applications critiques nécessitant une haute disponibilité.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'alice'
    AND t.name = 'DevOps';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Monitoring avec Prometheus et Grafana ',
    'Prometheus et Grafana forment une stack de monitoring complète pour surveiller les applications et  l’infrastructure.Prometheus collecte les métriques,
        tandis que Grafana les visualise sous forme de tableaux de bord personnalisables.Cette combinaison permet de détecter rapidement les problèmes et d’optimiser les performances.Elle est largement utilisée dans les environnements de production.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'bob'
    AND t.name = 'DevOps';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Infrastructure as Code avec Terraform ',
    'Terraform permet de définir et de provisionner une infrastructure de manière déclarative.Cela facilite la gestion des ressources cloud et garantit la reproductibilité des environnements.Les fichiers de configuration Terraform peuvent être versionnés et partagés,
        ce qui améliore la collaboration et réduit les erreurs de configuration.Cet outil est indispensable pour les équipes DevOps.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'charlie'
    AND t.name = 'DevOps';
INSERT INTO article (title, content, author_id, topic_id, created_at)
SELECT ' Sécurité DevOps: Bonnes pratiques ',
    'Intégrer la sécurité dès le début du cycle DevOps est essentiel pour protéger les applications et les données.Cela inclut l’analyse statique du code,
        le scan des vulnérabilités dans les dépendances,
        et la gestion sécurisée des secrets.Des outils comme SonarQube,
        Trivy et HashiCorp Vault aident à automatiser ces processus et à se conformer aux réglementations en vigueur.',
    u.id,
    t.id,
    TIMESTAMPADD(
        SECOND,
        - FLOOR(RAND() * 60 * 60 * 24 * 120),
        CURRENT_TIMESTAMP
    )
FROM mdd_user u
    JOIN topic t
WHERE u.username = 'david'
    AND t.name = 'DevOps';
-- Commentaires
DELETE FROM article_comment;
INSERT INTO article_comment (content, author_id, article_id)
SELECT 'Merci pour cet article très complet !',
    u.id,
    a.id
FROM mdd_user u
    JOIN article a
WHERE u.username = 'alice'
    AND a.title = 'Java 21 : Les Virtual Threads expliqués';
INSERT INTO article_comment (content, author_id, article_id)
SELECT 'Je vais tester ça dès ce week-end.',
    u.id,
    a.id
FROM mdd_user u
    JOIN article a
WHERE u.username = 'bob'
    AND a.title = 'Java 21 : Les Virtual Threads expliqués';
INSERT INTO article_comment (content, author_id, article_id)
SELECT 'As-tu un exemple de code pour illustrer ?',
    u.id,
    a.id
FROM mdd_user u
    JOIN article a
WHERE u.username = 'alice'
    AND a.title = 'Les Records en Java : Une révolution pour les DTOs';
SET FOREIGN_KEY_CHECKS = 1;