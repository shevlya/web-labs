import {CommonModule, DatePipe, LowerCasePipe} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RouterLink} from '@angular/router';
import {Task, TaskStatus, TaskStatusLabels} from '../../models/tasks';

@Component({
  selector: 'app-task-card',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, LowerCasePipe],
  templateUrl: './task-card.component.html',
  styleUrl: './task-card.component.scss'
})
export class TaskCardComponent {
  @Input({required: true}) task!: Task;
  @Output() deleteTask = new EventEmitter<number>();

  readonly TaskStatusLabels = TaskStatusLabels;
  readonly TaskStatus = TaskStatus;

  onDelete(): void {
    if (this.task.id != null) {
      this.deleteTask.emit(this.task.id);
    }
  }
}
