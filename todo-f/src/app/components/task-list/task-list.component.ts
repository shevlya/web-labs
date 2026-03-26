import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TaskCardComponent} from '../task-card/task-card.component';
import {Task} from '../../models/tasks';
import {TaskService} from '../../services/task.service';
import {AuthService} from '../../services/auth.service';

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
      this.errorMessage = 'Не удалось определить пользователя';
      this.isLoading = false;
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.taskService.getTasks(userId).subscribe({
      next: tasks => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Ошибка загрузки задач';
        this.isLoading = false;
      }
    });
  }

  onDeleteTask(id: number): void {
    if (!confirm('Вы уверины, что хотите удалить эту задачу?')) {
      return;
    }
    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(t => t.id !== id);
      },
      error: (err) => {
        const msg = err.error?.message ?? 'Не удалось удалить задачу';
        alert(msg);
      },
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
