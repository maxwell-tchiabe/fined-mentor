import { Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from '../../../../shared/components/header/header.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
    selector: 'app-terms-of-service',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule, HeaderComponent, FooterComponent],
    templateUrl: './terms-of-service.component.html'
})
export class TermsOfServiceComponent {
    constructor(private location: Location) { }

    public goBack(): void {
        this.location.back();
    }
}
