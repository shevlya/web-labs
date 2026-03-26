export interface Task {
  id?: number;
  title: string;
  status: TaskStatus;
  createdBy?: number;
  createdAt?: string;
}

export enum TaskStatus {
  OPEN = 'OPEN',
  DONE = 'DONE',
  IN_PROGRESS = 'IN_PROGRESS',
  CLOSED = 'CLOSED'
}

export const TaskStatusLabels: Record<TaskStatus, string> = {
  [TaskStatus.OPEN]: 'Открыта',
  [TaskStatus.DONE]: 'Сделана',
  [TaskStatus.IN_PROGRESS]: 'В процессе выполнения',
  [TaskStatus.CLOSED]: 'Закрыта'
}
