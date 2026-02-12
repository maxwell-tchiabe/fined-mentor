import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from '../../../../shared/components/header/header.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
    selector: 'app-privacy-policy',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule, HeaderComponent, FooterComponent],
    templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicyComponent {
}
