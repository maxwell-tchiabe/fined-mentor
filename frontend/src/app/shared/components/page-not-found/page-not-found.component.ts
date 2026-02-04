import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-page-not-found',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './page-not-found.component.html'
})
export class PageNotFoundComponent { }
