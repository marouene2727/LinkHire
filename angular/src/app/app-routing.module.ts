// Angular Import
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

// project import
import { AdminComponent } from './theme/layout/admin/admin.component';
import { GuestComponent } from './theme/layout/guest/guest.component';

const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then((c) => c.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/register/register.component').then((c) => c.RegisterComponent)
  },
  {
    path: 'verify-email',
    loadComponent: () => import('./components/verify-email/verify-email.component').then((c) => c.VerifyEmailComponent)
  },

  {
    path: '',
    component: AdminComponent,
    children: [
      {
        path: '',
        redirectTo: '/analytics',
        pathMatch: 'full'
      },
      {
        path: 'analytics',
        loadComponent: () => import('./demo/dashboard/dash-analytics.component').then((c) => c.DashAnalyticsComponent)
      },
      {
        path: 'component',
        loadChildren: () => import('./demo/ui-element/ui-basic.module').then((m) => m.UiBasicModule)
      },
      {
        path: 'chart',
        loadComponent: () => import('./demo/chart-maps/core-apex.component').then((c) => c.CoreApexComponent)
      },
      {
        path: 'forms',
        loadComponent: () => import('./demo/forms/form-elements/form-elements.component').then((c) => c.FormElementsComponent)
      },
      {
        path: 'tables',
        loadComponent: () => import('./demo/tables/tbl-bootstrap/tbl-bootstrap.component').then((c) => c.TblBootstrapComponent)
      },
      {
        path: 'sample-page',
        loadComponent: () => import('./demo/other/sample-page/sample-page.component').then((c) => c.SamplePageComponent)
      },
      {
        path: 'job-offers',
        loadComponent: () => import('./components/job-offers/job-offers.component').then((c) => c.JobOffersComponent)
      },
      {
        path: 'job-offers/create',
        loadComponent: () => import('./components/job-offers/job-offer-form.component').then((c) => c.JobOfferFormComponent)
      },
      {
        path: 'job-offers/:id/edit',
        loadComponent: () => import('./components/job-offers/job-offer-form.component').then((c) => c.JobOfferFormComponent)
      },
      {
        path: 'job-offers/:id',
        loadComponent: () => import('./components/job-offers/job-offer-detail.component').then((c) => c.JobOfferDetailComponent)
      },
      {
        path: 'applications',
        loadComponent: () => import('./components/applications/applications-list.component').then((c) => c.ApplicationsListComponent)
      },
      {
        path: 'applications/validated',
        loadComponent: () => import('./components/applications/applications-list.component').then((c) => c.ApplicationsListComponent)
      },
      {
        path: 'applications/ambiguous',
        loadComponent: () => import('./components/applications/applications-list.component').then((c) => c.ApplicationsListComponent)
      },
      {
        path: 'applications/rejected',
        loadComponent: () => import('./components/applications/applications-list.component').then((c) => c.ApplicationsListComponent)
      },
      {
        path: 'applications/archived',
        loadComponent: () => import('./components/applications/applications-list.component').then((c) => c.ApplicationsListComponent)
      },
      {
        path: 'applications/:id',
        loadComponent: () => import('./components/applications/application-detail.component').then((c) => c.ApplicationDetailComponent)
      }
    ]
  },
  {
    path: '',
    component: GuestComponent,
    children: [
      {
        path: 'apply/:id',
        loadComponent: () => import('./components/candidate-application/candidate-application.component').then((c) => c.CandidateApplicationComponent)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
