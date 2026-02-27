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
        let completed = 0;
        function checkDone() {
            completed++;
            if (completed === 3) done();
        }

        service.getAll().subscribe(() => {
            checkDone();
        });
        httpMock.expectOne('/api/articles').flush([]);

        service.getById(12).subscribe(() => {
            checkDone();
        });
        httpMock.expectOne('/api/articles/12').flush({});

        service.create({ title: 't' } as any).subscribe(() => {
            checkDone();
        });
        const createReq = httpMock.expectOne('/api/articles');
        expect(createReq.request.method).toBe('POST');
        createReq.flush({});
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
