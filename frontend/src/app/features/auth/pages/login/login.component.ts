import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ChatSessionService } from '../../../../core/services/chat-session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private chatSessionService: ChatSessionService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      usernameOrEmail: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.error = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          // After successful login, load sessions so navbar and pages update
          this.chatSessionService.loadSessions().subscribe({
            next: () => {
              this.router.navigate(['/chat']);
            },
            error: (err) => {
              console.error('Failed to load sessions after login:', err);
              this.router.navigate(['/chat']);
            }
          });
        } else {
          this.error = response.message || 'Login failed';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'An error occurred during login';
        this.loading = false;
      }
    });
  }
}
