import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';
import { ApiResponse } from '../models/chat.model';
import { LoginRequest, RegisterRequest, ActivateRequest, JwtResponse } from '../models/auth.model';


@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly apiUrl = `${environment.apiUrl}/auth`;
    private readonly TOKEN_KEY = 'jwtToken';
    private readonly USER_KEY = 'currentUser';

    private currentUserSubject = new BehaviorSubject<JwtResponse | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient, private router: Router) {
        this.loadUserFromStorage();
    }

    private loadUserFromStorage(): void {
        const storedUser = localStorage.getItem(this.USER_KEY);
        if (storedUser) {
            try {
                this.currentUserSubject.next(JSON.parse(storedUser));
            } catch (e) {
                console.error('Error parsing stored user', e);
                this.logout();
            }
        }
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
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
        this.router.navigate(['/auth/login']);
    }

    private handleLoginSuccess(data: JwtResponse): void {
        localStorage.setItem(this.TOKEN_KEY, data.token);
        // Store minimal user info
        const user: JwtResponse = {
            token: data.token,
            type: data.type,
            id: data.id,
            username: data.username,
            email: data.email,
            roles: data.roles
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUserSubject.next(user);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}
