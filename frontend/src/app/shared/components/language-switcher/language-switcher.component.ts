import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="relative">
      <button (click)="toggleDropdown($event)"
              class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-white/10 transition-colors focus:outline-none focus:ring-2 focus:ring-alien-accent"
              aria-label="Select Language">
        <i class="pi pi-globe text-xl text-alien-primary"></i>
        <span class="uppercase font-medium text-sm text-alien-primary">{{ currentLang }}</span>
        <i class="pi pi-chevron-down text-xs text-gray-400"></i>
      </button>

      <div *ngIf="isOpen"
           class="absolute right-0 mt-2 w-48 bg-alien-bg border border-white/10 rounded-xl shadow-xl overflow-hidden z-50">
        <button *ngFor="let lang of languages"
                (click)="switchLanguage(lang.code)"
                class="w-full text-left px-4 py-3 text-sm text-gray-300 hover:bg-white/10 hover:text-white transition-colors flex items-center justify-between">
          <span>{{ lang.label }}</span>
          <i *ngIf="currentLang === lang.code" class="pi pi-check text-green-400"></i>
        </button>
      </div>
    </div>
  `
})
export class LanguageSwitcherComponent {
  currentLang: string = 'en';
  isOpen: boolean = false;
  languages = [
    { code: 'en', label: 'English' },
    { code: 'fr', label: 'Français' },
    { code: 'de', label: 'Deutsch' }
  ];

  constructor(private translate: TranslateService) {
    this.currentLang = this.translate.currentLang || this.translate.getDefaultLang() || 'en';
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.isOpen = !this.isOpen;
  }

  switchLanguage(lang: string) {
    this.translate.use(lang);
    this.currentLang = lang;
    this.isOpen = false;
  }
}
