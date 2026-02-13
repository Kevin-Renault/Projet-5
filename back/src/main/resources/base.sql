-- 1. Remplacez 'ton_mot_de_passe' par un vrai mot de passe fort de votre choix.
-- 2. Exécutez ce script dans MySQL pour créer la base et l'utilisateur avec les droits CRUD.
-- 3. Enregistrez les informations suivantes dans des variables d'environnement côté application Spring Boot :
--    - DB_USER : le nom d'utilisateur MySQL (ex : kevin)
--    - DB_PASSWORD : le mot de passe MySQL choisi  (ex : votre_mot_de_passe)
--    - DB_MDD_NAME : le nom de la base de données (ex : mdd_db)
--    Sous Windows, deux possibilités pour chaque variable :
--      - Pour la session courante (temporaire) :
--          set DB_USER="kevin"
--          set DB_PASSWORD="votre_mot_de_passe"
--          set DB_MDD_NAME="mdd_db"
--      - Pour toutes les futures sessions (persistant) :
--          setx DB_USER "kevin"
--          setx DB_PASSWORD "votre_mot_de_passe"
--          setx DB_MDD_NAME "mdd_db"
CREATE DATABASE mdd_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'kevin' @'localhost' IDENTIFIED BY 'ton_mot_de_passe';
GRANT SELECT,
    INSERT,
    UPDATE,
    DELETE,
    ALTER,
    CREATE,
    DROP,
    REFERENCES ON mdd_db.* TO 'kevin' @'localhost';
FLUSH PRIVILEGES;