/// <reference types="cypress" />
// Test d'intégration complet : inscription, login, navigation, commentaire, logout
import { USER_PREFIX } from '../support/commands';

describe('Full integration flow', () => {
    const unique = () => `${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
    const username = `${USER_PREFIX}${unique()}`;
    const email = `${USER_PREFIX}${unique()}@example.com`;
    const password = 'TestP@ssw0rd1';

    it('register, login, comment, logout', () => {
        cy.intercept('GET', '/api/auth/csrf').as('csrf');
        cy.intercept('POST', '/api/auth/register').as('register');
        cy.intercept('POST', '/api/auth/login').as('login');
        cy.intercept('GET', '/api/topics').as('topics');
        cy.intercept('GET', '/api/articles').as('articles');
        cy.intercept('POST', '/api/comments').as('createComment');

        // Register
        cy.register(username, email, password);
        cy.wait('@register').its('response.statusCode').should('eq', 200);
        cy.url({ timeout: 20000 }).should('include', '/articles');
        cy.get('nav').should('exist');

        // Logout
        cy.logout();
        cy.location('pathname').should('be.oneOf', ['/', '/user/login']);

        // Login
        cy.login(email, password);
        cy.wait('@login').its('response.statusCode').should('eq', 200);
        cy.url({ timeout: 20000 }).should('include', '/articles');
        cy.get('nav').should('exist');

        // Go to topics and subscribe
        cy.subscribeToFirstTopic();
        cy.visit('/user/profile');
        cy.wait('@topics');
        cy.get('.topics-list .topic-card').should('have.length', 1);
        cy.get('.topics-list .topic-card .topic-title').should('exist');

        // Go to articles and vérifier les articles
        cy.visit('/articles');
        cy.get('.list-grid').should('exist');
        cy.wait('@articles').its('response.statusCode').should('eq', 200);
        cy.get('.list-grid .card').should('have.length.greaterThan', 0);

        // Vérifie que chaque article affiché possède un topic (dans la liste)
        cy.get('.list-grid .card').each(($el) => {
            cy.wrap($el).find('.card-title').should('exist').invoke('text').should('not.be.empty');
        });

        // Post a comment et vérifie le topic sur la page de détail
        cy.get('.list-grid .card').first().click();
        cy.get('.article-topic').should('exist').invoke('text').should('not.be.empty');
        cy.get('textarea#content').type('Cypress integration test comment');
        cy.contains('button[type="submit"]', 'Sauvegarder').click();
        cy.wait('@createComment').its('response.statusCode').should('be.oneOf', [200, 201]);
        cy.contains('.comment-content', 'Cypress integration test comment').should('exist');

        // Logout
        cy.logout();
    });
});
