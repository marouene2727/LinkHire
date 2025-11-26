import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface JobOffer {
  id: number;
  title: string;
  company: string;
  location: string;
  contractType: string;
  salaryMin?: number;
  salaryMax?: number;
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  createdAt: string;
  applicationDeadline?: string;
  applicationsCount: number;
  applicationUrl?: string;
}

@Component({
  selector: 'app-job-offers',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center">
                <h5 class="mb-0 me-3">Offres d'emploi</h5>
                <button class="btn btn-outline-secondary btn-sm" (click)="loadJobOffers()" title="Actualiser">
                  <i class="feather icon-refresh-cw"></i>
                </button>
              </div>
              <button class="btn btn-primary" routerLink="/job-offers/create">
                <i class="feather icon-plus me-2"></i>Nouvelle offre
              </button>
            </div>
            
            <div class="card-body">
              <!-- Filtres -->
              <div class="row mb-3">
                <div class="col-md-3">
                  <select class="form-select" [(ngModel)]="selectedStatus" (change)="filterOffers()">
                    <option value="">Tous les statuts</option>
                    <option value="DRAFT">Brouillon</option>
                    <option value="PUBLISHED">Publié</option>
                    <option value="CLOSED">Fermé</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <input type="text" class="form-control" placeholder="Rechercher par titre ou entreprise..." 
                         [(ngModel)]="searchTerm" (input)="filterOffers()">
                </div>
              </div>
              
              <!-- Tableau -->
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>Titre</th>
                      <th>Entreprise</th>
                      <th>Localisation</th>
                      <th>Type</th>
                      <th>Salaire</th>
                      <th>Statut</th>
                      <th>Candidatures</th>
                      <th>Date limite</th>
                      <th>Lien</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let offer of filteredOffers">
                      <td>
                        <strong>{{ offer.title }}</strong>
                      </td>
                      <td>{{ offer.company }}</td>
                      <td>{{ offer.location }}</td>
                      <td>
                        <span class="badge bg-secondary">{{ getContractTypeLabel(offer.contractType) }}</span>
                      </td>
                      <td>
                        <span *ngIf="offer.salaryMin && offer.salaryMax">
                          {{ offer.salaryMin }}€ - {{ offer.salaryMax }}€
                        </span>
                        <span *ngIf="!offer.salaryMin || !offer.salaryMax" class="text-muted">
                          Non spécifié
                        </span>
                      </td>
                      <td>
                        <span class="badge" [ngClass]="getStatusClass(offer.status)">
                          {{ getStatusLabel(offer.status) }}
                        </span>
                      </td>
                      <td>
                        <span class="badge bg-info">{{ offer.applicationsCount || 0 }}</span>
                      </td>
                      <td>
                        <span *ngIf="offer.applicationDeadline">
                          {{ formatDate(offer.applicationDeadline) }}
                        </span>
                        <span *ngIf="!offer.applicationDeadline" class="text-muted">
                          Aucune
                        </span>
                      </td>
                      <td class="text-center">
                        <!-- Copier le lien (seulement si PUBLISHED) -->
                        <button class="btn btn-outline-info btn-sm" *ngIf="offer.status === 'PUBLISHED'" 
                                (click)="copyApplicationLink(offer)" title="Copier le lien du formulaire">
                          <i class="feather icon-copy"></i>
                        </button>
                        <span *ngIf="offer.status !== 'PUBLISHED'" class="text-muted">
                          <i class="feather icon-minus"></i>
                        </span>
                      </td>
                      <td>
                        <div class="btn-group btn-group-sm">
                          <!-- Consulter (toujours visible) -->
                          <button class="btn btn-outline-primary" [routerLink]="['/job-offers', offer.id]" 
                                  title="Consulter">
                            <i class="feather icon-eye"></i>
                          </button>
                          
                          <!-- Modifier (seulement si DRAFT) -->
                          <button class="btn btn-outline-secondary" *ngIf="offer.status === 'DRAFT'" 
                                  [routerLink]="['/job-offers', offer.id, 'edit']" title="Modifier">
                            <i class="feather icon-edit"></i>
                          </button>
                          
                          <!-- Publier (seulement si DRAFT) -->
                          <button class="btn btn-outline-success" *ngIf="offer.status === 'DRAFT'" 
                                  (click)="publishOffer(offer.id)" title="Publier">
                            <i class="feather icon-upload"></i>
                          </button>
                          
                          <!-- Clôturer (si DRAFT ou PUBLISHED) -->
                          <button class="btn btn-outline-danger" 
                                  *ngIf="offer.status === 'DRAFT' || offer.status === 'PUBLISHED'" 
                                  (click)="confirmCloseOffer(offer)" title="Clôturer">
                            <i class="feather icon-x-circle"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <div *ngIf="filteredOffers.length === 0" class="text-center py-4">
                  <p class="text-muted">Aucune offre d'emploi trouvée.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .badge {
      font-size: 0.75rem;
    }
    .btn-group-sm .btn {
      padding: 0.25rem 0.5rem;
    }
  `]
})
export class JobOffersComponent implements OnInit {
  private http = inject(HttpClient);
  
  jobOffers: JobOffer[] = [];
  filteredOffers: JobOffer[] = [];
  selectedStatus = '';
  searchTerm = '';

  ngOnInit(): void {
    this.loadJobOffers();
  }

  loadJobOffers(): void {
    this.http.get<JobOffer[]>('http://localhost:8080/api/job-offers')
      .subscribe({
        next: (offers) => {
          this.jobOffers = offers;
          this.filteredOffers = offers;
        },
        error: (err) => console.error('Erreur lors du chargement des offres', err)
      });
  }

  filterOffers(): void {
    this.filteredOffers = this.jobOffers.filter(offer => {
      const matchesStatus = !this.selectedStatus || offer.status === this.selectedStatus;
      const matchesSearch = !this.searchTerm || 
        offer.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        offer.company.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      return matchesStatus && matchesSearch;
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'DRAFT': return 'bg-secondary';
      case 'PUBLISHED': return 'bg-success';
      case 'CLOSED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'DRAFT': return 'Brouillon';
      case 'PUBLISHED': return 'Publié';
      case 'CLOSED': return 'Fermé';
      default: return status;
    }
  }

  getContractTypeLabel(type: string): string {
    switch (type) {
      case 'CDI': return 'CDI';
      case 'CDD': return 'CDD';
      case 'FREELANCE': return 'Freelance';
      case 'STAGE': return 'Stage';
      case 'APPRENTISSAGE': return 'Apprentissage';
      default: return type;
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }

  confirmCloseOffer(offer: JobOffer): void {
    const message = `Êtes-vous sûr de vouloir clôturer l'offre "${offer.title}" ?\n\nCette action est irréversible et empêchera de nouvelles candidatures.`;
    
    if (confirm(message)) {
      this.closeOffer(offer.id);
    }
  }

  closeOffer(id: number): void {
    this.http.patch(`http://localhost:8080/api/job-offers/${id}/close`, {})
      .subscribe({
        next: () => {
          this.loadJobOffers();
          alert('Offre clôturée avec succès');
        },
        error: (err) => {
          console.error('Erreur lors de la fermeture', err);
          alert('Erreur lors de la clôture de l\'offre');
        }
      });
  }

  publishOffer(id: number): void {
    this.http.patch<JobOffer>(`http://localhost:8080/api/job-offers/${id}/publish`, {})
      .subscribe({
        next: (updatedOffer) => {
          this.loadJobOffers();
          const applicationUrl = updatedOffer.applicationUrl 
            ? `http://localhost:4200${updatedOffer.applicationUrl}`
            : `http://localhost:4200/apply/${id}`;
          const message = `Offre publiée avec succès !\n\nLien d'accès au formulaire :\n${applicationUrl}\n\nVoulez-vous copier le lien ?`;
          
          if (confirm(message)) {
            this.copyToClipboard(applicationUrl);
          }
        },
        error: (err) => {
          console.error('Erreur lors de la publication', err);
          alert('Erreur lors de la publication de l\'offre');
        }
      });
  }

  copyApplicationLink(offer: JobOffer): void {
    const applicationUrl = offer.applicationUrl 
      ? `http://localhost:4200${offer.applicationUrl}`
      : `http://localhost:4200/apply/${offer.id}`;
    this.copyToClipboard(applicationUrl);
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      alert('Lien copié dans le presse-papiers !');
    }).catch(() => {
      alert('Impossible de copier le lien. Veuillez le copier manuellement :\n' + text);
    });
  }
}