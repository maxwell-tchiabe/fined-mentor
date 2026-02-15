import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, filter, map, take } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {

    constructor(private authService: AuthService, private router: Router) { }

    canActivate(): Observable<boolean | UrlTree> {
        return this.authService.isInitialized$.pipe(
            filter(initialized => initialized),
            take(1),
            map(() => {
                if (this.authService.isAuthenticated()) {
                    return true;
                }
                return this.router.createUrlTree(['/auth/login']);
            })
        );
    }
}
