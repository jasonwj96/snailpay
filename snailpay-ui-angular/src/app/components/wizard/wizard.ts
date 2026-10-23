import { Component, computed, input, signal } from '@angular/core';
import { NgComponentOutlet } from '@angular/common';
import { WizardStep } from '../../models/Wizard.models';

@Component({
  selector: 'app-wizard',
  imports: [NgComponentOutlet],
  templateUrl: './wizard.html',
})
export class WizardComponent {
  readonly steps = input.required<readonly WizardStep[]>();
  readonly currentStepIndex = signal(1);
  readonly currentStepName = computed(() => this.steps()[this.currentStepIndex()]?.id ?? '');
  readonly currentStepComponent = computed(() => this.steps()[this.currentStepIndex()]);

  goToPreviousStep(): void {
    this.currentStepIndex.update((index) => Math.max(0, index - 1));
  }

  goToNextStep(): void {
    this.currentStepIndex.update((index) => Math.min(this.steps().length - 1, index + 1));
  }
}