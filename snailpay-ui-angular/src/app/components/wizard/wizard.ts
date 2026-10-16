import { Component, computed, input, linkedSignal, output } from '@angular/core';
import { NgComponentOutlet } from '@angular/common';
import { WizardStep } from '../../models/Wizard.models';

@Component({
  selector: 'app-wizard',
  imports: [NgComponentOutlet],
  templateUrl: './wizard.html',
})
export class WizardComponent {
  readonly steps = input.required<readonly WizardStep[]>();
  readonly initialStepId = input<string>();
  readonly completed = output<void>();

  private readonly _currentStepIndex = linkedSignal(() => {
    const startId = this.initialStepId();
    const startIndex = startId ? this.steps().findIndex(step => step.id === startId) : 0;
    return Math.max(startIndex, 0);
  });

  readonly currentStepIndex = this._currentStepIndex.asReadonly();
  readonly currentStep = computed(() => this.steps()[this._currentStepIndex()]);

  goToNextStep(): void {
    const isLastStep = this._currentStepIndex() >= this.steps().length - 1;
    if (isLastStep) {
      this.completed.emit();
    } else {
      this._currentStepIndex.update(index => index + 1);
    }
  }
}