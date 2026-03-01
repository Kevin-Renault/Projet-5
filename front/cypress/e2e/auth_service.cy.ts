/// <reference types="cypress" />

// Tests e2e ciblés pour augmenter la couverture de AuthService (src/app/core/auth/auth.service.ts)
// Objectif: exécuter les branches initSession (success, fallback refresh, failure), login error, logout error.

describe('AuthService (coverage via e2e)', () => {
    const user = { id: 1, username: 'it_user', email: 'it_user@example.com', password: '' };

    const seedApiForArticlesPage = () => {
        // ArticleListComponent charge /api/articles et /api/users
        cy.intercept('GET', '/api/articles', {
            statusCode: 200,
            body: [
                {
                    id: 1,
                    title: 'Article',
                    content: '...',
                    createdAt: new Date().toISOString(),
                    authorId: 1,
                    topicId: 1,
                },
            ],
        }).as('articles');

        cy.intercept('GET', '/api/users', {
            statusCode: 200,
            body: [{ id: 1, username: 'it_user', email: 'it_user@example.com', password: '' }],
        }).as('users');
    };

    const stubCsrfOk = () => {
        cy.intercept('GET', '/api/auth/csrf', {
            statusCode: 204,
            headers: {
                'X-XSRF-TOKEN': 'it-xsrf-token',
            },
            body: '',
        }).as('csrf');
    };

    it('initSession success: /me ok -> navigates to /articles', () => {
        stubCsrfOk();
        seedApiForArticlesPage();

        cy.intercept('GET', '/api/auth/me', {
            statusCode: 200,
            body: user,
        }).as('me');

        // Should not be called on the happy path.
        cy.intercept('POST', '/api/auth/refresh', {
            statusCode: 200,
            body: {},
        }).as('refresh');

        cy.visit('/');

        cy.wait('@csrf');
        cy.wait('@me');

        cy.location('pathname', { timeout: 20000 }).should('eq', '/articles');

        // Let the articles page settle (avoid flakiness).
        cy.wait('@articles');
        cy.wait('@users');

        // Ensure refresh was not needed.
        cy.get('@refresh.all').then((calls) => {
            expect(calls, 'refresh calls').to.have.length(0);
        });
    });

    it('initSession fallback: /me 401 -> /refresh ok -> /me ok', () => {
        stubCsrfOk();
        seedApiForArticlesPage();

        let meCalls = 0;
        cy.intercept('GET', '/api/auth/me', (req) => {
            meCalls += 1;
            if (meCalls === 1) {
                req.reply({ statusCode: 401, body: { message: 'unauthorized' } });
                return;
            }
            req.reply({ statusCode: 200, body: user });
        }).as('me');

        cy.intercept('POST', '/api/auth/refresh', {
            statusCode: 200,
            body: {},
        }).as('refresh');

        // Trigger AuthGuard.initSession by hitting a protected route.
        cy.visit('/articles');

        cy.wait('@csrf');
        cy.wait('@me');
        cy.wait('@refresh');
        cy.wait('@me');

        cy.location('pathname', { timeout: 20000 }).should('eq', '/articles');
        cy.wait('@articles');
        cy.wait('@users');
    });

    it('initSession failure: /me 401 & /refresh 401 -> redirects to / and shows error on home', () => {
        stubCsrfOk();

        cy.intercept('GET', '/api/auth/me', {
            statusCode: 401,
            body: { message: 'unauthorized' },
        }).as('me');

        cy.intercept('POST', '/api/auth/refresh', {
            statusCode: 401,
            body: { message: 'no session' },
        }).as('refresh');

        // Visiting protected route triggers guard -> redirect '/'
        // Then HomeComponent runs initSession and will also fail, displaying ErrorComponent.
        cy.visit('/articles');

        cy.location('pathname', { timeout: 20000 }).should('eq', '/');

        cy.contains('h1', 'Une erreur est survenue', { timeout: 20000 }).should('exist');
        cy.contains('Action tentée', { timeout: 20000 }).should('exist');
        cy.contains('Restauration de la session', { timeout: 20000 }).should('exist');
        cy.contains('Initialisation automatique de la session échouée', { timeout: 20000 }).should('exist');
    });

    it('login error: shows auth error message on /user/login', () => {
        stubCsrfOk();

        cy.intercept('POST', '/api/auth/login', {
            statusCode: 401,
            body: { message: 'invalid credentials' },
        }).as('login');

        cy.visit('/user/login');
        cy.get('#login').type('wrong@example.com');
        cy.get('#password').type('Bad-password1!');
        cy.get('button[type="submit"]').click();

        cy.wait('@csrf');
        cy.wait('@login');

        cy.location('pathname').should('eq', '/user/login');
        cy.contains('Authentication failed: invalid credentials').should('exist');
    });

    it('logout error: still clears session and navigates to /', () => {
        stubCsrfOk();
        seedApiForArticlesPage();

        cy.intercept('POST', '/api/auth/login', {
            statusCode: 200,
            body: { user },
        }).as('login');

        cy.intercept('POST', '/api/auth/logout', {
            statusCode: 500,
            body: { message: 'server error' },
        }).as('logout');

        // Login through the UI to ensure session is set by AuthService.login.
        cy.visit('/user/login');
        cy.get('#login').type(user.email);
        cy.get('#password').type('Whatever-password1!');
        cy.get('button[type="submit"]').click();

        cy.wait('@csrf');
        cy.wait('@login');

        cy.location('pathname', { timeout: 20000 }).should('eq', '/articles');
        cy.wait('@articles');
        cy.wait('@users');

        // Click logout (HeaderComponent -> AuthService.logout)
        cy.contains('button', 'Se déconnecter').click();

        cy.wait('@csrf');
        cy.wait('@logout');

        cy.location('pathname', { timeout: 20000 }).should('eq', '/');
        cy.contains('button', 'Se connecter').should('exist');
    });
});
