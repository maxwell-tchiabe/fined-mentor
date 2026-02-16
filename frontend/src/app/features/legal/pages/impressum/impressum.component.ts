import { Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from '../../../../shared/components/header/header.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
    selector: 'app-impressum',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule, HeaderComponent, FooterComponent],
    templateUrl: './impressum.component.html'
})
export class ImpressumComponent {
    currentDate = new Date('2026-01-26');

    constructor(private location: Location) { }

    public goBack(): void {
        this.location.back();
    }
}
