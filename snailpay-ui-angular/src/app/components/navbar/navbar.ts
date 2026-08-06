import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgOptimizedImage } from '@angular/common';
import { OverlayModule, ConnectedPosition } from '@angular/cdk/overlay';

import navItemsData from './nav.json';
import { NavItem } from './nav.model';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, NgOptimizedImage, OverlayModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent {
  // Angular 19+ components are standalone by default — no `standalone: true` needed.

  protected readonly navItems: NavItem[] = navItemsData;

  // Equivalent to `useState<string | null>(null)`
  protected readonly activeItem = signal<string | null>(null);

  // Equivalent to the `pos` state + manual getBoundingClientRect in your React Dropdown:
  // cdkConnectedOverlay computes this itself against the origin, so no manual calc needed.
  protected readonly dropdownPositions: ConnectedPosition[] = [
    {
      originX: 'center',
      originY: 'bottom',
      overlayX: 'center',
      overlayY: 'top',
    },
  ];

  // Grace period so moving the mouse from the trigger to the dropdown
  // panel doesn't close the menu mid-transit (they're different DOM
  // subtrees since the dropdown renders into the CDK overlay container).
  private closeTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private readonly closeDelayMs = 200;

  protected onItemHover(label: string): void {
    this.cancelClose();
    this.activeItem.set(label);
  }

  protected onDropdownHover(): void {
    this.cancelClose();
  }

  protected onRegionLeave(): void {
    this.scheduleClose();
  }

  private scheduleClose(): void {
    this.cancelClose();
    this.closeTimeoutId = setTimeout(() => this.activeItem.set(null), this.closeDelayMs);
  }

  private cancelClose(): void {
    if (this.closeTimeoutId !== null) {
      clearTimeout(this.closeTimeoutId);
      this.closeTimeoutId = null;
    }
  }
}