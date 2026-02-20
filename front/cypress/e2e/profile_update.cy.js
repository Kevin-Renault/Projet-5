// Test d'intégration : modification de profil et re-login

describe('Profile update and re-login', () => {
    const unique = () => Math.random().toString(36).substring(2, 10);
    const username = `it_${unique()}`;
    const email = `it_${unique()}@example.com`;
    const password = 'TestP@ssw0rd1';

    it('register, update profile, logout, re-login', () => {
        cy.intercept('GET', '/api/auth/csrf').as('csrf');
        cy.intercept('POST', '/api/auth/register').as('register');
        cy.intercept('PUT', '/api/users').as('updateUser');
        cy.intercept('POST', '/api/auth/login').as('login');

        // Register
        cy.visit('/user/register');
        cy.get('#username').type(username);
        cy.get('#email').type(email);
        cy.get('#password').type(password);
        cy.get('button[type="submit"]').click();
        cy.wait('@csrf');
        cy.wait('@register').its('response.statusCode').should('eq', 200);
        cy.url({ timeout: 20000 }).should('include', '/articles');

        // Go to profile
        cy.visit('/user/profile');
        const newUsername = `it2_${unique()}`;
        const newEmail = `it2_${unique()}@example.com`;
        cy.get('#username').clear().type(newUsername);
        cy.get('#email').clear().type(newEmail);
        cy.get('#password').clear().type(password);
        cy.contains('button[type="submit"]', 'Sauvegarder').click();
        cy.wait('@csrf');
        cy.wait('@updateUser').its('response.statusCode').should('eq', 200);
        cy.contains('Mise à jour réussie').should('exist');

        // Logout
        cy.contains('button', 'Se déconnecter').click();
        cy.url().should('match', /\/\/?$/);

        // Login with new email
        cy.visit('/user/login');
        cy.get('#login').type(newEmail);
        cy.get('#password').type(password);
        cy.get('button[type="submit"]').click();
        cy.wait('@csrf');
        cy.wait('@login').its('response.statusCode').should('eq', 200);
        cy.url({ timeout: 20000 }).should('include', '/articles');
    });
});
