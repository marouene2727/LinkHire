import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface JobOfferResponse {
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
  previewVisibility?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  applicationUrl?: string;
  expiresAt?: string;
}

@Component({
  selector: 'app-job-offer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './job-offer-form.component.html',
  styleUrls: ['./job-offer-form.component.scss']
})
export class JobOfferFormComponent implements OnInit {
  jobForm: FormGroup;
  loading = false;
  error = '';
  isEdit = false;
  jobId?: number;
  activeAccordion = 'general';
  currentJobOffer?: JobOfferResponse;
  
  // Gestion des compétences
  selectedSkills: string[] = [];
  skillSearchTerm = '';
  availableSkills = [
    'JavaScript', 'TypeScript', 'Angular', 'React', 'Vue.js', 'Node.js', 'Express',
    'Java', 'Spring Boot', 'Python', 'Django', 'Flask', 'C#', '.NET', 'PHP', 'Laravel',
    'HTML', 'CSS', 'SCSS', 'Bootstrap', 'Tailwind CSS', 'MySQL', 'PostgreSQL', 'MongoDB',
    'Docker', 'Kubernetes', 'AWS', 'Azure', 'GCP', 'Git', 'Jenkins', 'CI/CD',
    'REST API', 'GraphQL', 'Microservices', 'Agile', 'Scrum', 'TDD', 'Unit Testing'
  ].sort();

  // Gestion des langues
  selectedLanguages: {language: string, level: number, type: string}[] = [];
  languageSearchTerm = '';
  availableLanguages = [
    'Français', 'Anglais', 'Espagnol', 'Allemand', 'Italien', 'Portugais', 'Russe',
    'Chinois', 'Japonais', 'Coréen', 'Arabe', 'Néerlandais', 'Suédois', 'Norvégien'
  ].sort();

  languageLevels = [
    { value: 1, label: 'Débutant' },
    { value: 2, label: 'Intermédiaire' },
    { value: 3, label: 'Avancé' },
    { value: 4, label: 'Courant' },
    { value: 5, label: 'Natif' }
  ];

  languageTypes = [
    { value: 'spoken', label: 'Parlé' },
    { value: 'reading', label: 'Lecture' },
    { value: 'both', label: 'Parlé & Lecture' }
  ];

  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  constructor() {
    this.jobForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      company: ['', Validators.required],
      expiresAt: [''],
      location: ['', Validators.required],
      remoteWork: ['no'],
      remoteDays: [''],
      salaryMin: [''],
      salaryMax: [''],
      salaryCurrency: ['EUR'],
      experienceMin: [''],
      experienceMax: [''],
      contractType: ['', Validators.required],
      educationLevel: [''],
      requiredSkills: ['', Validators.required],
      languages: [''],
      benefits: [''],
      applicationDeadline: [''],
      startDate: [''],
      contactEmail: ['', Validators.email],
      contactPhone: [''],
      previewVisibility: [JSON.stringify(this.previewVisibility)]
    });
  }

  ngOnInit(): void {
    this.jobId = this.route.snapshot.params['id'];
    this.isEdit = !!this.jobId;
    
    if (this.isEdit) {
      this.loadJobOffer();
    }
    
    // Écouter les changements des champs pour mettre à jour la description automatiquement
    this.setupAutoDescription();
  }

  loadJobOffer(): void {
    this.http.get<JobOfferResponse>(`http://localhost:8080/api/job-offers/${this.jobId}`)
      .subscribe({
        next: (job) => {
          this.currentJobOffer = job;
          this.jobForm.patchValue({
            ...job,
            remoteWork: job.remoteWork ? 'yes' : 'no',
            applicationDeadline: job.applicationDeadline ? 
              new Date(job.applicationDeadline).toISOString().slice(0, 10) : '',
            startDate: job.startDate ? 
              new Date(job.startDate).toISOString().slice(0, 10) : '',
            expiresAt: job.expiresAt ? 
              new Date(job.expiresAt).toISOString().slice(0, 10) : ''
          });
          
          if (job.previewVisibility) {
            this.previewVisibility = JSON.parse(job.previewVisibility);
          }
        },
        error: () => {
          this.error = 'Erreur lors du chargement de l\'offre';
        }
      });
  }

  onSubmit(): void {
    if (this.jobForm.valid) {
      this.loading = true;
      this.error = '';
      
      const jobData = this.prepareJobData();
      console.log('Données envoyées:', jobData);
      
      const request = this.isEdit ? 
        this.http.put(`http://localhost:8080/api/job-offers/${this.jobId}`, jobData) :
        this.http.post('http://localhost:8080/api/job-offers', jobData);
      
      request.subscribe({
        next: () => {
          this.router.navigate(['/job-offers']);
        },
        error: (error) => {
          console.error('Erreur détaillée:', error);
          this.error = 'Erreur lors de la sauvegarde: ' + (error.error?.message || error.message);
          this.loading = false;
        }
      });
    }
  }

  onSubmitAndPublish(): void {
    if (this.jobForm.valid) {
      this.loading = true;
      this.error = '';
      
      const jobData = this.prepareJobData();
      console.log('Données envoyées:', jobData);
      
      this.http.post<JobOfferResponse>('http://localhost:8080/api/job-offers', jobData)
        .subscribe({
          next: (job) => {
            this.http.patch(`http://localhost:8080/api/job-offers/${job.id}/publish`, {})
              .subscribe({
                next: () => {
                  this.router.navigate(['/job-offers']);
                },
                error: () => {
                  this.error = 'Offre créée mais erreur lors de la publication';
                  this.loading = false;
                }
              });
          },
          error: (error) => {
            console.error('Erreur détaillée:', error);
            this.error = 'Erreur lors de la création: ' + (error.error?.message || error.message);
            this.loading = false;
          }
        });
    }
  }

  private prepareJobData(): Record<string, unknown> {
    const formValue = this.jobForm.value;
    const salaryRange = this.buildSalaryRange(formValue);
    const experienceLevel = this.buildExperienceLevel(formValue);
    
    return {
      title: formValue.title,
      description: formValue.description,
      expiresAt: formValue.expiresAt ? new Date(formValue.expiresAt).toISOString() : null,
      company: formValue.company,
      location: formValue.location,
      remoteWork: formValue.remoteWork === 'yes',
      contractType: formValue.contractType,
      educationLevel: formValue.educationLevel || null,
      requiredSkills: formValue.requiredSkills,
      languages: formValue.languages || null,
      benefits: formValue.benefits || null,
      applicationDeadline: formValue.applicationDeadline ? 
        new Date(formValue.applicationDeadline).toISOString() : null,
      startDate: formValue.startDate ? 
        new Date(formValue.startDate).toISOString() : null,
      contactEmail: formValue.contactEmail || null,
      contactPhone: formValue.contactPhone || null,
      salaryMin: formValue.salaryMin ? parseInt(formValue.salaryMin) : null,
      salaryMax: formValue.salaryMax ? parseInt(formValue.salaryMax) : null,
      salaryCurrency: formValue.salaryCurrency || 'EUR',
      experienceMin: formValue.experienceMin ? parseInt(formValue.experienceMin) : null,
      experienceMax: formValue.experienceMax ? parseInt(formValue.experienceMax) : null,
      salaryRange: salaryRange,
      experienceLevel: experienceLevel,
      previewVisibility: formValue.previewVisibility
    };
  }

  private buildSalaryRange(formValue: Record<string, unknown>): string | null {
    if (formValue['salaryMin'] && formValue['salaryMax']) {
      const currency = formValue['salaryCurrency'] === 'USD' ? '$' : '€';
      return `${formValue['salaryMin']}${currency} - ${formValue['salaryMax']}${currency}`;
    } else if (formValue['salaryMin']) {
      const currency = formValue['salaryCurrency'] === 'USD' ? '$' : '€';
      return `À partir de ${formValue['salaryMin']}${currency}`;
    } else if (formValue['salaryMax']) {
      const currency = formValue['salaryCurrency'] === 'USD' ? '$' : '€';
      return `Jusqu'à ${formValue['salaryMax']}${currency}`;
    }
    return null;
  }

  private buildExperienceLevel(formValue: Record<string, unknown>): string | null {
    if (formValue['experienceMin'] && formValue['experienceMax']) {
      return `${formValue['experienceMin']} - ${formValue['experienceMax']} ans`;
    } else if (formValue['experienceMin']) {
      return `Minimum ${formValue['experienceMin']} ans`;
    } else if (formValue['experienceMax']) {
      return `Maximum ${formValue['experienceMax']} ans`;
    }
    return null;
  }

  goBack(): void {
    this.router.navigate(['/job-offers']);
  }

  toggleAccordion(section: string): void {
    this.activeAccordion = this.activeAccordion === section ? '' : section;
  }

  isAccordionOpen(section: string): boolean {
    return this.activeAccordion === section;
  }

  // Gestion des compétences
  onSkillToggle(skill: string, event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    if (checkbox.checked) {
      if (!this.selectedSkills.includes(skill)) {
        this.selectedSkills.push(skill);
      }
    } else {
      this.selectedSkills = this.selectedSkills.filter(s => s !== skill);
    }
    this.updateSkillsForm();
  }

  isSkillSelected(skill: string): boolean {
    return this.selectedSkills.includes(skill);
  }

  get filteredSkills(): string[] {
    if (!this.skillSearchTerm) {
      return this.availableSkills;
    }
    return this.availableSkills.filter(skill => 
      skill.toLowerCase().includes(this.skillSearchTerm.toLowerCase())
    );
  }

  get canAddNewSkill(): boolean {
    return this.skillSearchTerm.trim().length > 0 && 
           !this.availableSkills.some(skill => 
             skill.toLowerCase() === this.skillSearchTerm.toLowerCase()
           ) &&
           !this.selectedSkills.includes(this.skillSearchTerm.trim());
  }

  addNewSkill(): void {
    const newSkill = this.skillSearchTerm.trim();
    if (newSkill && !this.availableSkills.includes(newSkill) && !this.selectedSkills.includes(newSkill)) {
      this.availableSkills.push(newSkill);
      this.availableSkills.sort();
      this.selectedSkills.push(newSkill);
      this.updateSkillsForm();
      this.skillSearchTerm = '';
    }
  }

  removeSkill(skill: string): void {
    this.selectedSkills = this.selectedSkills.filter(s => s !== skill);
    this.updateSkillsForm();
  }

  private updateSkillsForm(): void {
    this.jobForm.patchValue({
      requiredSkills: this.selectedSkills.join(', ')
    });
  }

  // Gestion des langues
  get filteredLanguages(): string[] {
    if (!this.languageSearchTerm) {
      return this.availableLanguages;
    }
    return this.availableLanguages.filter(lang => 
      lang.toLowerCase().includes(this.languageSearchTerm.toLowerCase())
    );
  }

  get canAddNewLanguage(): boolean {
    return this.languageSearchTerm.trim().length > 0 && 
           !this.availableLanguages.some(lang => 
             lang.toLowerCase() === this.languageSearchTerm.toLowerCase()
           ) &&
           !this.selectedLanguages.some(sl => sl.language === this.languageSearchTerm.trim());
  }

  addLanguage(language: string, level: number): void {
    if (!this.selectedLanguages.some(sl => sl.language === language)) {
      this.selectedLanguages.push({ language, level, type: 'both' });
      this.updateLanguagesForm();
    }
  }

  addNewLanguage(): void {
    const newLanguage = this.languageSearchTerm.trim();
    if (newLanguage && !this.availableLanguages.includes(newLanguage)) {
      this.availableLanguages.push(newLanguage);
      this.availableLanguages.sort();
    }
    this.addLanguage(newLanguage, 1);
    this.languageSearchTerm = '';
  }

  removeLanguage(language: string): void {
    this.selectedLanguages = this.selectedLanguages.filter(sl => sl.language !== language);
    this.updateLanguagesForm();
  }

  updateLanguageLevel(language: string, level: number): void {
    const langIndex = this.selectedLanguages.findIndex(sl => sl.language === language);
    if (langIndex !== -1) {
      this.selectedLanguages[langIndex].level = level;
      this.updateLanguagesForm();
    }
  }

  updateLanguageType(language: string, type: string): void {
    const langIndex = this.selectedLanguages.findIndex(sl => sl.language === language);
    if (langIndex !== -1) {
      this.selectedLanguages[langIndex].type = type;
      this.updateLanguagesForm();
    }
  }

  private updateLanguagesForm(): void {
    const languagesString = this.selectedLanguages
      .map(sl => {
        const typeLabel = this.languageTypes.find(t => t.value === sl.type)?.label || 'Les 2';
        return `${sl.language} (${this.getLanguageLevelLabel(sl.level)} - ${typeLabel})`;
      })
      .join(', ');
    this.jobForm.patchValue({
      languages: languagesString
    });
  }

  getLanguageLevelLabel(level: number): string {
    const levelObj = this.languageLevels.find(l => l.value === level);
    return levelObj ? levelObj.label : 'Débutant';
  }

  getSelectedLanguageLevel(language: string): number {
    const selectedLang = this.selectedLanguages.find(sl => sl.language === language);
    return selectedLang ? selectedLang.level : 0;
  }

  getRemoteWorkLabel(): string {
    const remoteWork = this.jobForm.get('remoteWork')?.value;
    const remoteDays = this.jobForm.get('remoteDays')?.value;
    
    switch (remoteWork) {
      case 'yes': return 'Télétravail complet';
      case 'hybrid': return `Hybride (${remoteDays || 0} jours/semaine)`;
      default: return '';
    }
  }

  isRemoteWorkVisible(): boolean {
    const remoteWork = this.jobForm.get('remoteWork')?.value;
    return remoteWork === 'yes' || remoteWork === 'hybrid';
  }

  // Gestion des certifications
  selectedCertifications: string[] = [];
  certificationSearchTerm = '';

  // Gestion de la visibilité dans l'aperçu
  previewVisibility = {
    salary: true,
    experience: true,
    description: true,
    skills: true,
    languages: true,
    certifications: true,
    benefits: true
  };

  get canAddNewCertification(): boolean {
    return this.certificationSearchTerm.trim().length > 0 && 
           !this.selectedCertifications.includes(this.certificationSearchTerm.trim());
  }

  addNewCertification(): void {
    const newCert = this.certificationSearchTerm.trim();
    if (newCert && !this.selectedCertifications.includes(newCert)) {
      this.selectedCertifications.push(newCert);
      this.updateCertificationsForm();
      this.certificationSearchTerm = '';
    }
  }

  removeCertification(certification: string): void {
    this.selectedCertifications = this.selectedCertifications.filter(c => c !== certification);
    this.updateCertificationsForm();
  }

  private updateCertificationsForm(): void {
    // Les certifications sont maintenant gérées séparément
  }

  onLanguageSelect(language: string, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const level = parseInt(select.value);
    if (level > 0) {
      const existingLang = this.selectedLanguages.find(sl => sl.language === language);
      if (existingLang) {
        // Mettre à jour le niveau de la langue existante
        this.updateLanguageLevel(language, level);
      } else {
        // Ajouter une nouvelle langue
        this.addLanguage(language, level);
      }
    }
  }

  onLanguageLevelChange(language: string, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const level = parseInt(select.value);
    this.updateLanguageLevel(language, level);
  }

  onLanguageTypeChange(language: string, event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.updateLanguageType(language, select.value);
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

  togglePreviewVisibility(field: keyof typeof this.previewVisibility): void {
    this.previewVisibility[field] = !this.previewVisibility[field];
    this.jobForm.patchValue({
      previewVisibility: JSON.stringify(this.previewVisibility)
    });
  }

  private setupAutoDescription(): void {
    // Méthode vide - la génération automatique se fait maintenant via le bouton
  }

  fillAutoDescription(): void {
    const autoDescription = this.generateAutoDescription();
    if (autoDescription) {
      this.jobForm.patchValue({ description: autoDescription });
    }
  }

  private generateAutoDescription(): string {
    const formValue = this.jobForm.value;
    let description = '';
    
    // Titre et entreprise
    if (formValue.title && formValue.company) {
      description += `Nous recherchons un(e) ${formValue.title} pour rejoindre l'équipe de ${formValue.company}.\n\n`;
    }
    
    // Localisation et télétravail
    if (formValue.location) {
      description += `📍 Localisation : ${formValue.location}`;
      if (this.isRemoteWorkVisible()) {
        description += ` - ${this.getRemoteWorkLabel()}`;
      }
      description += '\n\n';
    }
    
    // Type de contrat
    if (formValue.contractType) {
      description += `💼 Type de contrat : ${this.getContractTypeLabel(formValue.contractType)}\n\n`;
    }
    
    // Expérience requise
    if (formValue.experienceMin || formValue.experienceMax) {
      description += '🎯 Expérience requise : ';
      if (formValue.experienceMin && formValue.experienceMax) {
        description += `${formValue.experienceMin} à ${formValue.experienceMax} ans`;
      } else if (formValue.experienceMin) {
        description += `Minimum ${formValue.experienceMin} ans`;
      } else if (formValue.experienceMax) {
        description += `Maximum ${formValue.experienceMax} ans`;
      }
      description += '\n\n';
    }
    
    // Niveau d'éducation
    if (formValue.educationLevel) {
      description += `🎓 Niveau d'études : ${formValue.educationLevel}\n\n`;
    }
    
    // Compétences requises
    if (this.selectedSkills.length > 0) {
      description += '🔧 Compétences requises :\n';
      this.selectedSkills.forEach(skill => {
        description += `• ${skill}\n`;
      });
      description += '\n';
    }
    
    // Langues
    if (this.selectedLanguages.length > 0) {
      description += '🌍 Langues :\n';
      this.selectedLanguages.forEach(lang => {
        const typeLabel = this.languageTypes.find(t => t.value === lang.type)?.label || 'Les 2';
        description += `• ${lang.language} - ${this.getLanguageLevelLabel(lang.level)} (${typeLabel})\n`;
      });
      description += '\n';
    }
    
    // Certifications
    if (this.selectedCertifications.length > 0) {
      description += '🏆 Certifications appréciées :\n';
      this.selectedCertifications.forEach(cert => {
        description += `• ${cert}\n`;
      });
      description += '\n';
    }
    
    // Avantages
    if (formValue.benefits) {
      description += `✨ Avantages :\n${formValue.benefits}\n\n`;
    }
    
    return description.trim();
  }

  publishCurrentOffer(): void {
    if (!this.jobId) return;
    
    this.loading = true;
    this.http.patch(`http://localhost:8080/api/job-offers/${this.jobId}/publish`, {})
      .subscribe({
        next: (updatedOffer: any) => {
          this.currentJobOffer = updatedOffer;
          this.loading = false;
          const applicationUrl = `http://localhost:4200/apply${updatedOffer.applicationUrl || '/' + this.jobId}`;
          const message = `Offre publiée avec succès !\n\nLien d'accès au formulaire :\n${applicationUrl}\n\nVoulez-vous copier le lien ?`;
          
          if (confirm(message)) {
            this.copyToClipboard(applicationUrl);
          }
        },
        error: (err) => {
          console.error('Erreur lors de la publication', err);
          this.error = 'Erreur lors de la publication de l\'offre';
          this.loading = false;
        }
      });
  }

  closeCurrentOffer(): void {
    if (!this.jobId || !this.currentJobOffer) return;
    
    const message = `Êtes-vous sûr de vouloir clôturer l'offre "${this.currentJobOffer.title}" ?\n\nCette action est irréversible et empêchera de nouvelles candidatures.`;
    
    if (confirm(message)) {
      this.loading = true;
      this.http.patch(`http://localhost:8080/api/job-offers/${this.jobId}/close`, {})
        .subscribe({
          next: (updatedOffer: any) => {
            this.currentJobOffer = updatedOffer;
            this.loading = false;
            alert('Offre clôturée avec succès');
          },
          error: (err) => {
            console.error('Erreur lors de la clôture', err);
            this.error = 'Erreur lors de la clôture de l\'offre';
            this.loading = false;
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
}