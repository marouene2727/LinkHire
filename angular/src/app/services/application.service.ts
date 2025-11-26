import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Application {
  id: number;
  candidate: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    currentPosition?: string;
  };
  jobOffer: {
    id: number;
    title: string;
    company: string;
  };
  emailSubject: string;
  receivedAt: string;
  status: 'PENDING' | 'VALIDATED' | 'AMBIGUOUS' | 'REJECTED';
  aiScore?: number;
  aiAnalysis?: string;
  responseSent: boolean;
}

export interface DashboardStats {
  totalApplications: number;
  totalCandidates: number;
  totalJobOffers: number;
  activeJobOffers: number;
  validatedApplications: number;
  ambiguousApplications: number;
  rejectedApplications: number;
  pendingApplications: number;
  todayApplications: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications`);
  }

  getApplicationsByStatus(status: string): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications/status/${status}`);
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`);
  }

  getRecentApplications(limit: number = 10): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/dashboard/recent-applications?limit=${limit}`);
  }

  updateApplicationStatus(id: number, status: string): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${id}/status`, { status });
  }

  addRecruiterNotes(id: number, notes: string): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${id}/notes`, { notes });
  }

  getUnreadNotifications(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/applications/notifications/unread`);
  }

  markAllNotificationsAsRead(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/applications/notifications/mark-all-read`, {});
  }

  markJobOfferNotificationsAsRead(jobOfferId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/applications/notifications/mark-job-offer-read/${jobOfferId}`, {});
  }
}