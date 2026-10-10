import { Type } from '@angular/core';

export interface WizardStep {
  id: string;
  component: Type<unknown>;
}