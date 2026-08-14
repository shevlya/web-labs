export interface AdminStatistics {
  totalUsers: number;
  totalOrganizers: number;
  pendingEvents: number;
  pendingOrganizerRequests: number;
}

export interface AdminEvent {
  idEvent: number;
  eventName: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  eventFormat: 'ONLINE' | 'OFFLINE' | 'HYBRID';
  eventStatus: 'PLANNED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  eventCategoryName: string;
  idEventCategory: number;
  placeName: string | null;
  price: number;
  imageUrl: string | null;
  verified: boolean;
  moderationStatus: 'PUBLISHED' | 'PENDING' | 'REJECTED';
  organizerFio: string;
  eventDescription?: string | null;
  maxParticipants?: number | null;
  idPlace?: number | null;
  draftChanges?: Record<string, any>;
}

export interface DraftField {
  key: string;
  currentValue: any;
  newValue: any;
  label: string;
  selected: boolean;
}

export interface OrganizerRequestShort {
  idOrganizerRequest: number;
  userId: number;
  userFio: string;
  userEmail: string;
  requestStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  requestText: string;
  submittedAt: string;
}

export interface UserResponse {
  idUser: number;
  fio: string;
  email: string;
  roleName: string;
  userStatus: 'ACTIVE' | 'BLOCKED' | 'EXPECTED';
  birthDate: string | null;
  hasDisability: boolean;
}

export interface CatalogItem {
  id: number;
  name: string;
  description?: string;
  colorCode?: string;
}

export interface ConfirmConfig {
  visible: boolean;
  title: string;
  message: string;
  confirmLabel: string;
  type: 'approve' | 'reject' | 'delete';
  comment?: string;
  showComment: boolean;
  onConfirm: () => void;
}

export type UserStatusType = 'ACTIVE' | 'BLOCKED' | 'EXPECTED';

export const USER_STATUSES: UserStatusType[] = ['ACTIVE', 'BLOCKED', 'EXPECTED'];
export const USER_STATUS_LABELS: Record<UserStatusType, string> = {
  ACTIVE: 'Активен',
  BLOCKED: 'Заблокирован',
  EXPECTED: 'Ожидает'
};

export type RoleNameType = 'ADMIN' | 'ORGANIZER' | 'USER';

export const ROLE_LABELS: Record<RoleNameType, string> = {
  ADMIN: 'Администратор',
  ORGANIZER: 'Организатор',
  USER: 'Пользователь'
};
