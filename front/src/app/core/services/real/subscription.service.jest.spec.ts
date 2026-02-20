import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TopicSubscriptionService } from './subscription.service';

describe('TopicSubscriptionService (jest)', () => {
    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [TopicSubscriptionService],
        });
        return {
            service: TestBed.inject(TopicSubscriptionService),
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

    it('subscribes/unsubscribes and fetches subscriptions', (done) => {
        const { service, httpMock } = setup();

        service.subscribeOnTopic(10).subscribe();
        const subReq = httpMock.expectOne('/api/subscriptions');
        expect(subReq.request.method).toBe('POST');
        expect(subReq.request.body).toEqual({ topicId: 10 });
        subReq.flush([]);

        service.unsubscribeFromTopic(10).subscribe();
        const unsubReq = httpMock.expectOne('/api/subscriptions?topicId=10');
        expect(unsubReq.request.method).toBe('DELETE');
        unsubReq.flush([]);

        service.getUserTopicSubscriptions().subscribe();
        const meReq = httpMock.expectOne('/api/subscriptions');
        expect(meReq.request.method).toBe('GET');
        meReq.flush([]);

        service.getTopicSubscribers(10).subscribe({ next: () => done(), error: done });
        const subscribersReq = httpMock.expectOne('/api/subscriptions?topicId=10');
        expect(subscribersReq.request.method).toBe('GET');
        subscribersReq.flush([]);
    });
});
