import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ArticleService } from './article.service';

describe('ArticleService (jest)', () => {
    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ArticleService],
        });
        return {
            service: TestBed.inject(ArticleService),
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
        httpMock.expectOne('/api/articles').flush([]);

        service.getById(12).subscribe();
        httpMock.expectOne('/api/articles/12').flush({});

        service.create({ title: 't' } as any).subscribe();
        const createReq = httpMock.expectOne('/api/articles');
        expect(createReq.request.method).toBe('POST');
        createReq.flush({});

        service.update(12, { title: 'u' }).subscribe();
        const updateReq = httpMock.expectOne('/api/articles/12');
        expect(updateReq.request.method).toBe('PUT');
        updateReq.flush({});

        service.delete(12).subscribe({ next: () => done(), error: done });
        const deleteReq = httpMock.expectOne('/api/articles/12');
        expect(deleteReq.request.method).toBe('DELETE');
        deleteReq.flush(null);
    });

    it('sortByDateDesc/sortByDateAsc returns new sorted arrays', () => {
        const service = new ArticleService({} as any);
        const articles = [
            { createdAt: '2024-01-02T00:00:00.000Z' },
            { createdAt: '2024-01-01T00:00:00.000Z' },
        ] as any[];

        const desc = service.sortByDateDesc(articles as any);
        expect(desc[0].createdAt).toBe('2024-01-02T00:00:00.000Z');
        expect(desc).not.toBe(articles);

        const asc = service.sortByDateAsc(articles as any);
        expect(asc[0].createdAt).toBe('2024-01-01T00:00:00.000Z');
        expect(asc).not.toBe(articles);
    });
});
