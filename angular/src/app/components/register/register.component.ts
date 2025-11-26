import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface RegisterResponse {
  message: string;
  user: {
    id: number;
    username: string;
    email: string;
    role: string;
    emailVerified: boolean;
  };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  
  registerForm: FormGroup;
  loading = false;
  error = '';
  success = false;
  emailSent = false;

  constructor() {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.error = '';
      
      const userData = {
        ...this.registerForm.value,
        role: 'RECRUITER'
      };
      
      this.http.post<RegisterResponse>('http://localhost:8080/api/auth/register', userData)
        .subscribe({
          next: (response) => {
            this.success = true;
            this.emailSent = true;
            this.loading = false;
          },
          error: (err) => {
            this.error = err.error?.error || 'Erreur lors de l\'inscription';
            this.loading = false;
          }
        });
    }
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}