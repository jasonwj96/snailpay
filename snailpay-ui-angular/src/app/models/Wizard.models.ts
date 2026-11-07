import { Type } from '@angular/core';

export interface WizardStep {
  id: string;
  label: string;
  component: Type<unknown>;
}