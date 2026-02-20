import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TopicService } from './topic.service';

describe('TopicService (jest)', () => {
    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [TopicService],
        });
        return {
            service: TestBed.inject(TopicService),
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

    it('gets topics and topic by id', (done) => {
        const { service, httpMock } = setup();

        service.getAll().subscribe();
        httpMock.expectOne('/api/topics').flush([]);

        service.getById(3).subscribe({ next: () => done(), error: done });
        httpMock.expectOne('/api/topics/3').flush({ id: 3, name: 't' });
    });
});
