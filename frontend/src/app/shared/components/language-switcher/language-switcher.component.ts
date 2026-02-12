import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="flex bg-base-300/40 rounded-full p-0.5 gap-0.5 border border-base-300/50">
      <button *ngFor="let lang of languages" 
              (click)="switchLanguage(lang.code, $event)"
              [class]="currentLang === lang.code ? 
                       'bg-brand-500 text-white shadow-sm' : 
                       'text-content-300 hover:text-content-100 hover:bg-base-300/50'"
              class="w-8 h-7 text-[10px] font-bold rounded-full transition-all uppercase flex items-center justify-center focus:outline-none">
        {{ lang.code }}
      </button>
    </div>
  `
})
export class LanguageSwitcherComponent implements OnInit {
  currentLang: string = 'en';
  languages = [
    { code: 'en', label: 'English' },
    { code: 'fr', label: 'Français' },
    { code: 'de', label: 'Deutsch' }
  ];

  constructor(private translate: TranslateService) { }

  ngOnInit() {
    this.currentLang = this.translate.currentLang || this.translate.getDefaultLang() || 'en';
  }

  switchLanguage(lang: string, event: Event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.translate.use(lang);
    this.currentLang = lang;
  }
}
