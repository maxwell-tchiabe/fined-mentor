import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex items-center justify-center p-4" [class]="sizeClass">
      <div class="animate-spin rounded-full border-4 border-dashed" 
           [class]="spinnerClasses"></div>
      <span *ngIf="showText" class="ml-2 text-content-200">Loading...</span>
    </div>
  `
})
export class LoadingSpinnerComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() showText = false;

  get sizeClass(): string {
    return {
      'sm': 'h-8',
      'md': 'h-16',
      'lg': 'h-24'
    }[this.size];
  }

  get spinnerClasses(): string {
    const base = 'border-brand-500';
    const size = {
      'sm': 'h-6 w-6 border-2',
      'md': 'h-8 w-8 border-3',
      'lg': 'h-12 w-12 border-4'
    }[this.size];
    return `${base} ${size}`;
  }
}