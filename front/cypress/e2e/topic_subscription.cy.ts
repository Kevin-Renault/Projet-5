// Test d'intégration : souscription à des topics et interaction avec articles
import { USER_PREFIX } from '../support/commands';

describe('Topic subscription and article interaction', () => {
    const unique = () => Math.random().toString(36).substring(2, 10);
    const username = `${USER_PREFIX}${unique()}`;
    const email = `${USER_PREFIX}${unique()}@example.com`;
    const password = 'TestP@ssw0rd1';

    it('subscribe to topics, comment on articles', () => {
        cy.intercept('GET', '/api/auth/csrf').as('csrf');
        cy.intercept('POST', '/api/auth/register').as('register');
        cy.intercept('GET', '/api/topics').as('topics');
        cy.intercept('POST', '/api/subscriptions').as('subscribe');
        cy.intercept('GET', '/api/subscriptions').as('subscriptions');
        cy.intercept('GET', '/api/articles').as('articles');
        cy.intercept('POST', '/api/comments').as('createComment');

        // Register
        cy.register(username, email, password);
        cy.wait('@register').then((interception) => {
            // Debug log
            // eslint-disable-next-line no-console
            console.log('REGISTER INTERCEPTION', interception);
            expect(interception.response?.statusCode, 'register status').to.be.oneOf([200, 201]);
        });
        cy.url({ timeout: 20000 }).should('include', '/articles');
        // Vérifie présence d'un élément de navigation
        cy.get('nav').should('exist');

        // Go to topics and subscribe to the first
        cy.subscribeToFirstTopic();
        // Vérifie que le topic abonné apparaît dans le profil
        cy.visit('/user/profile');
        cy.wait('@subscriptions').its('response.statusCode').should('eq', 200);
        cy.get('.topics-list .topic-card').should('have.length', 1);
        cy.get('.topics-list .topic-card .topic-title').should('exist');

        // Go to articles and comment on the first
        cy.postCommentOnFirstArticle('Mon commentaire');
        // Vérifie le status de la création du commentaire
        cy.wait('@createComment').its('response.statusCode').should('be.oneOf', [200, 201]);
        // Vérifie que le commentaire est affiché
        cy.contains('.comment-content', 'Mon commentaire').should('exist');


        // Logout
        cy.logout();
    });
});
