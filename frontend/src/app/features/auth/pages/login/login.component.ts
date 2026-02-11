import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { ChatSessionService } from '../../../../core/services/chat-session.service';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FooterComponent, TranslateModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  public loginForm: FormGroup;
  public loading: boolean = false;
  public error: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private chatSessionService: ChatSessionService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      usernameOrEmail: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  public onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.error = '';

    this.authService.login(this.loginForm.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
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
        },
        error: (err) => {
          this.error = err.error?.message || 'An error occurred during login';
        }
      });
  }
}
