import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TaskCardComponent} from '../task-card/task-card.component';
import {Task} from '../../models/tasks';
import {TaskService} from '../../services/task.service';
import {AuthService} from '../../services/auth.service';
import {ERROR_MESSAGES} from '../../constants/errors';
import {HttpErrorResponse} from '@angular/common/http';
import {APP_CONSTANTS} from '../../constants/app';
import {DialogFrameComponent} from "../../shared/dialog-frame/dialog-frame.component";

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TaskCardComponent, DialogFrameComponent],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.scss'
})
export class TaskListComponent {
  tasks: Task[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  showDeleteConfirm = false;
  taskToDeleteId: number | null = null;

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
    this.taskToDeleteId = id;
    this.showDeleteConfirm = true;
  }


  confirmDelete(): void {
    if (this.taskToDeleteId) {
      this.taskService.deleteTask(this.taskToDeleteId).subscribe({
        next: () => {
          this.tasks = this.tasks.filter(t => t.id !== this.taskToDeleteId);
          this.setSuccess('Задача успешно удалена');
          setTimeout(() => this.successMessage = '', APP_CONSTANTS.TIMEOUT.SUCCESS);
          this.showDeleteConfirm = false;
          this.taskToDeleteId = null;
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 403) {
            this.setError(ERROR_MESSAGES.TASK.DELETE_FORBIDDEN);
          } else if (err.status === 404) {
            this.setError(ERROR_MESSAGES.TASK.NOT_FOUND);
          } else {
            const msg = err.error?.message ?? ERROR_MESSAGES.TASK.DELETE_FAILED;
            this.setError(msg);
          }
          this.showDeleteConfirm = false;
          this.taskToDeleteId = null;
        }
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.taskToDeleteId = null;
  }

  logout(): void {
    this.authService.logout();
  }

  private setLoading(loading: boolean): void {
    this.isLoading = loading;
    if (loading) {
      this.errorMessage = '';
      this.successMessage = '';
    }
  }

  private setError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    this.isLoading = false;
  }

  private setSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    this.isLoading = false;
  }
}
