import {Routes} from '@angular/router';
import {LoginComponent} from './components/login/login.component';
import {TaskListComponent} from './components/task-list/task-list.component';
import {EditTaskComponent} from './components/edit-task/edit-task.component';
import {authGuard} from './guards/auth.guard';

export const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {path: 'tasks', component: TaskListComponent, canActivate: [authGuard]},
  {path: 'tasks/new', component: EditTaskComponent, canActivate: [authGuard]},
  {path: 'tasks/:taskId', component: EditTaskComponent, canActivate: [authGuard]},
  {path: '', redirectTo: '/tasks', pathMatch: 'full'},
  {path: '**', redirectTo: '/tasks'}
];
