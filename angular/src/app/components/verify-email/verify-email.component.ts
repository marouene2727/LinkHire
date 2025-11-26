import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="verify-container">
      <div class="verify-card">
        <div class="verify-header">
          <h2>Vérification Email</h2>
        </div>
        
        <div class="verify-content" *ngIf="!loading">
          <div *ngIf="success" class="success-message">
            <i class="success-icon">✓</i>
            <h3>Email vérifié avec succès!</h3>
            <p>Votre compte a été activé. Vous pouvez maintenant vous connecter.</p>
            <button (click)="goToLogin()" class="btn-primary">Se connecter</button>
          </div>
          
          <div *ngIf="error" class="error-message">
            <i class="error-icon">✗</i>
            <h3>Erreur de vérification</h3>
            <p>{{ error }}</p>
            <button (click)="goToLogin()" class="btn-secondary">Retour à la connexion</button>
          </div>
        </div>
        
        <div class="verify-content" *ngIf="loading">
          <div class="loading-message">
            <div class="spinner"></div>
            <p>Vérification en cours...</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .verify-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .verify-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      padding: 40px;
      max-width: 500px;
      width: 100%;
      text-align: center;
    }

    .verify-header h2 {
      color: #333;
      margin-bottom: 30px;
      font-weight: 600;
    }

    .success-message, .error-message, .loading-message {
      padding: 20px 0;
    }

    .success-icon, .error-icon {
      font-size: 48px;
      margin-bottom: 20px;
      display: block;
    }

    .success-icon {
      color: #28a745;
    }

    .error-icon {
      color: #dc3545;
    }

    .success-message h3 {
      color: #28a745;
      margin-bottom: 15px;
    }

    .error-message h3 {
      color: #dc3545;
      margin-bottom: 15px;
    }

    .success-message p, .error-message p {
      color: #666;
      margin-bottom: 25px;
      line-height: 1.5;
    }

    .btn-primary, .btn-secondary {
      padding: 12px 30px;
      border: none;
      border-radius: 6px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s ease;
      text-decoration: none;
      display: inline-block;
    }

    .btn-primary {
      background: #667eea;
      color: white;
    }

    .btn-primary:hover {
      background: #5a6fd8;
      transform: translateY(-2px);
    }

    .btn-secondary {
      background: #6c757d;
      color: white;
    }

    .btn-secondary:hover {
      background: #5a6268;
      transform: translateY(-2px);
    }

    .spinner {
      border: 3px solid #f3f3f3;
      border-top: 3px solid #667eea;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .loading-message p {
      color: #666;
      margin: 0;
    }
  `]
})
export class VerifyEmailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  
  loading = true;
  success = false;
  error = '';

  ngOnInit(): void {
    const token = this.route.snapshot.queryParams['token'];
    
    if (!token) {
      this.error = 'Token de vérification manquant.';
      this.loading = false;
      return;
    }

    this.verifyEmail(token);
  }

  private verifyEmail(token: string): void {
    this.http.get<{message?: string, error?: string}>(`http://localhost:8080/api/auth/verify-email?token=${token}`)
      .subscribe({
        next: (response) => {
          this.success = true;
          this.loading = false;
        },
        error: (err) => {
          this.error = err.error?.error || 'Erreur lors de la vérification.';
          this.loading = false;
        }
      });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}