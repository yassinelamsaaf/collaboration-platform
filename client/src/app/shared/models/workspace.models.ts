export type GlobalRole = 'USER' | 'ADMIN';
export type ProjectRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
export type TeamRole = 'LEADER' | 'MEMBER';
export type ProjectStatus = 'ACTIVE' | 'ARCHIVED';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type NotificationType =
  | 'TASK_ASSIGNED'
  | 'STATUS_CHANGED'
  | 'COMMENT_ADDED'
  | 'DEADLINE_APPROACHING'
  | 'MEMBER_INVITED';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Project {
  id: string;
  slug: string;
  name: string;
  description: string | null;
  createdByUserId: string;
  status: ProjectStatus;
  currentUserRole: ProjectRole;
  createdAt: string;
  updatedAt: string;
  archivedAt: string | null;
}

export interface ProjectMember {
  id: string;
  projectId: string;
  userId: string;
  role: ProjectRole;
  joinedAt: string;
  memberName: string;
  memberEmail: string;
}

export interface Team {
  id: string;
  projectId: string;
  slug: string;
  name: string;
  description: string | null;
  createdByUserId: string;
  createdAt: string;
  updatedAt: string;
}

export interface TeamMember {
  id: string;
  teamId: string;
  userId: string;
  role: TeamRole;
  joinedAt: string;
  memberName: string;
  memberEmail: string;
}

export interface ProjectInvitation {
  id: string;
  projectId: string;
  projectSlug: string;
  projectName: string;
  email: string;
  role: ProjectRole;
  status: 'PENDING' | 'ACCEPTED' | 'REVOKED' | 'EXPIRED';
  invitedByUserId: string;
  expiresAt: string;
  createdAt: string;
  acceptedAt: string | null;
}

export interface ProjectInvitationPreview {
  projectId: string;
  projectSlug: string;
  projectName: string;
  email: string;
  role: ProjectRole;
  status: 'PENDING' | 'ACCEPTED' | 'REVOKED' | 'EXPIRED';
  expiresAt: string;
}

export interface Task {
  id: string;
  projectId: string;
  teamId: string;
  assigneeId: string | null;
  title: string;
  description: string | null;
  priority: Priority;
  status: TaskStatus;
  dueDate: string | null;
  createdByUserId: string;
  subTaskCount: number;
  completedSubTaskCount: number;
  commentCount: number;
  attachmentCount: number;
  totalTimeMinutes: number;
  createdAt: string;
  updatedAt: string;
}

export interface Label {
  id: string;
  projectId: string;
  name: string;
  color: string;
  createdAt: string;
}

export interface SubTask {
  id: string;
  taskId: string;
  assigneeId: string | null;
  title: string;
  isDone: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TimeEntry {
  id: string;
  taskId: string;
  userId: string;
  durationMinutes: number;
  date: string;
  description: string | null;
  createdAt: string;
}

export interface Comment {
  id: string;
  taskId: string;
  userId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface Attachment {
  id: string;
  taskId: string;
  userId: string;
  fileName: string;
  fileUrl: string;
  fileSize: number;
  createdAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  relatedEntityType: string | null;
  relatedEntityId: string | null;
  createdAt: string;
}

export interface NotificationPrefs {
  taskAssignedEmail: boolean;
  statusChangedEmail: boolean;
  commentAddedEmail: boolean;
  deadlineApproachingEmail: boolean;
  memberInvitedEmail: boolean;
}

export interface ActivityLog {
  id: string;
  actorId: string;
  projectId: string;
  entityType: string;
  entityId: string;
  action: string;
  details: string;
  timestamp: string;
}

export interface ProjectProgress {
  project: Project;
  total: number;
  done: number;
  percent: number;
}

export type StatusBreakdown = Record<TaskStatus, number>;
export type PriorityBreakdown = Record<Priority, number>;

export interface DashboardSnapshot {
  projects: Project[];
  projectCount: number;
  activeProjectCount: number;
  taskCount: number;
  overdueCount: number;
  dueSoonCount: number;
  completedCount: number;
  inProgressCount: number;
  myTaskCount: number;
  completionRate: number;
  statusBreakdown: StatusBreakdown;
  priorityBreakdown: PriorityBreakdown;
  projectProgress: ProjectProgress[];
  myTasks: Task[];
  upcomingTasks: Task[];
  recentNotifications: NotificationItem[];
}

export interface ProjectWorkspaceSnapshot {
  project: Project;
  members: ProjectMember[];
  teams: Team[];
  invitations: ProjectInvitation[];
  activity: ActivityLog[];
  tasks: Task[];
  labels: Label[];
}

export interface SearchResultItem {
  kind: 'project' | 'task' | 'member' | 'team';
  id: string;
  title: string;
  subtitle: string;
  meta?: string;
  route: string;
}
