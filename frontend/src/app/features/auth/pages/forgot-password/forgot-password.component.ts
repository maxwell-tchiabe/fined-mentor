import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FooterComponent],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  public forgotForm: FormGroup;
  public loading: boolean = false;
  public submitted: boolean = false;
  public successMessage: string = '';
  public errorMessage: string = '';

  private readonly REDIRECT_DELAY_MS: number = 3000;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.forgotForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  public get formControls() {
    return this.forgotForm.controls;
  }

  public onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    if (this.forgotForm.invalid) {
      return;
    }

    this.loading = true;
    const email = this.formControls['email'].value;

    this.authService.forgotPassword(email)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Password reset link sent to your email.';
          setTimeout(() => {
            this.router.navigate(['/auth/reset-password']);
          }, this.REDIRECT_DELAY_MS);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Something went wrong. Please try again.';
        }
      });
  }
}
