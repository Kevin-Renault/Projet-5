package com.openclassrooms.mddapi;

/**
 * Constantes utilisées uniquement pour les tests d'intégration et unitaires.
 * Centralise les valeurs de test pour éviter la duplication et faciliter la
 * maintenance.
 */
public final class TestConstants {
    private TestConstants() {
    }

    // Préfixe pour les utilisateurs de test
    public static final String TEST_USER_PREFIX = "it_to_erase_";
    // Domaine email pour les tests
    public static final String TEST_EMAIL_DOMAIN = "@example.com";
    // Mot de passe de test valide
    public static final String TEST_PASSWORD = "TestP@ssw0rd1";
    // Email invalide pour les tests de validation
    public static final String INVALID_EMAIL = "not-an-email";
    // Autres constantes spécifiques aux tests à ajouter ici
}
