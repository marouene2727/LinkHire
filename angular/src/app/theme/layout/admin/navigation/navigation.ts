export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  badge?: {
    title?: string;
    type?: string;
  };
  children?: NavigationItem[];
}

export const NavigationItems: NavigationItem[] = [
  {
    id: 'main',
    title: 'Principal',
    type: 'group',
    icon: 'icon-group',
    children: [
      {
        id: 'dashboard',
        title: 'Tableau de bord',
        type: 'item',
        url: '/analytics',
        icon: 'feather icon-home'
      }
    ]
  },
  {
    id: 'recruitment',
    title: 'Recrutement',
    type: 'group',
    icon: 'icon-group',
    children: [
      {
        id: 'applications',
        title: 'Candidatures',
        type: 'collapse',
        icon: 'feather icon-users',
        children: [
          {
            id: 'all-applications',
            title: 'Toutes les candidatures',
            type: 'item',
            url: '/applications'
          },
          {
            id: 'validated',
            title: 'Validées',
            type: 'item',
            url: '/applications/validated'
          },
          {
            id: 'ambiguous',
            title: 'Ambiguës',
            type: 'item',
            url: '/applications/ambiguous'
          },
          {
            id: 'rejected',
            title: 'Rejetées',
            type: 'item',
            url: '/applications/rejected'
          },
          {
            id: 'archived',
            title: 'Archivées',
            type: 'item',
            url: '/applications/archived',
            icon: 'feather icon-lock'
          }
        ]
      },
      {
        id: 'job-offers',
        title: 'Offres d\'emploi',
        type: 'item',
        url: '/job-offers',
        icon: 'feather icon-briefcase'
      },
      {
        id: 'candidates',
        title: 'Candidats (Bientôt)',
        type: 'item',
        url: '/candidates',
        icon: 'feather icon-user'
      }
    ]
  },
  {
    id: 'communication',
    title: 'Communication',
    type: 'group',
    icon: 'icon-group',
    children: [
      {
        id: 'email-templates',
        title: 'Templates d\'emails (bientôt)',
        type: 'item',
        url: '/email-templates',
        icon: 'feather icon-mail'
      }
    ]
  }
];
