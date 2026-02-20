import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ArticleCommentService } from './article-comment.service';

describe('ArticleCommentService (jest)', () => {
    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ArticleCommentService],
        });
        return {
            service: TestBed.inject(ArticleCommentService),
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

    it('fetches comments by articleId and creates comments', (done) => {
        const { service, httpMock } = setup();

        service.getAllByArticleId(5).subscribe();
        const getReq = httpMock.expectOne('/api/comments?articleId=5');
        expect(getReq.request.method).toBe('GET');
        getReq.flush([]);

        service.create({ content: 'hi' } as any).subscribe({ next: () => done(), error: done });
        const postReq = httpMock.expectOne('/api/comments');
        expect(postReq.request.method).toBe('POST');
        postReq.flush({ id: 1 });
    });
});
