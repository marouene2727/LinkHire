import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface JobOfferData {
  id: number;
  title: string;
  company: string;
  location: string;
  contractType: string;
  salaryMin?: number;
  salaryMax?: number;
  experienceMin?: number;
  experienceMax?: number;
  description?: string;
  requiredSkills?: string;
  languages?: string;
  certifications?: string;
  benefits?: string;
  contactEmail?: string;
  contactPhone?: string;
  teletravail?: string;
}

// interface CandidateApplication {
//   firstName: string;
//   lastName: string;
//   phone: string;
//   email: string;
//   linkedinUrl?: string;
//   githubUrl?: string;
//   cv?: File;
//   coverLetter?: File;
//   message: string;
// }

@Component({
  selector: 'app-candidate-application',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="application-container">
      <div class="container-fluid">
        <div class="row g-4">
          <!-- Aperçu du poste - Gauche -->
          <div class="col-lg-5">
            <div class="card job-preview-card h-100">
              <div class="card-header bg-primary text-white">
                <h5 class="mb-0"><i class="feather icon-briefcase me-2"></i>Aperçu du poste</h5>
              </div>
              <div class="card-body preview-content" *ngIf="jobOffer; else noData">
                <!-- Logo et titre -->
                <div class="text-center mb-4 mt-3">
                  <img src="assets/images/images perso/office-building.png" alt="Logo" class="company-logo mb-3">
                  <h2 class="job-title mb-2">{{ jobOffer.title }}</h2>
                  <h4 class="company-name text-muted">{{ jobOffer.company }}</h4>
                </div>

                <!-- Informations principales -->
                <div class="info-grid mb-4">
                  <div class="info-item" *ngIf="jobOffer.location">
                    <i class="feather icon-map-pin"></i>
                    <span>{{ jobOffer.location }}</span>
                  </div>
                  <div class="info-item" *ngIf="jobOffer.contractType">
                    <i class="feather icon-briefcase"></i>
                    <span>{{ jobOffer.contractType }}</span>
                  </div>
                  <div class="info-item" *ngIf="jobOffer.salaryMin && jobOffer.salaryMax">
                    <i class="feather icon-dollar-sign"></i>
                    <span>{{ jobOffer.salaryMin }}€ - {{ jobOffer.salaryMax }}€</span>
                  </div>
                  <div class="info-item" *ngIf="jobOffer.experienceMin !== null && jobOffer.experienceMax !== null">
                    <i class="feather icon-award"></i>
                    <span>{{ jobOffer.experienceMin }} - {{ jobOffer.experienceMax }} ans</span>
                  </div>
                  <div class="info-item" *ngIf="jobOffer.teletravail">
                    <i class="feather icon-home"></i>
                    <span>{{ jobOffer.teletravail }}</span>
                  </div>
                </div>

                <!-- Description -->
                <div class="section mb-4" *ngIf="jobOffer.description">
                  <h5 class="section-title">Description du poste</h5>
                  <div class="section-content">
                    <p style="white-space: pre-line;">{{ jobOffer.description }}</p>
                  </div>
                </div>

                <!-- Compétences requises -->
                <div class="section mb-4" *ngIf="jobOffer.requiredSkills">
                  <h5 class="section-title">Compétences requises</h5>
                  <div class="section-content">
                    <div class="skills-container">
                      <span class="skill-badge" *ngFor="let skill of getSkillsArray()">{{ skill }}</span>
                    </div>
                  </div>
                </div>

                <!-- Langues -->
                <div class="section mb-4" *ngIf="jobOffer.languages">
                  <h5 class="section-title">Langues</h5>
                  <div class="section-content">
                    <div class="languages-container">
                      <span class="language-badge" *ngFor="let lang of getLanguagesArray()">{{ lang }}</span>
                    </div>
                  </div>
                </div>

                <!-- Certifications -->
                <div class="section mb-4" *ngIf="jobOffer.certifications">
                  <h5 class="section-title">Certifications souhaitées</h5>
                  <div class="section-content">
                    <div class="certifications-container">
                      <span class="certification-badge" *ngFor="let cert of getCertificationsArray()">{{ cert }}</span>
                    </div>
                  </div>
                </div>

                <!-- Avantages -->
                <div class="section mb-4" *ngIf="jobOffer.benefits">
                  <h5 class="section-title">Avantages</h5>
                  <div class="section-content">
                    <p style="white-space: pre-line;">{{ jobOffer.benefits }}</p>
                  </div>
                </div>

                <!-- Contact -->
                <div class="section" *ngIf="jobOffer.contactEmail || jobOffer.contactPhone">
                  <h5 class="section-title">Contact</h5>
                  <div class="section-content">
                    <div class="contact-info">
                      <div *ngIf="jobOffer.contactEmail" class="contact-item">
                        <i class="feather icon-mail"></i>
                        <span>{{ jobOffer.contactEmail }}</span>
                      </div>
                      <div *ngIf="jobOffer.contactPhone" class="contact-item">
                        <i class="feather icon-phone"></i>
                        <span>{{ jobOffer.contactPhone }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <ng-template #noData>
                <div class="card-body text-center">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Chargement...</span>
                  </div>
                  <p class="mt-3">Chargement de l'offre d'emploi...</p>
                  <small class="text-muted">ID: {{ jobId }}</small>
                </div>
              </ng-template>
            </div>
          </div>
          
          <!-- Formulaire de candidature - Droite -->
          <div class="col-lg-7">
            <div class="card application-form-card h-100">
              <div class="card-header bg-success text-white">
                <h5 class="mb-0"><i class="feather icon-user-plus me-2"></i>Postuler à cette offre</h5>
              </div>
              <div class="card-body">
                <form [formGroup]="applicationForm" (ngSubmit)="onSubmit()">
                  <div class="row mt-3">
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Prénom *</label>
                      <input type="text" class="form-control" formControlName="firstName" 
                             [class.is-invalid]="isFieldInvalid('firstName')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('firstName')">
                        {{ getFieldError('firstName') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Nom *</label>
                      <input type="text" class="form-control" formControlName="lastName"
                             [class.is-invalid]="isFieldInvalid('lastName')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('lastName')">
                        {{ getFieldError('lastName') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Email *</label>
                      <input type="email" class="form-control" formControlName="email"
                             [class.is-invalid]="isFieldInvalid('email')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('email')">
                        {{ getFieldError('email') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Téléphone *</label>
                      <input type="tel" class="form-control" formControlName="phone"
                             [class.is-invalid]="isFieldInvalid('phone')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('phone')">
                        {{ getFieldError('phone') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">LinkedIn</label>
                      <input type="url" class="form-control" formControlName="linkedinUrl"
                             placeholder="https://linkedin.com/in/votre-profil"
                             [class.is-invalid]="isFieldInvalid('linkedinUrl')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('linkedinUrl')">
                        {{ getFieldError('linkedinUrl') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">GitHub</label>
                      <input type="url" class="form-control" formControlName="githubUrl"
                             placeholder="https://github.com/votre-profil"
                             [class.is-invalid]="isFieldInvalid('githubUrl')">
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('githubUrl')">
                        {{ getFieldError('githubUrl') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Poste actuel</label>
                      <input type="text" class="form-control" formControlName="currentPosition"
                             placeholder="Ex: Développeur Full Stack">
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Entreprise actuelle</label>
                      <input type="text" class="form-control" formControlName="currentCompany"
                             placeholder="Ex: Google, Microsoft...">
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Localisation</label>
                      <input type="text" class="form-control" formControlName="location"
                             placeholder="Ex: Paris, Lyon, Remote...">
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">CV *</label>
                      <input type="file" class="form-control" accept=".pdf,.doc,.docx" 
                             (change)="onFileSelect($event, 'cv')"
                             [class.is-invalid]="isFieldInvalid('cv')">
                      <div class="form-text">Formats acceptés: PDF, DOC, DOCX (max 5MB)</div>
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('cv')">
                        {{ getFieldError('cv') }}
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label class="form-label">Lettre de motivation</label>
                      <input type="file" class="form-control" accept=".pdf,.doc,.docx" 
                             (change)="onFileSelect($event, 'coverLetter')"
                             [class.is-invalid]="isFieldInvalid('coverLetter')">
                      <div class="form-text">Formats acceptés: PDF, DOC, DOCX (max 5MB)</div>
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('coverLetter')">
                        {{ getFieldError('coverLetter') }}
                      </div>
                    </div>
                    <div class="col-12 mb-4">
                      <label class="form-label">Message de motivation *</label>
                      <textarea class="form-control" rows="5" formControlName="message"
                                placeholder="Expliquez pourquoi vous êtes intéressé(e) par ce poste..."
                                [class.is-invalid]="isFieldInvalid('message')"></textarea>
                      <div class="invalid-feedback" *ngIf="isFieldInvalid('message')">
                        {{ getFieldError('message') }}
                      </div>
                    </div>
                  </div>
                  
                  <div class="d-flex gap-3">
                    <button type="submit" class="btn btn-success" [disabled]="isSubmitting || applicationForm.invalid">
                      <span *ngIf="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                      <i class="feather icon-send me-2" *ngIf="!isSubmitting"></i>
                      {{ isSubmitting ? 'Envoi en cours...' : 'Envoyer ma candidature' }}
                    </button>
                    <button type="button" class="btn btn-outline-secondary" (click)="goBack()">
                      <i class="feather icon-arrow-left me-2"></i>
                      Retour
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrl: './candidate-application.component.scss'
})
export class CandidateApplicationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  applicationForm: FormGroup;
  isSubmitting = false;
  jobOffer: JobOfferData | null = null;
  jobId: string | null = null;

  constructor() {
    this.applicationForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      phone: ['', [Validators.required, Validators.pattern(/^[+]?[0-9\s\-()]{10,}$/)]],
      email: ['', [Validators.required, Validators.email]],
      linkedinUrl: ['', [Validators.pattern(/^https:\/\/(www\.)?linkedin\.com\/.*$/)]],
      githubUrl: ['', [Validators.pattern(/^https:\/\/(www\.)?github\.com\/.*$/)]],
      currentPosition: [''],
      currentCompany: [''],
      location: [''],
      cv: [null, [Validators.required]],
      coverLetter: [null],
      message: ['', [Validators.required, Validators.minLength(50)]]
    });
  }

  ngOnInit(): void {
    this.jobId = this.route.snapshot.paramMap.get('id');
    if (this.jobId) {
      this.loadJobOffer();
    }
  }

  private loadJobOffer(): void {
    // Si l'ID est un UUID (contient des tirets), c'est une applicationUrl
    const isUUID = this.jobId?.includes('-');
    const endpoint = isUUID 
      ? `http://localhost:8080/api/job-offers/apply/${this.jobId}`
      : `http://localhost:8080/api/job-offers/${this.jobId}`;
    
    console.log('Chargement de l\'offre depuis:', endpoint);
      
    this.http.get<JobOfferData>(endpoint)
      .subscribe({
        next: (jobOffer) => {
          console.log('Offre chargée:', jobOffer);
          this.jobOffer = jobOffer;
        },
        error: (error) => {
          console.error('Erreur lors du chargement de l\'offre:', error);
          if (error.status === 410) {
            alert('Cette offre d\'emploi n\'est plus disponible. Elle a été clôturée par le recruteur.');
            window.close();
          } else if (error.status === 404) {
            alert('Offre d\'emploi non trouvée.');
            window.close();
          }
        }
      });
  }

  onFileSelect(event: Event, fieldName: string): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      // Validation du type de fichier
      const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
      if (!allowedTypes.includes(file.type)) {
        alert('Seuls les fichiers PDF et Word sont acceptés');
        input.value = '';
        return;
      }
      
      // Validation de la taille (5MB max)
      if (file.size > 5 * 1024 * 1024) {
        alert('Le fichier ne doit pas dépasser 5MB');
        input.value = '';
        return;
      }
      
      this.applicationForm.patchValue({ [fieldName]: file });
    }
  }

  onSubmit(): void {
    if (this.applicationForm.valid && this.jobOffer) {
      this.isSubmitting = true;
      
      const formData = new FormData();
      const formValue = this.applicationForm.value;
      
      // Ajout de l'ID de l'offre d'emploi
      formData.append('jobOfferId', this.jobOffer.id.toString());
      
      // Ajout des champs texte
      formData.append('firstName', formValue.firstName);
      formData.append('lastName', formValue.lastName);
      formData.append('email', formValue.email);
      formData.append('phone', formValue.phone);
      formData.append('message', formValue.message);
      
      if (formValue.linkedinUrl) {
        formData.append('linkedinUrl', formValue.linkedinUrl);
      }
      if (formValue.githubUrl) {
        formData.append('githubUrl', formValue.githubUrl);
      }
      if (formValue.currentPosition) {
        formData.append('currentPosition', formValue.currentPosition);
      }
      if (formValue.currentCompany) {
        formData.append('currentCompany', formValue.currentCompany);
      }
      if (formValue.location) {
        formData.append('location', formValue.location);
      }
      
      // Ajout des fichiers
      if (formValue.cv) {
        formData.append('cv', formValue.cv);
      }
      if (formValue.coverLetter) {
        formData.append('coverLetter', formValue.coverLetter);
      }
      
      // Appel API pour soumettre la candidature
      this.http.post('http://localhost:8080/api/applications/submit', formData)
        .subscribe({
          next: (response) => {
            console.log('Candidature soumise avec succès:', response);
            alert('Candidature envoyée avec succès !');
            this.applicationForm.reset();
            this.isSubmitting = false;
          },
          error: (error) => {
            console.error('Erreur lors de l\'envoi de la candidature:', error);
            if (error.status === 410) {
              alert('Cette offre d\'emploi n\'est plus disponible. Elle a été clôturée pendant que vous remplissiez le formulaire.');
              window.close();
            } else {
              alert('Erreur lors de l\'envoi de la candidature. Veuillez réessayer.');
            }
            this.isSubmitting = false;
          }
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.applicationForm.controls).forEach(key => {
      const control = this.applicationForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.applicationForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.applicationForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} est requis`;
      if (field.errors['email']) return 'Email invalide';
      if (field.errors['minlength']) return `Minimum ${field.errors['minlength'].requiredLength} caractères`;
      if (field.errors['pattern']) {
        if (fieldName === 'phone') return 'Numéro de téléphone invalide';
        if (fieldName === 'linkedinUrl') return 'URL LinkedIn invalide';
        if (fieldName === 'githubUrl') return 'URL GitHub invalide';
      }
    }
    return '';
  }

  getSkillsArray(): string[] {
    if (!this.jobOffer?.requiredSkills) return [];
    return this.jobOffer.requiredSkills.split(',').map((skill: string) => skill.trim());
  }

  getLanguagesArray(): string[] {
    if (!this.jobOffer?.languages) return [];
    return this.jobOffer.languages.split(',').map((lang: string) => lang.trim());
  }

  getCertificationsArray(): string[] {
    if (!this.jobOffer?.certifications) return [];
    return this.jobOffer.certifications.split(',').map((cert: string) => cert.trim());
  }

  goBack(): void {
    window.history.back();
  }
}