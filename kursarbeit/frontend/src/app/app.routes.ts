import {Routes} from '@angular/router';
import {HomePageComponent} from './pages/home-page/home-page.component';
import {ProfilePageComponent} from './pages/profile-page/profile-page.component';
import {EventsPageComponent} from './pages/events/events-page/events-page.component';
import {EventDetailPageComponent} from './pages/events/event-detail-page/event-detail-page.component';
import {CreateEventComponent} from './pages/events/create-event/create-event.component';
import {ManageEventsComponent} from './pages/events/manage-events/manage-events.component';
import {EditEventComponent} from './pages/events/edit-event/edit-event.component';
import {MyEventsComponent} from './pages/events/my-events/my-events.component';
import {AdminComponent} from './pages/admin/admin.component';
import {InfoCenterComponent} from './pages/info-center/info-center.component';
import {authGuard} from './guards/auth.guard';
import {roleGuard} from './guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    component: HomePageComponent
  },
  {
    path: 'info-center',
    component: InfoCenterComponent
  },
  {
    path: 'events',
    component: EventsPageComponent
  },

  {
    path: 'events/create',
    component: CreateEventComponent,
    canActivate: [roleGuard(['ORGANIZER', 'ADMIN'])]
  },
  {
    path: 'events/manage',
    component: ManageEventsComponent,
    canActivate: [roleGuard(['ORGANIZER', 'ADMIN'])]
  },
  {
    path: 'events/edit/:id',
    component: EditEventComponent,
    canActivate: [roleGuard(['ORGANIZER', 'ADMIN'])]
  },

  {
    path: 'events/:id',
    component: EventDetailPageComponent
  },

  {
    path: 'profile',
    component: ProfilePageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'my-events',
    component: MyEventsComponent,
    canActivate: [authGuard]
  },

  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [roleGuard(['ADMIN'])]
  },

  {
    path: '**',
    redirectTo: ''
  }
];
