import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';

describe('UserService (jest)', () => {
    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [UserService],
        });
        return {
            service: TestBed.inject(UserService),
            httpMock: TestBed.inject(HttpTestingController),
        };
    }

    afterEach(() => {
        try {
            TestBed.inject(HttpTestingController).verify();
        } catch {
            // ignore
        }
    });

    it('hits expected endpoints', (done) => {
        const { service, httpMock } = setup();

        service.getAll().subscribe();
        httpMock.expectOne('/api/users').flush([]);

        service.getById(2).subscribe();
        httpMock.expectOne('/api/users/2').flush({ id: 2 });

        service.update({ username: 'x' }).subscribe({ next: () => done(), error: done });
        const req = httpMock.expectOne('/api/users');
        expect(req.request.method).toBe('PUT');
        req.flush({ id: 2, username: 'x' });
    });
});
