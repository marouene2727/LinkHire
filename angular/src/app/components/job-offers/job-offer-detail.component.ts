import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';

declare let bootstrap: any;

interface JobOffer {
  id: number;
  title: string;
  description: string;
  company: string;
  location: string;
  remoteWork: boolean;
  salaryMin?: number;
  salaryMax?: number;
  experienceMin?: number;
  experienceMax?: number;
  contractType: string;
  educationLevel?: string;
  requiredSkills: string;
  preferredSkills?: string;
  languages?: string;
  benefits?: string;
  applicationDeadline?: string;
  startDate?: string;
  contactEmail?: string;
  contactPhone?: string;
  status: string;
  createdAt: string;
  applicationUrl: string;
}

interface Application {
  id: number;
  candidate: {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    linkedinProfile?: string;
  };
  receivedAt: string;
  status: string;
  aiScore?: number;
  aiAnalysis?: string;
  viewedByRecruiter?: boolean;
  viewedAt?: string;
}

@Component({
  selector: 'app-job-offer-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './job-offer-detail.component.html',
  styleUrls: ['./job-offer-detail.component.scss']
})
export class JobOfferDetailComponent implements OnInit {
  jobOffer: JobOffer | null = null;
  applications: Application[] = [];
  filteredApplications: Application[] = [];
  selectedApplications: number[] = [];
  selectedStatus = '';
  selectedViewed = '';
  showArchived = false;
  validatedCandidates: any[] = [];
  selectedCandidateName = '';
  selectedApplicationId = 0;
  bulkValidateForm: FormGroup;
  bulkRejectForm: FormGroup;
  quickValidateForm: FormGroup;
  quickRejectForm: FormGroup;
  
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  constructor() {
    this.bulkValidateForm = this.fb.group({
      interviewDate: [''],
      message: ['']
    });
    this.bulkRejectForm = this.fb.group({
      message: ['']
    });
    this.quickValidateForm = this.fb.group({
      message: ['']
    });
    this.quickRejectForm = this.fb.group({
      message: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadJobOffer(id);
  }

  loadJobOffer(id: number): void {
    this.http.get<JobOffer>(`http://localhost:8080/api/job-offers/${id}`)
      .subscribe({
        next: (job) => {
          this.jobOffer = job;
          if (job.status === 'PUBLISHED' || job.status === 'CLOSED') {
            this.loadApplications(id);
          }
        },
        error: (err) => console.error('Erreur lors du chargement', err)
      });
  }
  
  loadApplications(jobOfferId: number): void {
    const token = localStorage.getItem('token');
    if (!token) {
      console.log('Pas de token, pas de chargement des candidatures');
      return;
    }
    
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.get<Application[]>(`http://localhost:8080/api/applications/job-offer/${jobOfferId}`, { headers })
      .subscribe({
        next: (apps: Application[]) => {
          this.applications = apps;
          this.filteredApplications = apps;
          this.loadValidatedCandidates();
          console.log('Candidatures chargées:', apps);
          // Initialiser les tooltips après le chargement des données
          setTimeout(() => this.initTooltips(), 100);
        },
        error: (err) => {
          console.error('Erreur candidatures', err);
          if (err.status === 404) {
            console.log('Endpoint non trouvé - vérifier que le serveur Spring Boot est démarré');
          }
        }
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

  getEducationLabel(level: string): string {
    switch (level) {
      case 'BAC': return 'Bac';
      case 'BAC_PLUS_2': return 'Bac +2';
      case 'BAC_PLUS_3': return 'Bac +3';
      case 'BAC_PLUS_5': return 'Bac +5';
      case 'DOCTORAT': return 'Doctorat';
      case 'AUTRE': return 'Autre';
      default: return level;
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }

  getScoreClass(score: number): string {
    if (score >= 15) return 'bg-success';
    if (score >= 10) return 'bg-warning';
    return 'bg-danger';
  }

  getScoreBreakdown(totalScore: number): string {
    // Simulation de la décomposition du score basée sur les critères d'évaluation
    const technical = Math.min(8, Math.round(totalScore * 0.4)); // 40% pour les compétences techniques (max 8)
    const experience = Math.min(6, Math.round(totalScore * 0.3)); // 30% pour l'expérience (max 6)
    const education = Math.min(3, Math.round(totalScore * 0.15)); // 15% pour la formation (max 3)
    const motivation = Math.min(3, Math.round(totalScore * 0.15)); // 15% pour la motivation (max 3)
    
    return `Détail de la note:\n• Compétences techniques: ${technical}/8\n• Expérience: ${experience}/6\n• Formation: ${education}/3\n• Motivation: ${motivation}/3\n\nTotal: ${totalScore}/20`;
  }

  copyApplicationLink(): void {
    if (this.jobOffer) {
      const link = `http://localhost:4200${this.jobOffer.applicationUrl}`;
      navigator.clipboard.writeText(link).then(() => {
        alert('Lien copié dans le presse-papiers !');
      });
    }
  }

  publishOffer(): void {
    if (!this.jobOffer) return;
    
    this.http.patch(`http://localhost:8080/api/job-offers/${this.jobOffer.id}/publish`, {})
      .subscribe({
        next: (updatedOffer: any) => {
          this.jobOffer = updatedOffer;
          const applicationUrl = `http://localhost:4200/apply${updatedOffer.applicationUrl || '/' + this.jobOffer!.id}`;
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

  closeOffer(): void {
    if (!this.jobOffer) return;
    
    const message = `Êtes-vous sûr de vouloir clôturer l'offre "${this.jobOffer.title}" ?\n\nCette action est irréversible et empêchera de nouvelles candidatures.`;
    
    if (confirm(message)) {
      this.http.patch(`http://localhost:8080/api/job-offers/${this.jobOffer.id}/close`, {})
        .subscribe({
          next: (updatedOffer: any) => {
            this.jobOffer = updatedOffer;
            alert('Offre clôturée avec succès');
          },
          error: (err) => {
            console.error('Erreur lors de la clôture', err);
            alert('Erreur lors de la clôture de l\'offre');
          }
        });
    }
  }

  private copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      alert('Lien copié dans le presse-papiers !');
    }).catch(() => {
      alert('Impossible de copier le lien. Veuillez le copier manuellement :\n' + text);
    });
  }

  refreshApplications(): void {
    if (this.jobOffer) {
      this.loadApplications(this.jobOffer.id);
    }
  }

  filterApplications(): void {
    this.filteredApplications = this.applications.filter(app => {
      const matchesStatus = !this.selectedStatus || app.status === this.selectedStatus;
      const matchesViewed = !this.selectedViewed || 
        (this.selectedViewed === 'true' && app.viewedByRecruiter) ||
        (this.selectedViewed === 'false' && !app.viewedByRecruiter);
      
      return matchesStatus && matchesViewed;
    });
    // Réinitialiser la sélection après filtrage
    this.selectedApplications = [];
  }

  isSelected(appId: number): boolean {
    return this.selectedApplications.includes(appId);
  }

  toggleSelection(appId: number): void {
    const index = this.selectedApplications.indexOf(appId);
    if (index > -1) {
      this.selectedApplications.splice(index, 1);
    } else {
      this.selectedApplications.push(appId);
    }
  }

  isAllSelected(): boolean {
    return this.filteredApplications.length > 0 && 
           this.selectedApplications.length === this.filteredApplications.length;
  }

  isSomeSelected(): boolean {
    return this.selectedApplications.length > 0 && 
           this.selectedApplications.length < this.filteredApplications.length;
  }

  toggleAllSelection(): void {
    if (this.isAllSelected()) {
      this.selectedApplications = [];
    } else {
      this.selectedApplications = this.filteredApplications.map(app => app.id);
    }
  }

  allSelectedAreViewed(): boolean {
    return this.selectedApplications.every(id => {
      const app = this.applications.find(a => a.id === id);
      return app?.viewedByRecruiter === true;
    });
  }

  openBulkValidateModal(): void {
    this.bulkValidateForm.patchValue({ message: this.getBulkValidationTemplate() });
    const modal = new bootstrap.Modal(document.getElementById('bulkValidateModal'));
    modal.show();
  }

  openBulkRejectModal(): void {
    this.bulkRejectForm.patchValue({ message: this.getBulkRejectionTemplate() });
    const modal = new bootstrap.Modal(document.getElementById('bulkRejectModal'));
    modal.show();
  }

  getBulkValidationTemplate(): string {
    const interviewDate = this.bulkValidateForm?.get('interviewDate')?.value;
    const interviewText = interviewDate ? 
      `\n\nNous souhaitons vous rencontrer pour un entretien le ${new Date(interviewDate).toLocaleDateString('fr-FR')} à ${new Date(interviewDate).toLocaleTimeString('fr-FR', {hour: '2-digit', minute: '2-digit'})}.` : 
      '\n\nNous vous recontacterons prochainement pour organiser un entretien.';
    
    return `Bonjour [nom du candidat],\n\nNous avons le plaisir de vous informer que votre candidature pour le poste de ${this.jobOffer?.title} chez ${this.jobOffer?.company} a retenu notre attention.${interviewText}\n\nCordialement,\nL'équipe de recrutement`;
  }

  getBulkRejectionTemplate(): string {
    return `Bonjour [nom du candidat],\n\nNous vous remercions pour l'intérêt que vous portez à notre entreprise et pour le temps consacré à votre candidature pour le poste de ${this.jobOffer?.title}.\n\nAprès étude attentive de votre profil, nous regrettons de vous informer que nous ne pouvons pas donner suite à votre candidature pour ce poste.\n\nNous vous souhaitons pleine réussite dans vos recherches.\n\nCordialement,\nL'équipe de recrutement`;
  }

  updateBulkValidationTemplate(): void {
    this.bulkValidateForm.patchValue({ message: this.getBulkValidationTemplate() });
  }

  getCandidateName(appId: number): string {
    const app = this.applications.find(a => a.id === appId);
    return app ? `${app.candidate.firstName} ${app.candidate.lastName}` : 'Candidat inconnu';
  }

  confirmBulkValidation(): void {
    const formData = this.bulkValidateForm.value;
    this.bulkValidateApplications(formData.message, formData.interviewDate);
    bootstrap.Modal.getInstance(document.getElementById('bulkValidateModal')).hide();
  }

  confirmBulkRejection(): void {
    const message = this.bulkRejectForm.value.message;
    this.bulkRejectApplications(message);
    bootstrap.Modal.getInstance(document.getElementById('bulkRejectModal')).hide();
  }

  loadValidatedCandidates(): void {
    if (!this.jobOffer) return;
    
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.get<any[]>(`http://localhost:8080/api/applications/job-offer/${this.jobOffer.id}/validated`, { headers })
      .subscribe({
        next: (candidates) => {
          this.validatedCandidates = candidates;
        },
        error: (err) => console.error('Erreur candidats validés', err)
      });
  }

  openQuickValidateModal(appId: number, candidateName: string): void {
    this.selectedApplicationId = appId;
    this.selectedCandidateName = candidateName;
    this.quickValidateForm.patchValue({
      message: `Félicitations ${candidateName} ! Votre candidature a été retenue.`
    });
    const modal = new bootstrap.Modal(document.getElementById('quickValidateModal'));
    modal.show();
  }

  openQuickRejectModal(appId: number, candidateName: string): void {
    this.selectedApplicationId = appId;
    this.selectedCandidateName = candidateName;
    this.quickRejectForm.patchValue({
      message: `Bonjour ${candidateName}, nous ne pouvons pas donner suite à votre candidature.`
    });
    const modal = new bootstrap.Modal(document.getElementById('quickRejectModal'));
    modal.show();
  }

  confirmQuickValidation(): void {
    const message = this.quickValidateForm.value.message;
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.post(`http://localhost:8080/api/applications/${this.selectedApplicationId}/validate`, 
      { message }, { headers })
      .subscribe({
        next: () => {
          bootstrap.Modal.getInstance(document.getElementById('quickValidateModal')).hide();
          this.refreshApplications();
          this.loadValidatedCandidates();
          alert('Candidature validée avec succès');
        },
        error: (err) => {
          console.error('Erreur validation:', err);
          alert('Erreur lors de la validation');
        }
      });
  }

  confirmQuickRejection(): void {
    const message = this.quickRejectForm.value.message;
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.post(`http://localhost:8080/api/applications/${this.selectedApplicationId}/reject`, 
      { message }, { headers })
      .subscribe({
        next: () => {
          bootstrap.Modal.getInstance(document.getElementById('quickRejectModal')).hide();
          this.refreshApplications();
          alert('Candidature rejetée');
        },
        error: (err) => {
          console.error('Erreur rejet:', err);
          alert('Erreur lors du rejet');
        }
      });
  }

  toggleArchivedView(): void {
    if (this.showArchived) {
      // Charger toutes les candidatures y compris archivées
      this.loadAllApplications();
    } else {
      // Recharger seulement les candidatures actives
      this.refreshApplications();
    }
  }

  loadAllApplications(): void {
    if (!this.jobOffer) return;
    
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    // Endpoint spécial pour récupérer toutes les candidatures y compris archivées
    this.http.get<Application[]>(`http://localhost:8080/api/applications/job-offer/${this.jobOffer.id}/all`, { headers })
      .subscribe({
        next: (apps: Application[]) => {
          this.applications = apps;
          this.filteredApplications = apps;
          this.filterApplications();
        },
        error: (err) => {
          console.error('Erreur candidatures avec archivées', err);
          // Fallback sur l'endpoint normal
          this.refreshApplications();
        }
      });
  }

  bulkValidateApplications(message: string, interviewDate?: string): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    const promises = this.selectedApplications.map(id => 
      this.http.post(`http://localhost:8080/api/applications/${id}/validate`, 
        { message, interviewDate }, { headers }).toPromise()
    );
    
    Promise.all(promises).then(() => {
      alert(`${this.selectedApplications.length} candidature(s) validée(s) avec succès`);
      this.selectedApplications = [];
      this.refreshApplications();
    }).catch(err => {
      console.error('Erreur validation en lot:', err);
      alert('Erreur lors de la validation en lot');
    });
  }

  bulkRejectApplications(message: string): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    const promises = this.selectedApplications.map(id => 
      this.http.post(`http://localhost:8080/api/applications/${id}/reject`, 
        { message }, { headers }).toPromise()
    );
    
    Promise.all(promises).then(() => {
      alert(`${this.selectedApplications.length} candidature(s) rejetée(s)`);
      this.selectedApplications = [];
      this.refreshApplications();
    }).catch(err => {
      console.error('Erreur rejet en lot:', err);
      alert('Erreur lors du rejet en lot');
    });
  }

  validateApplication(applicationId: number): void {
    const message = prompt('Message de validation (sera envoyé par email au candidat):');
    if (message !== null) {
      const token = localStorage.getItem('token');
      const headers = { 'Authorization': `Bearer ${token}` };
      
      this.http.post(`http://localhost:8080/api/applications/${applicationId}/validate`, 
        { message: message || 'Félicitations ! Votre candidature a été retenue.' }, { headers })
        .subscribe({
          next: () => {
            alert('Candidature validée et email envoyé au candidat');
            this.refreshApplications();
          },
          error: (err) => {
            console.error('Erreur validation:', err);
            alert('Erreur lors de la validation');
          }
        });
    }
  }

  rejectApplication(applicationId: number): void {
    const message = prompt('Raison du rejet (sera envoyée par email au candidat):');
    if (message !== null) {
      const token = localStorage.getItem('token');
      const headers = { 'Authorization': `Bearer ${token}` };
      
      this.http.post(`http://localhost:8080/api/applications/${applicationId}/reject`, 
        { message: message || 'Nous vous remercions pour votre candidature, cependant nous ne pouvons pas donner suite.' }, { headers })
        .subscribe({
          next: () => {
            alert('Candidature rejetée et email envoyé au candidat');
            this.refreshApplications();
          },
          error: (err) => {
            console.error('Erreur rejet:', err);
            alert('Erreur lors du rejet');
          }
        });
    }
  }

  private initTooltips(): void {
    // Initialiser les tooltips Bootstrap
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(tooltipTriggerEl => {
      // @ts-ignore
      new bootstrap.Tooltip(tooltipTriggerEl);
    });
  }
}