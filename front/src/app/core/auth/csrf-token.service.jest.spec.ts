import { CsrfTokenService } from './csrf-token.service';

describe('CsrfTokenService (jest)', () => {
    it('stores, returns and clears the token', () => {
        const service = new CsrfTokenService();

        expect(service.getToken()).toBeNull();

        service.setToken('abc');
        expect(service.getToken()).toBe('abc');

        service.clear();
        expect(service.getToken()).toBeNull();
    });
});
