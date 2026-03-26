import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {TaskStatus, TaskStatusLabels} from '../../models/tasks';
import {TaskService} from '../../services/task.service';
import { APP_CONSTANTS } from '../../constants/app';
import { ERROR_MESSAGES } from '../../constants/errors';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-edit-task',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-task.component.html',
  styleUrl: './edit-task.component.scss'
})
export class EditTaskComponent {
  form: FormGroup;
  isEditMode = false;
  taskId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';

  readonly taskStatuses = Object.values(TaskStatus);
  readonly TaskStatusLabels = TaskStatusLabels;
  
  readonly titleMaxLength = APP_CONSTANTS.VALIDATION.TITLE_MAX_LENGTH;

  get titleControl() {
    return this.form.get('title');
  }

  get statusControl() {
    return this.form.get('status');
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(this.titleMaxLength)]],
      status: [TaskStatus.OPEN, [Validators.required]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('taskId');
    if (id) {
      this.isEditMode = true;
      this.taskId = +id;
      this.loadTask();
    }
  }

  loadTask(): void {
    if (!this.taskId) return;
    this.isLoading = true;
    this.taskService.getTask(this.taskId).subscribe({
      next: task => {
        this.form.patchValue({title: task.title, status: task.status});
        this.setLoading(false);
      },
      error: () => this.setError(ERROR_MESSAGES.TASK.LOAD_FAILED)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.clearError();
    const payload = this.form.value;
    const required$ = (this.isEditMode && this.taskId) ? this.taskService.updateTask(this.taskId, payload) : this.taskService.createTask(payload);

    required$.subscribe({
      next: () => {
        this.isSaving = false;
        this.router.navigate(['/tasks']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving = false;
        if (err.status === 400 && err.error?.message?.includes('лимит')) {
          this.setError(ERROR_MESSAGES.TASK.ACTIVE_LIMIT_EXCEEDED);
        } else {
          this.setError(err.error?.message ?? ERROR_MESSAGES.TASK.SAVE_FAILED);
        }
      }
    });
  }

  private setLoading(loading: boolean): void {
    this.isLoading = loading;
    if (loading) this.clearError();
  }

  private setError(message: string): void {
    this.errorMessage = message;
    this.isLoading = false;
  }

  private clearError(): void {
    this.errorMessage = '';
  }
}
