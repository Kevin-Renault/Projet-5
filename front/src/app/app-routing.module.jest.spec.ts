describe('routes (jest)', () => {
    it('does not include dev route when enableDevRoutes=false', async () => {
        const mod = await import('./app-routing.module');
        const paths = mod.routes.map(r => r.path);
        expect(paths).not.toContain('test');
        expect(paths).toContain('articles');
        expect(paths).toContain('topics');
        expect(paths).toContain('user/login');
    });

    it('includes dev route when enableDevRoutes=true (module mocked)', async () => {
        jest.resetModules();
        jest.doMock('../environments/environment', () => ({
            environment: { production: false, useMock: true, enableDevRoutes: true },
        }));

        const mod = await import('./app-routing.module');
        const paths = mod.routes.map(r => r.path);
        expect(paths).toContain('test');
    });

    it('loadComponent factories resolve to component classes', async () => {
        const mod = await import('./app-routing.module');
        const loadables = mod.routes.filter(r => typeof r.loadComponent === 'function');

        // Execute the functions so they count toward function coverage.
        for (const r of loadables) {
            const component = await r.loadComponent!();
            expect(component).toBeTruthy();
        }
    });
});
