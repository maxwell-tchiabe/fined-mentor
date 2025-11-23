import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './activate.component.html'
})
export class ActivateComponent {
  activateForm: FormGroup;
  loading = false;
  error = '';
  successMessage = '';

  showResend = false;
  resendEmail = '';
  resendLoading = false;
  resendMessage = '';
  resendSuccess = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.activateForm = this.fb.group({
      token: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.activateForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.activate(this.activateForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Account activated successfully. Redirecting to login...';
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        } else {
          this.error = response.message || 'Activation failed';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'An error occurred during activation';
        this.loading = false;
      }
    });
  }

  onResend(): void {
    if (!this.resendEmail) return;

    this.resendLoading = true;
    this.resendMessage = '';

    this.authService.resendActivation(this.resendEmail).subscribe({
      next: (response) => {
        this.resendSuccess = true;
        this.resendMessage = 'Activation code resent. Please check your email.';
        this.resendLoading = false;
      },
      error: (err) => {
        this.resendSuccess = false;
        this.resendMessage = err.error?.message || 'Failed to resend activation code.';
        this.resendLoading = false;
      }
    });
  }
}
