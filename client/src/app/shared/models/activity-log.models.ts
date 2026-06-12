export interface ActivityLogResponse {
  id: string;
  actorId: string;
  actorName?: string;
  projectId: string;
  entityType: string;
  entityId: string;
  action: string;
  details: string;
  timestamp: string;
}
