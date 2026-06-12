export interface NotificationResponse {
  id: string;
  userId: string;
  type: 'TASK_ASSIGNED' | 'STATUS_CHANGED' | 'COMMENT_ADDED' | 'MEMBER_INVITED' | 'DEADLINE_APPROACHING';
  title: string;
  message: string;
  isRead: boolean;
  relatedEntityType: string;
  relatedEntityId: string;
  createdAt: string;
}

export interface NotificationPrefsResponse {
  id: string;
  userId: string;
  emailOnTaskAssignment: boolean;
  emailOnStatusChange: boolean;
  emailOnCommentAdded: boolean;
  emailOnDeadlineApproaching: boolean;
}

export interface UpdateNotificationPrefsRequest {
  emailOnTaskAssignment?: boolean;
  emailOnStatusChange?: boolean;
  emailOnCommentAdded?: boolean;
  emailOnDeadlineApproaching?: boolean;
}

export interface UnreadCountResponse {
  count: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
