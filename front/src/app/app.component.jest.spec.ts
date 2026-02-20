import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent (integration jest)', () => {
    it('creates the root component', () => {
        TestBed.configureTestingModule({
            imports: [AppComponent],
        });

        const fixture = TestBed.createComponent(AppComponent);
        expect(fixture.componentInstance).toBeTruthy();
    });
});
