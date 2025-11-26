// angular import
import { Component, inject } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

// bootstrap import
import { NgbDropdownConfig, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';

// project import
import { SharedModule } from 'src/app/theme/shared/shared.module';
import { ChatUserListComponent } from './chat-user-list/chat-user-list.component';
import { ChatMsgComponent } from './chat-msg/chat-msg.component';
import { AuthService, User } from 'src/app/services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { ApplicationService } from 'src/app/services/application.service';
import { OnInit, OnDestroy } from '@angular/core';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-nav-right',
  imports: [SharedModule, ChatUserListComponent, ChatMsgComponent, NgbDropdownModule, RouterModule],
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss'],
  providers: [NgbDropdownConfig],
  animations: [
    trigger('slideInOutLeft', [
      transition(':enter', [style({ transform: 'translateX(100%)' }), animate('300ms ease-in', style({ transform: 'translateX(0%)' }))]),
      transition(':leave', [animate('300ms ease-in', style({ transform: 'translateX(100%)' }))])
    ]),
    trigger('slideInOutRight', [
      transition(':enter', [style({ transform: 'translateX(-100%)' }), animate('300ms ease-in', style({ transform: 'translateX(0%)' }))]),
      transition(':leave', [animate('300ms ease-in', style({ transform: 'translateX(-100%)' }))])
    ])
  ]
})
export class NavRightComponent implements OnInit, OnDestroy {
  // public props
  visibleUserList: boolean;
  chatMessage: boolean;
  friendId!: number;
  currentUser: User | null = null;
  notifications: any[] = [];
  totalUnreadCount = 0;
  private notificationSubscription?: Subscription;

  private authService = inject(AuthService);
  private router = inject(Router);
  private applicationService = inject(ApplicationService);

  // constructor
  constructor() {
    this.visibleUserList = false;
    this.chatMessage = false;
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit() {
    this.loadNotifications();
    // Actualiser les notifications toutes les 30 secondes
    this.notificationSubscription = interval(30000).subscribe(() => {
      this.loadNotifications();
    });
  }

  ngOnDestroy() {
    if (this.notificationSubscription) {
      this.notificationSubscription.unsubscribe();
    }
  }

  get displayName(): string {
    if (this.currentUser) {
      return `${this.currentUser.firstName} ${this.currentUser.lastName}`;
    }
    return 'John Doe';
  }

  // public method
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  loadNotifications() {
    console.log('Chargement des notifications...');
    this.applicationService.getUnreadNotifications().subscribe({
      next: (notifications) => {
        console.log('Notifications reçues:', notifications);
        this.notifications = notifications;
        this.totalUnreadCount = notifications.reduce((sum: number, notif: any) => sum + notif.unreadCount, 0);
        console.log('Total non lues:', this.totalUnreadCount);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des notifications', err);
      }
    });
  }

  getTimeAgo(date: string): string {
    const now = new Date();
    const notifDate = new Date(date);
    const diffMs = now.getTime() - notifDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `${diffMins} min`;
    if (diffHours < 24) return `${diffHours}h`;
    return `${diffDays}j`;
  }

  markAllAsRead() {
    this.applicationService.markAllNotificationsAsRead().subscribe({
      next: () => {
        this.loadNotifications();
      },
      error: (err) => {
        console.error('Erreur lors du marquage des notifications', err);
      }
    });
  }

  getNotificationLink(notification: any): string[] {
    if (notification.unreadCount === 1) {
      // S'il n'y a qu'une candidature, aller directement à la candidature
      return ['/applications', notification.singleApplicationId];
    } else {
      // S'il y en a plusieurs, aller à l'offre d'emploi avec scroll vers les candidatures
      return ['/job-offers', notification.jobOfferId];
    }
  }

  markJobOfferAsRead(jobOfferId: number) {
    this.applicationService.markJobOfferNotificationsAsRead(jobOfferId).subscribe({
      next: () => {
        // Recharger les notifications après marquage
        setTimeout(() => this.loadNotifications(), 100);
      },
      error: (err) => {
        console.error('Erreur lors du marquage des notifications de l\'offre', err);
      }
    });
  }

  // eslint-disable-next-line
  onChatToggle(friendID: any) {
    this.friendId = friendID;
    this.chatMessage = !this.chatMessage;
  }
}
