import { Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../../core/services/auth.service';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FooterComponent, TranslateModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  public registerForm: FormGroup;
  public loading: boolean = false;
  public error: string = '';
  public successMessage: string = '';

  private readonly REDIRECT_DELAY_MS: number = 2000;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private location: Location
  ) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  public goBack(): void {
    this.location.back();
  }

  public onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.successMessage = 'Registration successful. Please check your email for activation OTP.';
            setTimeout(() => {
              this.router.navigate(['/auth/activate']);
            }, this.REDIRECT_DELAY_MS);
          } else {
            this.error = response.message || 'Registration failed';
          }
        },
        error: (err) => {
          this.error = err.error?.message || 'An error occurred during registration';
        }
      });
  }
}
