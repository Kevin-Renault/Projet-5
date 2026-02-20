// Test d'intégration complet : inscription, login, navigation, commentaire, logout

describe('Full integration flow', () => {
    const unique = () => `${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
    const username = `it_${unique()}`;
    const email = `it_${unique()}@example.com`;
    const password = 'TestP@ssw0rd1';

    it('register, login, comment, logout', () => {
        cy.intercept('GET', '/api/auth/csrf').as('csrf');
        cy.intercept('POST', '/api/auth/register').as('register');
        cy.intercept('POST', '/api/auth/login').as('login');
        cy.intercept('GET', '/api/topics').as('topics');
        cy.intercept('GET', '/api/articles').as('articles');
        cy.intercept('POST', '/api/comments').as('createComment');

        // Register
        cy.visit('/user/register');
        cy.get('#username').type(username);
        cy.get('#email').type(email);
        cy.get('#password').type(password);
        cy.get('button[type="submit"]').click();
        cy.wait('@csrf').its('response.statusCode').should('eq', 204);
        cy.wait('@register').then((i) => {
            const payload = {
                request: {
                    url: i.request.url,
                    method: i.request.method,
                    headers: i.request.headers,
                    body: i.request.body,
                },
                response: {
                    statusCode: i.response?.statusCode,
                    headers: i.response?.headers,
                    body: i.response?.body,
                }
            };

            cy.writeFile('cypress/debug/register.json', payload).then(() => {
                expect(i.response?.statusCode).to.eq(200);
            });
        });
        cy.url({ timeout: 20000 }).should('include', '/articles');

        // Logout
        cy.contains('button', 'Se déconnecter').click();
        cy.location('pathname').should('be.oneOf', ['/', '/user/login']);

        // Login
        cy.visit('/user/login');
        cy.get('#login').type(email);
        cy.get('#password').type(password);
        cy.get('button[type="submit"]').click();
        cy.wait('@csrf');
        cy.wait('@login').then((i) => {
            const payload = {
                request: {
                    url: i.request.url,
                    method: i.request.method,
                    headers: i.request.headers,
                    body: i.request.body,
                },
                response: {
                    statusCode: i.response?.statusCode,
                    headers: i.response?.headers,
                    body: i.response?.body,
                }
            };

            cy.writeFile('cypress/debug/login.json', payload).then(() => {
                expect(i.response?.statusCode).to.eq(200);
            });
        });
        cy.url({ timeout: 20000 }).should('include', '/articles');

        // Go to topics
        cy.visit('/topics');
        cy.wait('@topics').its('response.statusCode').should('eq', 200);
        cy.get('.topics-list', { timeout: 20000 }).should('exist');

        // Go to articles
        cy.visit('/articles');
        cy.get('.list-grid').should('exist');
        cy.wait('@articles').then((i) => {
            const payload = {
                request: {
                    url: i.request.url,
                    method: i.request.method,
                    headers: i.request.headers,
                },
                response: {
                    statusCode: i.response?.statusCode,
                    headers: i.response?.headers,
                }
            };

            cy.writeFile('cypress/debug/articles.json', payload).then(() => {
                expect(i.response?.statusCode).to.eq(200);
            });
        });

        // Pick first article
        cy.get('.list-grid .card').first().click();
        cy.url().should('match', /\/articles\/\d+\/comment/);

        // Post a comment
        cy.getCookies().then((cookies) => {
            cy.writeFile('cypress/debug/cookies_before_comment.json', cookies).then(() => {
                expect(cookies.some((c) => c.name === 'access_token')).to.eq(true);
            });
        });
        cy.get('textarea#content').type('Cypress integration test comment');
        cy.contains('button[type="submit"]', 'Sauvegarder').click();
        cy.wait('@createComment').then((i) => {
            const payload = {
                request: {
                    url: i.request.url,
                    method: i.request.method,
                    headers: i.request.headers,
                    body: i.request.body,
                },
                response: {
                    statusCode: i.response?.statusCode,
                    headers: i.response?.headers,
                    body: i.response?.body,
                }
            };

            cy.writeFile('cypress/debug/createComment.json', payload).then(() => {
                expect(i.response?.statusCode).to.be.oneOf([200, 201]);
            });
        });
        cy.contains('.comment-content', 'Cypress integration test comment').should('exist');

        // Logout
        cy.contains('button', 'Se déconnecter').click();
        cy.location('pathname').should('be.oneOf', ['/', '/user/login']);
    });
});
