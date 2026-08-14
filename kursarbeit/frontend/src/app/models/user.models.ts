export interface User {
  userId: number;
  fio: string;
  email: string;
  roleName: string;
  avatarUrl?: string | null;
  avatarId?: number | null;
  hasDisability?: boolean;
  birthDate?: string | null;
}

export interface CategoryDto {
  idEventCategory: number;
  eventCategoryName: string;
  colorCode: string;
}

export interface UserInterestDto {
  id: number;
  name: string;
  colorCode: string;
}

export interface Avatar {
  idAvatar: number;
  avatarUrl: string;
}

export interface OrganizerRequest {
  idOrganizerRequest: number;
  requestText: string;
  requestStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  reviewComment: string | null;
}
