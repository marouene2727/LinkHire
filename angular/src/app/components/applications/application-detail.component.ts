import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Location } from '@angular/common';

declare let bootstrap: any;

interface ApplicationDetail {
  id: number;
  candidate: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    linkedinProfile?: string;
    githubProfile?: string;
    currentPosition?: string;
    currentCompany?: string;
    location?: string;
    yearsExperience?: number;
    skills?: string;
  };
  jobOffer: {
    id: number;
    title: string;
    company: string;
    location: string;
  };
  receivedAt: string;
  status: string;
  aiScore?: number;
  aiAnalysis?: string;
  emailSubject?: string;
  emailBody?: string;
  recruiterNotes?: string;
  archived?: boolean;
  archivedAt?: string;
  documents: {
    id: number;
    fileName: string;
    documentType: string;
    fileSize: number;
  }[];
}

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './application-detail.component.html',
  styleUrls: ['./application-detail.component.scss']
})
export class ApplicationDetailComponent implements OnInit {
  application: ApplicationDetail | null = null;
  validateForm: FormGroup;
  rejectForm: FormGroup;
  notesForm: FormGroup;
  contactForm: FormGroup;
  
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private location = inject(Location);

  constructor() {
    this.validateForm = this.fb.group({
      interviewDate: [''],
      message: ['']
    });
    this.rejectForm = this.fb.group({
      message: ['']
    });
    this.notesForm = this.fb.group({
      notes: ['']
    });
    this.contactForm = this.fb.group({
      subject: [''],
      message: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadApplication(id);
  }

  loadApplication(id: number): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.get<ApplicationDetail>(`http://localhost:8080/api/applications/${id}`, { headers })
      .subscribe({
        next: (app) => {
          this.application = app;
        },
        error: (err) => console.error('Erreur lors du chargement', err)
      });
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

  getScoreBreakdown(totalScore: number): string {
    const technical = Math.min(8, Math.round(totalScore * 0.4));
    const experience = Math.min(6, Math.round(totalScore * 0.3));
    const education = Math.min(3, Math.round(totalScore * 0.15));
    const motivation = Math.min(3, Math.round(totalScore * 0.15));
    
    return `Détail de la note:\n• Compétences techniques: ${technical}/8\n• Expérience: ${experience}/6\n• Formation: ${education}/3\n• Motivation: ${motivation}/3\n\nTotal: ${totalScore}/20`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }

  getCleanFileName(fileName: string): string {
    // Supprimer l'UUID au début du nom de fichier
    const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_/;
    return fileName.replace(uuidPattern, '');
  }

  formatAiAnalysis(analysis: string): string {
    if (!analysis) return '';
    return analysis.replace(/\\n/g, '<br>');
  }

  viewDocument(docId: number, fileName: string): void {
    window.open(`http://localhost:8080/api/applications/documents/${docId}/download?token=view`, '_blank');
  }

  downloadDocument(docId: number, fileName: string): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.get(`http://localhost:8080/api/applications/documents/${docId}/download`, 
      { headers, responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = this.getCleanFileName(fileName);
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => console.error('Erreur téléchargement', err)
      });
  }

  openValidateModal(): void {
    this.validateForm.patchValue({ message: this.getValidationTemplate() });
    const modal = new bootstrap.Modal(document.getElementById('validateModal'));
    modal.show();
  }

  openRejectModal(): void {
    this.rejectForm.patchValue({ message: this.getRejectionTemplate() });
    const modal = new bootstrap.Modal(document.getElementById('rejectModal'));
    modal.show();
  }

  openNotesModal(): void {
    this.notesForm.patchValue({ notes: this.application?.recruiterNotes || '' });
    const modal = new bootstrap.Modal(document.getElementById('notesModal'));
    modal.show();
  }

  openContactModal(): void {
    this.contactForm.patchValue({
      subject: `Concernant votre candidature - ${this.application?.jobOffer?.title}`,
      message: ''
    });
    const modal = new bootstrap.Modal(document.getElementById('contactModal'));
    modal.show();
  }

  getValidationTemplate(): string {
    const interviewDate = this.validateForm?.get('interviewDate')?.value;
    const interviewText = interviewDate ? 
      `\n\nNous souhaitons vous rencontrer pour un entretien le ${new Date(interviewDate).toLocaleDateString('fr-FR')} à ${new Date(interviewDate).toLocaleTimeString('fr-FR', {hour: '2-digit', minute: '2-digit'})}.` : 
      '\n\nNous vous recontacterons prochainement pour organiser un entretien.';
    
    return `Bonjour ${this.application?.candidate?.firstName},\n\nNous avons le plaisir de vous informer que votre candidature pour le poste de ${this.application?.jobOffer?.title} chez ${this.application?.jobOffer?.company} a retenu notre attention.${interviewText}\n\nCordialement,\nL'équipe de recrutement`;
  }

  updateValidationTemplate(): void {
    this.validateForm.patchValue({ message: this.getValidationTemplate() });
  }

  getRejectionTemplate(): string {
    let feedback = '';
    if (this.application?.aiAnalysis && this.application?.aiScore) {
      const score = this.application.aiScore;
      const reasons = [];
      
      if (score < 15) {
        if (this.application.aiAnalysis.includes('Expérience')) {
          reasons.push('le niveau d\'expérience requis pour ce poste');
        }
        if (this.application.aiAnalysis.includes('Compétences')) {
          reasons.push('l\'adéquation des compétences techniques');
        }
        if (this.application.aiAnalysis.includes('Motivation')) {
          reasons.push('l\'alignement avec nos critères de sélection');
        }
      }
      
      if (reasons.length > 0) {
        feedback = `\n\nAprès analyse de votre profil, nous avons identifié des écarts concernant ${reasons.join(', ')}. Bien que votre candidature présente des aspects intéressants, nous recherchons un profil correspondant plus précisément à nos besoins actuels.`;
      }
    }
    
    return `Bonjour ${this.application?.candidate?.firstName},\n\nNous vous remercions pour l'intérêt que vous portez à notre entreprise et pour le temps consacré à votre candidature pour le poste de ${this.application?.jobOffer?.title}.\n\nAprès étude attentive de votre profil, nous regrettons de vous informer que nous ne pouvons pas donner suite à votre candidature pour ce poste.${feedback}\n\nNous vous encourageons à postuler à nouveau pour d'autres opportunités qui pourraient mieux correspondre à votre profil.\n\nNous vous souhaitons pleine réussite dans vos recherches.\n\nCordialement,\nL'équipe de recrutement`;
  }

  validateApplication(): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    const formData = this.validateForm.value;
    
    this.http.post(`http://localhost:8080/api/applications/${this.application?.id}/validate`, {
      message: formData.message,
      interviewDate: formData.interviewDate
    }, { headers }).subscribe({
      next: () => {
        this.application!.status = 'VALIDATED';
        bootstrap.Modal.getInstance(document.getElementById('validateModal')).hide();
        alert('Email de validation envoyé avec succès !');
      },
      error: (err) => {
        console.error('Erreur validation', err);
        alert('Erreur lors de l\'envoi de l\'email');
      }
    });
  }

  rejectApplication(): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.post(`http://localhost:8080/api/applications/${this.application?.id}/reject`, {
      message: this.rejectForm.value.message
    }, { headers }).subscribe({
      next: () => {
        this.application!.status = 'REJECTED';
        bootstrap.Modal.getInstance(document.getElementById('rejectModal')).hide();
        alert('Email de rejet envoyé avec succès !');
      },
      error: (err) => {
        console.error('Erreur rejet', err);
        alert('Erreur lors de l\'envoi de l\'email');
      }
    });
  }

  saveNotes(): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.put(`http://localhost:8080/api/applications/${this.application?.id}/notes`, {
      notes: this.notesForm.value.notes
    }, { headers }).subscribe({
      next: () => {
        this.application!.recruiterNotes = this.notesForm.value.notes;
        bootstrap.Modal.getInstance(document.getElementById('notesModal')).hide();
        alert('Notes sauvegardées avec succès !');
      },
      error: (err) => {
        console.error('Erreur sauvegarde notes', err);
        alert('Erreur lors de la sauvegarde');
      }
    });
  }

  sendContactEmail(): void {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    this.http.post(`http://localhost:8080/api/applications/${this.application?.id}/contact`, {
      subject: this.contactForm.value.subject,
      message: this.contactForm.value.message
    }, { headers }).subscribe({
      next: () => {
        bootstrap.Modal.getInstance(document.getElementById('contactModal')).hide();
        alert('Email envoyé avec succès !');
      },
      error: (err) => {
        console.error('Erreur envoi email', err);
        alert('Erreur lors de l\'envoi de l\'email');
      }
    });
  }

  goBack(): void {
    this.location.back();
  }
}