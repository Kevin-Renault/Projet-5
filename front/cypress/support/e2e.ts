// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

beforeEach(() => {
    // Stabilise les layouts responsives en CI/headless.
    cy.viewport(1920, 1080)
})

Cypress.on('uncaught:exception', (err) => {
    const msg = (err && (err as any).message) ? String((err as any).message) : ''

    // Erreurs fréquentes et non bloquantes sur des UI Material/ResizeObserver
    if (
        msg.includes('ResizeObserver loop limit exceeded') ||
        msg.includes('ResizeObserver loop completed with undelivered notifications')
    ) {
        return false
    }

    // Laisse Cypress échouer sur les vraies erreurs applicatives.
    return true
})

afterEach(() => {
    cy.window({ log: false }).then((win) => {
        const coverage = (win as any).__coverage__
        if (coverage) {
            cy.task('saveCoverage', coverage, { log: false })
        }
    })
})