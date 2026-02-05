import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../../shared/components/header/header.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
    selector: 'app-impressum',
    standalone: true,
    imports: [CommonModule, RouterModule, HeaderComponent, FooterComponent],
    templateUrl: './impressum.component.html'
})
export class ImpressumComponent {
    currentDate = new Date('2026-01-26');
}
