export interface Category {
  idEventCategory: number;
  eventCategoryName: string;
  eventCategoryDescription?: string;
  colorCode?: string;
}

export interface EventCard {
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
  placeType?: 'PHYSICAL' | 'ONLINE' | null;
  price: number;
  imageUrl: string | null;
  verified: boolean;
  moderationStatus?: 'PUBLISHED' | 'PENDING' | 'REJECTED';
  maxParticipants?: number | null;
  categoryColor?: string;
  isOnline?: boolean;
}

export interface EventDetail {
  idEvent: number;
  idOrganizer: number;
  organizerFio: string;
  idAdmin: number | null;
  adminFio: string | null;
  eventFormat: 'ONLINE' | 'OFFLINE' | 'HYBRID';
  eventStatus: 'PLANNED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  idPlace: number | null;
  placeName: string | null;
  placeType: 'PHYSICAL' | 'ONLINE' | null;
  idEventCategory: number;
  eventCategoryName: string;
  eventName: string;
  eventDescription: string | null;
  eventDate: string;
  startTime: string;
  endTime: string;
  maxParticipants: number | null;
  imageUrl: string | null;
  price: number;
  verified: boolean;
  verificationComment: string | null;
  draftChanges: any;
  moderationStatus: 'PUBLISHED' | 'PENDING' | 'REJECTED';
  categoryColor?: string;
}

export interface EventCreateDto {
  idOrganizer: number;
  eventFormat: 'ONLINE' | 'OFFLINE' | 'HYBRID';
  eventStatus: 'PLANNED';
  idEventCategory: number;
  idPlace: number | null;
  eventName: string;
  eventDescription?: string | null;
  eventDate: string;
  startTime: string;
  endTime: string;
  maxParticipants?: number | null;
  imageUrl?: string | null;
  price: number;
  verified?: boolean;
  verificationComment?: string | null;
}

export interface EventSubmitChangesDto {
  eventFormat?: 'ONLINE' | 'OFFLINE' | 'HYBRID';
  idEventCategory?: number;
  idPlace?: number | null;
  eventName?: string;
  eventDescription?: string;
  eventDate?: string;
  startTime?: string;
  endTime?: string;
  maxParticipants?: number | null;
  imageUrl?: string | null;
  price?: number;
}

export interface EventParticipant {
  userId: number;
  userFio: string;
  userEmail: string;
  eventId: number;
  eventName: string;
  participationStatus: 'REGISTERED' | 'ATTENDED' | 'CANCELLED' | 'WAITLISTED' | 'REJECTED_BY_ORGANIZER';
  registrationDate: string;
}

export interface Place {
  idPlace: number;
  placeName: string;
  placeDescription: string | null;
  type: 'PHYSICAL' | 'ONLINE';
}

export interface PhysicalPlaceCreateDto {
  placeName: string;
  placeDescription?: string | null;
  address: string;
  disabilityAccessible: boolean;
}

export interface OnlinePlaceCreateDto {
  placeName: string;
  placeDescription?: string | null;
  meetingUrl: string;
  specialNotes?: string | null;
  recording?: boolean;
}
