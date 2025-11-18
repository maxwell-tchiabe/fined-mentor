import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, map, catchError, of } from 'rxjs';
import { ChatSessionService } from '../services/chat-session.service';

@Injectable({
  providedIn: 'root'
})
export class SessionGuard implements CanActivate {
  
  constructor(
    private chatSessionService: ChatSessionService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const sessionId = route.params['sessionId'];
    
    if (!sessionId) {
      this.router.navigate(['/']);
      return of(false);
    }

    return this.chatSessionService.getSession(sessionId).pipe(
      map(session => {
        if (session && session.active) {
          this.chatSessionService.setActiveSession(session);
          return true;
        } else {
          this.router.navigate(['/']);
          return false;
        }
      }),
      catchError(() => {
        this.router.navigate(['/']);
        return of(false);
      })
    );
  }
}