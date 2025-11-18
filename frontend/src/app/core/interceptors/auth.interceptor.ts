import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Add API key or other auth headers here if needed
    const modifiedReq = req.clone({
      url: req.url,
      headers: req.headers.set('Content-Type', 'application/json')
    });
    
    return next.handle(modifiedReq);
  }
}