// Test d'intégration : souscription à des topics et interaction avec articles

describe('Topic subscription and article interaction', () => {
    const unique = () => Math.random().toString(36).substring(2, 10);
    const username = `it_${unique()}`;
    const email = `it_${unique()}@example.com`;
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
        cy.visit('/user/register');
        cy.get('#username').type(username);
        cy.get('#email').type(email);
        cy.get('#password').type(password);
        cy.get('button[type="submit"]').click();
        cy.wait('@csrf');
        cy.wait('@register').its('response.statusCode').should('eq', 200);
        cy.url({ timeout: 20000 }).should('include', '/articles');

        // Go to topics and subscribe to two
        cy.visit('/topics');
        cy.wait('@topics').its('response.statusCode').should('eq', 200);
        cy.get('.topic-card').should('have.length.greaterThan', 0);

        cy.get('.topic-card').its('length').then((len) => {
            const toSubscribe = Math.min(2, len);
            for (let i = 0; i < toSubscribe; i++) {
                // The page can re-render after each subscription, so always query fresh.
                cy.contains('button', "s'abonner").first().click();
                cy.wait('@subscribe').its('response.statusCode').should('be.oneOf', [200, 201]);
            }
        });

        // Verify subscriptions are visible in profile
        cy.visit('/user/profile');
        cy.wait('@subscriptions').its('response.statusCode').should('eq', 200);
        cy.get('.topics-list .topic-card').should('have.length.greaterThan', 0);

        // Go to articles and comment on up to 2
        cy.visit('/articles');
        cy.wait('@articles').its('response.statusCode').should('eq', 200);
        cy.get('.list-grid .card').should('have.length.greaterThan', 0);

        cy.get('.list-grid .card').its('length').then((len) => {
            const toComment = Math.min(2, len);

            const commentOnCard = (index) => {
                cy.get('.list-grid .card').eq(index).click();
                cy.get('textarea#content').type(`Cypress topic comment ${index}`);
                cy.contains('button[type="submit"]', 'Sauvegarder').click();
                cy.wait('@createComment').its('response.statusCode').should('be.oneOf', [200, 201]);
                cy.contains('.comment-content', `Cypress topic comment ${index}`).should('exist');
            };

            commentOnCard(0);

            if (toComment > 1) {
                cy.visit('/articles');
                cy.wait('@articles').its('response.statusCode').should('eq', 200);
                commentOnCard(1);
            }
        });

        // Logout
        cy.contains('button', 'Se déconnecter').click();
        cy.url().should('match', /\/\/?$/);
    });
});
