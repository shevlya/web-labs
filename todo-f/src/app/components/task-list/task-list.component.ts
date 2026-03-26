import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TaskCardComponent} from '../task-card/task-card.component';
import {Task} from '../../models/tasks';
import {TaskService} from '../../services/task.service';
import {AuthService} from '../../services/auth.service';
import { ERROR_MESSAGES } from '../../constants/errors';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TaskCardComponent],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.scss'
})
export class TaskListComponent {
  tasks: Task[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private taskService: TaskService,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    const userId = this.authService.getUserId();
    if (userId == null) {
      this.setError(ERROR_MESSAGES.AUTH.USER_NOT_FOUND);
      return;
    }
    this.setLoading(true);
    this.taskService.getTasks(userId).subscribe({
      next: tasks => {
        this.tasks = tasks;
        this.setLoading(false);
      },
      error: () => this.setError(ERROR_MESSAGES.TASK.LIST_LOAD_FAILED)
    });
  }

  onDeleteTask(id: number): void {
    if (!confirm('Вы уверены, что хотите удалить эту задачу?')) {
      return;
    }
    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(t => t.id !== id);
      },
      error: (err) => {
        const msg = err.error?.message ?? ERROR_MESSAGES.TASK.DELETE_FAILED;
        alert(msg);
      },
    });
  }

  logout(): void {
    this.authService.logout();
  }

  private setLoading(loading: boolean): void {
    this.isLoading = loading;
    if (loading) this.errorMessage = '';
  }

  private setError(message: string): void {
    this.errorMessage = message;
    this.isLoading = false;
  }
}
