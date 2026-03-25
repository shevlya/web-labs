import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { TaskListComponent } from './components/task-list/task-list.component';
import { EditTaskComponent } from './components/edit-task/edit-task.component';

export const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {path: 'tasks', component: TaskListComponent},
    {path: 'task/new', component: EditTaskComponent},
    {path: 'tasks/:taskId', component: EditTaskComponent},
    {path: '', redirectTo: '/tasks', pathMatch: 'full'},
    {path: '**', redirectTo: '/tasks'}
];
