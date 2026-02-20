/** @type {import('jest').Config} */
module.exports = {
    preset: 'jest-preset-angular',
    setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'],
    testMatch: ['<rootDir>/src/**/*.jest.spec.ts'],
    testPathIgnorePatterns: ['/node_modules/', '/dist/', '/cypress/'],
    moduleNameMapper: {
        '^src/(.*)$': '<rootDir>/src/$1',
    },
    transform: {
        '^.+\\.(ts|mjs|js|html)$': [
            'jest-preset-angular',
            {
                tsconfig: '<rootDir>/tsconfig.jest.json',
                stringifyContentPathRegex: '\\.html$',
            },
        ],
    },
    collectCoverageFrom: [
        '<rootDir>/src/**/*.ts',
        '!<rootDir>/src/**/*.spec.ts',
        '!<rootDir>/src/test.ts',
        '!<rootDir>/src/main.ts',
        '!<rootDir>/src/polyfills.ts',
        '!<rootDir>/src/environments/**',
    ],
    coverageDirectory: '<rootDir>/coverage',
    coverageReporters: ['html', 'text-summary', 'json-summary', 'lcov'],
};
