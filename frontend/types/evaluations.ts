export interface EvaluationResponseDTO {
  id: number;
  registrationId: string;
  eventId: string;
  eventTitle: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface OrganizerEventFeedbackResponseDTO {
  eventId: string;
  eventTitle: string;
  averageRating: number;
  totalFeedbacks: number;
  feedbacks: EvaluationResponseDTO[];
}
