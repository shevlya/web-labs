import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {TaskStatus, TaskStatusLabels} from '../../models/tasks';
import {TaskService} from '../../services/task.service';

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
      title: ['', [Validators.required, Validators.maxLength(500)]],
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
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Не удалось загрузить задачу';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const payload = this.form.value;
    const required$ = (this.isEditMode && this.taskId) ? this.taskService.updateTask(this.taskId, payload) : this.taskService.createTask(payload);

    required$.subscribe({
      next: () => {
        this.isSaving = false;
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        this.isSaving = false;
        this.errorMessage = err.error?.message ?? 'Ошибка сохранения';
      }
    });
  }

}
