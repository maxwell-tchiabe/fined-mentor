import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InputOtpModule } from 'primeng/inputotp';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, InputOtpModule, FooterComponent, TranslateModule],
  templateUrl: './activate.component.html',
  styleUrl: './activate.component.css'
})
export class ActivateComponent {
  public activateForm: FormGroup;
  public resendForm: FormGroup;
  public loading: boolean = false;
  public error: string = '';
  public successMessage: string = '';

  public showResend: boolean = false;
  public resendLoading: boolean = false;
  public resendMessage: string = '';
  public resendSuccess: boolean = false;

  private readonly REDIRECT_DELAY_MS: number = 2000;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.activateForm = this.formBuilder.group({
      token: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.resendForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  public onSubmit(): void {
    if (this.activateForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.activate(this.activateForm.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.successMessage = 'Account activated successfully. Redirecting to login...';
            setTimeout(() => {
              this.router.navigate(['/auth/login']);
            }, this.REDIRECT_DELAY_MS);
          } else {
            this.error = response.message || 'Activation failed';
          }
        },
        error: (err) => {
          this.error = err.error?.message || 'An error occurred during activation';
        }
      });
  }

  public onResend(): void {
    if (this.resendForm.invalid) return;

    this.resendLoading = true;
    this.resendMessage = '';

    const email = this.resendForm.get('email')?.value;
    this.authService.resendActivation(email)
      .pipe(finalize(() => this.resendLoading = false))
      .subscribe({
        next: (response) => {
          this.resendSuccess = true;
          this.resendMessage = 'Activation code resent. Please check your email.';
        },
        error: (err) => {
          this.resendSuccess = false;
          this.resendMessage = err.error?.message || 'Failed to resend activation code.';
        }
      });
  }
}
