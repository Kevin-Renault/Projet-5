-- Création des tables
CREATE TABLE IF NOT EXISTS topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS mdd_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Table de jointure pour les abonnements (Many-to-Many entre User et Topic)
CREATE TABLE IF NOT EXISTS user_topic_subscription (
    user_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, topic_id),
    FOREIGN KEY (user_id) REFERENCES mdd_user(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES mdd_user(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS article_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES mdd_user(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
);