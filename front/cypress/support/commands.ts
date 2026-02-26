export const USER_PREFIX = 'it_to_erase_';
/// <reference types="cypress" />

Cypress.Commands.add('register', (username: string, email: string, password: string) => {
    cy.visit('/user/register');
    cy.get('#username').type(username);
    cy.get('#email').type(email);
    cy.get('#password').type(password);
    cy.get('button[type="submit"]').click();
});

Cypress.Commands.add('login', (email: string, password: string) => {
    cy.visit('/user/login');
    cy.get('#login').type(email);
    cy.get('#password').type(password);
    cy.get('button[type="submit"]').click();
});

Cypress.Commands.add('subscribeToFirstTopic', () => {
    cy.visit('/topics');
    cy.get('.topics-list .topic-card', { timeout: 10000 }).should('exist');
    cy.get('.topics-list .topic-card').first().within(() => {
        cy.contains("s'abonner").click({ force: true });
        cy.contains('se désabonner', { timeout: 5000 }).should('exist');
    });
});

Cypress.Commands.add('postCommentOnFirstArticle', (comment: string) => {
    cy.visit('/articles');
    cy.get('.list-grid .card').first().click();
    cy.get('textarea#content').type(comment);
    cy.contains('button[type="submit"]', 'Sauvegarder').click();
    cy.contains('.comment-content', comment).should('exist');
});

Cypress.Commands.add('logout', () => {
    cy.contains('button', 'Se déconnecter').click();
    cy.location('pathname', { timeout: 10000 }).should('be.oneOf', ['/', '/user/login']);
    cy.wait(300); // Laisse le temps à la redirection de s'effectuer
    cy.visit('/user/profile');
    cy.location('pathname', { timeout: 10000 }).should('be.oneOf', ['/', '/user/login']);
});