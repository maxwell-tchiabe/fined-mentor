import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';
import { ApiResponse } from '../models/chat.model';
import { LoginRequest, RegisterRequest, ActivateRequest, JwtResponse } from '../models/auth.model';
import { LoggerService } from './logger.service';


@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly apiUrl = `${environment.apiUrl}/auth`;

    private currentUserSubject = new BehaviorSubject<JwtResponse | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(
        private http: HttpClient,
        private router: Router,
        private logger: LoggerService
    ) {

    }



    register(request: RegisterRequest): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/register`, request);
    }

    login(request: LoginRequest): Observable<ApiResponse<JwtResponse>> {
        return this.http.post<ApiResponse<JwtResponse>>(`${this.apiUrl}/login`, request).pipe(
            tap(response => {
                if (response.success && response.data) {
                    this.handleLoginSuccess(response.data);
                }
            })
        );
    }

    activate(request: ActivateRequest): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/activate`, request);
    }

    resendActivation(email: string): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/resend-activation?email=${email}`, {});
    }

    forgotPassword(email: string): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/forgot-password`, { email });
    }

    resetPassword(token: string, newPassword: string): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/reset-password`, { token, newPassword });
    }

    logout(): void {
        this.http.post(`${this.apiUrl}/logout`, {}).subscribe({

        });
    }



    private handleLoginSuccess(data: JwtResponse): void {
        // Store minimal user info (token is now in HttpOnly cookie)
        const user: JwtResponse = {
            token: '', // No longer stored here
            type: data.type,
            id: data.id,
            username: data.username,
            email: data.email,
            roles: data.roles
        };
        this.currentUserSubject.next(user);
    }

    getToken(): string | null {
        return null; // Token is handled by the browser via cookies
    }

    isAuthenticated(): boolean {
        return !!this.currentUserSubject.value;
    }
}
