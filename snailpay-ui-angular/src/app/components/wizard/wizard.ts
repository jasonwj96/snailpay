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
  readonly heading = input<string>();
  readonly logoSrc = input<string>();
  readonly completed = output<void>();

  private readonly _index = linkedSignal(() => {
    const id = this.initialStepId();
    const index = id ? this.steps().findIndex(s => s.id === id) : 0;
    return Math.max(index, 0);
  });

  readonly stepIndex = this._index.asReadonly();
  readonly step = computed(() => this.steps()[this._index()]);

  next(): void {
    if (this._index() >= this.steps().length - 1) {
      this.completed.emit();
    } else {
      this._index.update(i => i + 1);
    }
  }

  goTo(id: string): void {
    const index = this.steps().findIndex(s => s.id === id);
    if (index >= 0) {
      this._index.set(index);
    }
  }
}