import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Registration successful. Please check your email for activation OTP.';
          setTimeout(() => {
            this.router.navigate(['/auth/activate']);
          }, 2000);
        } else {
          this.error = response.message || 'Registration failed';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'An error occurred during registration';
        this.loading = false;
      }
    });
  }
}
