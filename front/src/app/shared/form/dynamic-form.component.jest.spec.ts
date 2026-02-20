import { TestBed } from '@angular/core/testing';
import { DynamicFormComponent } from './dynamic-form.component';

describe('DynamicFormComponent (jest)', () => {
    it('builds controls from formElements and emits on valid submit', () => {
        TestBed.configureTestingModule({
            imports: [DynamicFormComponent],
        });

        const fixture = TestBed.createComponent(DynamicFormComponent);
        const component = fixture.componentInstance;
        component.formElements = [
            { type: 'text', name: 'username', required: true },
            { type: 'email', name: 'email', required: true, pattern: String.raw`^[^@\s]+@[^@\s]+\.[^@\s]+$` },
        ];

        const emitSpy = jest.spyOn(component.formSubmit, 'emit');
        component.ngOnChanges();

        component.form.get('username')!.setValue('bob');
        component.form.get('email')!.setValue('bob@example.com');

        component.onSubmit();
        expect(emitSpy).toHaveBeenCalledWith({ username: 'bob', email: 'bob@example.com' });
    });

    it('does not emit when invalid and returns error classes/messages', () => {
        TestBed.configureTestingModule({
            imports: [DynamicFormComponent],
        });

        const fixture = TestBed.createComponent(DynamicFormComponent);
        const component = fixture.componentInstance;
        component.formElements = [{ type: 'text', name: 'username', required: true }];
        component.ngOnChanges();

        const emitSpy = jest.spyOn(component.formSubmit, 'emit');
        component.onSubmit();
        expect(emitSpy).not.toHaveBeenCalled();

        const ctrl = component.form.get('username')!;
        ctrl.markAsTouched();
        expect(component.showError('username')).toBe(true);
        expect(component.getErrorClass('username')).toBe('input-error');
        expect(component.getErrorMessage('username')).toContain('username est obligatoire');

        ctrl.setValue('x');
        ctrl.markAsDirty();
        expect(component.getErrorClass('username')).toBe('input-valid');
        expect(component.getStatusClass()).toBe('success');
        component.error = true;
        expect(component.getStatusClass()).toBe('error');
    });
});
