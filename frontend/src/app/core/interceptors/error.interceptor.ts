import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from 'primeng/api';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private messageService: MessageService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An unexpected error occurred';

        if (error.status === 429) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Too Many Requests',
            detail: 'You have exceeded the rate limit. Please wait a moment before trying again.',
            life: 5000
          });
          errorMessage = 'Rate limit exceeded. Please try again later.';
        } else if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = `Error: ${error.error.message}`;
        } else {
          // Server-side error
          errorMessage = error.error?.message || error.message || errorMessage;
        }

        console.error('HTTP Error:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}