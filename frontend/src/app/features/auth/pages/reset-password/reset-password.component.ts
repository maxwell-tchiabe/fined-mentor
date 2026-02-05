import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { InputOtpModule } from 'primeng/inputotp';
import { AuthService } from '../../../../core/services/auth.service';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputOtpModule, FormsModule, FooterComponent],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  public resetForm: FormGroup;
  public loading: boolean = false;
  public submitted: boolean = false;
  public successMessage: string = '';
  public errorMessage: string = '';
  private token: string = '';

  private readonly REDIRECT_DELAY_MS: number = 3000;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.resetForm = this.formBuilder.group({
      token: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  public ngOnInit(): void {
    const tokenFromUrl = this.route.snapshot.queryParams['token'];
    if (tokenFromUrl) {
      this.resetForm.patchValue({ token: tokenFromUrl });
    }
  }

  public get formControls() {
    return this.resetForm.controls;
  }

  public passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  public onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    if (this.resetForm.invalid) {
      return;
    }

    this.loading = true;
    const token = this.formControls['token'].value;
    const password = this.formControls['password'].value;

    this.authService.resetPassword(token, password)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Password reset successfully.';
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, this.REDIRECT_DELAY_MS);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Something went wrong. Please try again.';
        }
      });
  }
}
