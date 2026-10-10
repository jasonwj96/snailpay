import { Injectable, computed, signal } from '@angular/core';
import { WizardStep } from '../models/wizard.models';

@Injectable()
export class WizardService {
  private readonly _steps = signal<readonly WizardStep[]>([]);
  private readonly _index = signal(0);

  readonly steps = this._steps.asReadonly();
  readonly stepIndex = this._index.asReadonly();
  readonly step = computed(() => this._steps()[this._index()]);

  init(steps: readonly WizardStep[], initialId?: string): void {
    this._steps.set(steps);
    const index = initialId ? steps.findIndex(s => s.id === initialId) : 0;
    this._index.set(Math.max(index, 0));
  }

  next(): void {
    if (this._index() < this._steps().length - 1) {
      this._index.update(i => i + 1);
    }
  }

  goTo(id: string): void {
    const index = this._steps().findIndex(s => s.id === id);
    if (index >= 0) {
      this._index.set(index);
    }
  }
}