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