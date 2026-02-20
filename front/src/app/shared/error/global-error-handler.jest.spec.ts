import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { GlobalErrorHandler } from './global-error-handler';

describe('GlobalErrorHandler (jest)', () => {
    it('redirects on HTTP status codes', () => {
        const navigate = jest.fn();
        jest.spyOn(console, 'error').mockImplementation(() => {});

        TestBed.configureTestingModule({
            providers: [
                GlobalErrorHandler,
                { provide: Router, useValue: { navigate } },
            ],
        });

        const handler = TestBed.inject(GlobalErrorHandler);

        handler.handleError(new HttpErrorResponse({ status: 401 }));
        expect(navigate).toHaveBeenCalledWith(['/user/login']);

        handler.handleError(new HttpErrorResponse({ status: 404 }));
        expect(navigate).toHaveBeenCalledWith(['/not-found']);

        handler.handleError(new HttpErrorResponse({ status: 500 }));
        expect(navigate).toHaveBeenCalledWith(['/']);
    });

    it('handles non-HTTP errors without routing', () => {
        const navigate = jest.fn();
        jest.spyOn(console, 'error').mockImplementation(() => {});

        TestBed.configureTestingModule({
            providers: [
                GlobalErrorHandler,
                { provide: Router, useValue: { navigate } },
            ],
        });

        const handler = TestBed.inject(GlobalErrorHandler);
        handler.handleError(new Error('boom'));
        expect(navigate).not.toHaveBeenCalled();
    });
});
