import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface ApplicationListItem {
  id: number;
  candidate: {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
  };
  emailSubject: string;
  receivedAt: string;
  status: string;
  aiScore?: number;
}

@Component({
  selector: 'app-applications-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4 class="mb-0">{{ getTitle() }}</h4>
            </div>
            
            <div class="card-body">
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>Candidat</th>
                      <th>Email</th>
                      <th>Poste</th>
                      <th>Téléphone</th>
                      <th>Reçue le</th>
                      <th>Statut</th>
                      <th>Note IA</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let app of applications">
                      <td>{{ app.candidate.lastName }} {{ app.candidate.firstName }}</td>
                      <td>{{ app.candidate.email }}</td>
                      <td>{{ app.emailSubject }}</td>
                      <td>{{ app.candidate.phone || '-' }}</td>
                      <td>{{ formatDate(app.receivedAt) }}</td>
                      <td>
                        <span class="badge" [ngClass]="getStatusClass(app.status)">
                          {{ getStatusLabel(app.status) }}
                        </span>
                      </td>
                      <td>
                        <span *ngIf="app.aiScore" class="badge" [ngClass]="getScoreClass(app.aiScore)">
                          {{ app.aiScore }}/20
                        </span>
                        <span *ngIf="!app.aiScore" class="text-muted">-</span>
                      </td>
                      <td>
                        <button class="btn btn-outline-primary btn-sm" 
                                [routerLink]="['/applications', app.id]"
                                title="Consulter">
                          <i class="feather icon-eye"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <div *ngIf="applications.length === 0" class="text-center py-4">
                  <p class="text-muted">Aucune candidature trouvée</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .table th {
      border-top: none;
      font-weight: 600;
    }
    .badge {
      font-size: 0.75rem;
    }
  `]
})
export class ApplicationsListComponent implements OnInit {
  applications: ApplicationListItem[] = [];
  status: string | null = null;
  
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);

  ngOnInit(): void {
    // Détecter le statut depuis l'URL
    const url = this.route.snapshot.url.join('/');
    if (url.includes('validated')) {
      this.status = 'VALIDATED';
    } else if (url.includes('ambiguous')) {
      this.status = 'AMBIGUOUS';
    } else if (url.includes('rejected')) {
      this.status = 'REJECTED';
    } else if (url.includes('archived')) {
      this.status = 'ARCHIVED';
    } else {
      this.status = null;
    }
    
    this.loadApplications();
  }

  loadApplications(): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    let url = 'http://localhost:8080/api/applications';
    if (this.status === 'ARCHIVED') {
      url += '/archived';
    } else if (this.status) {
      url += `/status/${this.status}`;
    }
    
    this.http.get<ApplicationListItem[]>(url, { headers })
      .subscribe({
        next: (apps) => {
          this.applications = apps;
        },
        error: (err) => console.error('Erreur chargement candidatures', err)
      });
  }

  getTitle(): string {
    switch (this.status) {
      case 'VALIDATED': return 'Candidatures validées';
      case 'REJECTED': return 'Candidatures rejetées';
      case 'AMBIGUOUS': return 'Candidatures ambiguës';
      case 'PENDING': return 'Candidatures en attente';
      case 'ARCHIVED': return 'Candidatures archivées';
      default: return 'Toutes les candidatures';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'bg-warning';
      case 'VALIDATED': return 'bg-success';
      case 'AMBIGUOUS': return 'bg-info';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'VALIDATED': return 'Validé';
      case 'AMBIGUOUS': return 'À examiner';
      case 'REJECTED': return 'Rejeté';
      default: return status;
    }
  }

  getScoreClass(score: number): string {
    if (score >= 15) return 'bg-success';
    if (score >= 10) return 'bg-warning';
    return 'bg-danger';
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }
}