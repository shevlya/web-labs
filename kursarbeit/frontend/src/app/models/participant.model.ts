export interface Participant {
  userId: number;
  userFio: string;
  userEmail?: string;
  eventId: number;
  eventName: string;
  participationStatus: string;
  registrationDate: string;
}

export type StatusOption = {
  id: string;
  name: string;
};
