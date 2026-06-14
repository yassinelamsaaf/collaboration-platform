# Collaboration Platform Progress

This document tracks what exists in each module so the codebase can be understood quickly.

## Backend

### Identity & Access

Package: `server/src/main/java/com/inpt/collaborationplatform/Identity`

Implemented:
- User registration with email verification code.
- Login with JWT access and refresh tokens stored in HttpOnly cookies.
- Refresh token rotation backed by Redis.
- Logout with refresh-token removal and access-token blacklist.
- Current authenticated user lookup through `/api/auth/me`.
- Global roles: `USER`, `ADMIN`.

Expected local flow:
- `POST /api/auth/register` creates a disabled user and sends a MailHog verification code.
- `POST /api/auth/verify-code` enables the user after a valid code.
- `POST /api/auth/login` only succeeds for enabled users and writes access/refresh cookies.
- `GET /api/auth/me` and `POST /api/auth/logout` require an authenticated access cookie.
- `POST /api/auth/refresh` is public at the route layer because it authenticates via the refresh cookie.
- Local HTTP uses `APP_COOKIE_SECURE=false`; production HTTPS should use `APP_COOKIE_SECURE=true`.

Current boundaries:
- IAM answers "who is the current user?" and "what is their global role?"
- Project-scoped permissions are not owned by IAM; they belong to the Projects module.

Known gaps:
- Frontend route guard is not implemented yet.

Password reset:
- `POST /api/auth/forgot-password` sends a 6-digit code to the user's email.
- `POST /api/auth/reset-password` validates the code and updates the password.
- `POST /api/auth/resend-reset-code` sends a new code.
- Rate limiting: 60-second cooldown between requests, max 5 requests per hour per email.
- Reset codes expire after 10 minutes.
- Errors: invalid email, unverified account, invalid code, expired code, rate limit cooldown, rate limit max exceeded.

### Projects

Package: `server/src/main/java/com/inpt/collaborationplatform/projects`

Folders:
- `project/` for `Project`, `ProjectMember`, project roles, project CRUD, and project membership management.
- `invitation/` for pending project invitations and invitation DTO/repository code.
- `team/` for `Team`, `TeamMember`, team roles, team CRUD, and team membership management.
- `shared/` for project-module helpers such as slug generation.
- Each slice keeps its own mapper so services stay focused on use-case/business logic.
- Shared project lookups live in `ProjectLookupService` so project, invitation, team, and later work-management services reuse consistent "require project/member" behavior.

Implemented:
- Project domain with active/archived status.
- Project membership with project-scoped roles.
- Pending project invitations by email.
- Team domain with team-scoped roles.
- Project creation.
- Project and team slugs for readable frontend URLs while keeping UUIDs as internal database identities.
- Listing projects for the current user.
- Team listing supports backend search with `q` and returns `memberCount` for each team.
- Reading/updating/archiving a project with access checks.
- Listing project members with pagination, role-updating, and removing accepted project members.
- Inviting users by email and accepting invitations.
- Creating/listing/updating/deleting teams inside a project.
- Listing, adding, role-updating, and removing team members.
- Explicit project access levels: viewer, contributor, manager, owner.

Backend endpoints:
- `POST /api/projects` creates a project and makes the current user `OWNER`.
- `GET /api/projects` lists projects where the current user is a member, using pagination.
- `GET /api/projects/{projectRef}` returns one project if the current user is a member; `projectRef` can be the UUID or slug.
- `PATCH /api/projects/{projectRef}` updates project name/description for `OWNER` or `ADMIN`.
- `POST /api/projects/{projectRef}/archive` archives a project for `OWNER` or `ADMIN`.
- `GET /api/projects/{projectRef}/members` lists project members for any project member, using pagination.
- `PATCH /api/projects/{projectRef}/members/{memberUserId}/role` changes a member role for `OWNER`.
- `DELETE /api/projects/{projectRef}/members/{memberUserId}` removes a member for `OWNER`.
- `POST /api/projects/{projectRef}/invitations` creates a pending email invitation for `OWNER` or `ADMIN`.
- `GET /api/projects/{projectRef}/invitations` lists project invitations for `OWNER` or `ADMIN`, using pagination.
- `GET /api/projects/invitations/{token}` previews an invitation by token.
- `POST /api/projects/invitations/{token}/accept` accepts an invitation for the logged-in user with the invited email.
- `POST /api/projects/{projectRef}/invitations/{invitationId}/cancel` cancels a pending invitation for `OWNER` or `ADMIN`.
- `POST /api/projects/{projectRef}/teams` creates a team for `OWNER` or `ADMIN`.
- `GET /api/projects/{projectRef}/teams` lists teams for any project member, using pagination and optional `q` search by name/description.
- `GET /api/projects/{projectRef}/teams/{teamRef}` returns one team for any project member; `teamRef` can be the UUID or slug.
- `PATCH /api/projects/{projectRef}/teams/{teamRef}` updates team name/description for `OWNER` or `ADMIN`.
- `DELETE /api/projects/{projectRef}/teams/{teamRef}` deletes a team for `OWNER` or `ADMIN`.
- `GET /api/projects/{projectRef}/teams/{teamRef}/members` lists team members for any project member, using pagination.
- `POST /api/projects/{projectRef}/teams/{teamRef}/members` adds an existing project member to a team for `OWNER` or `ADMIN`.
- `PATCH /api/projects/{projectRef}/teams/{teamRef}/members/{memberUserId}/role` changes a team member role for `OWNER` or `ADMIN`.
- `DELETE /api/projects/{projectRef}/teams/{teamRef}/members/{memberUserId}` removes a team member for `OWNER` or `ADMIN`.

Current boundaries:
- Projects owns `projects` and `project_members`.
- Projects owns `project_invitations`; pending invitations are not project access yet.
- Projects owns `teams` and `team_members`.
- Project slugs are globally unique; team slugs are unique inside their project.
- New project members are created only when a pending invitation is accepted, except the project creator who becomes `OWNER`.
- Team members must already be accepted project members.
- Project `VIEWER` is read-only and cannot be added to teams.
- Team names are stored with a normalized key so uniqueness is enforced case-insensitively inside each project.
- `ProjectMember` stores `userId` instead of directly owning or modifying `User`.
- IAM identifies the user; Projects decides what that user can do inside a project.

Project roles:
- `OWNER`: full project control, archive project, change roles, remove members, manage teams, invite/cancel invitations.
- `ADMIN`: manage operational structure, update project, invite members, manage teams/team members.
- `MEMBER`: normal contributor; can read project context and later create/update work items.
- `VIEWER`: read-only project access; can view project, teams, tasks, comments, but cannot contribute or manage structure.

Verification:
- `bash mvnw -DskipTests package` passes.
- Postman collection JSON validates with `jq empty`.
- Docker server image rebuilds and restarts with `docker compose up -d --build server`.
- `bash mvnw test` currently fails before project logic because the default context-load test has no local test environment values for mail/database/Redis.

## Frontend

### Existing

Implemented:
- Public landing page.
- Auth pages: login, register, verify email, forgot password UI.
- Shared UI helpers and toast/error handling.
- Responsive auth and landing polish for mobile navigation, footer layout, and form spacing.
- Runtime frontend API configuration through `public/assets/env.js` so the same Angular build can target local or Docker environments.
- Frontend Docker image with Nginx static serving and SPA fallback routing.
- Dashboard workspace shell with responsive sidebar, project navigation, and protected project routes.
- Projects page for creating/listing/archive actions.
- Project overview, dedicated Project Members, Team Management, Kanban, task detail, and search screens.
- Project Members screen owns member search/filtering, invitations, project-role changes, member removal, and role-aware read-only states.
- Team Management screen uses backend team pagination/search, displays team participant counts, and focuses only on team creation plus selected-team membership with a compact selected-team inspector.
- Project workspace tabs are now split into `Overview`, `Project Members`, `Team Management`, and `Kanban Tasks`.
- Task detail and Kanban RBAC mirrors the backend: project contributors can comment, attach files, log time, and update task status; project contributors who are also members of the task's team can move/complete subtasks; subtask creation and metadata edits stay limited to the selected team leader; project owner can delete subtasks as an override.
- Dashboard write forms now show inline validation errors and warning toasts instead of silently ignoring invalid submissions.

Known gaps:
- API service is still auth-centric and should be generalized before adding project screens.

Auth guard:
- `core/guards/auth.guard.ts` — calls `AuthService.checkSession()` (POST /api/auth/refresh), redirects to `/auth/login` on failure.
- Applied via `canActivate` on `/dashboard` route in `app-routing.module.ts`.
- A minimal `DashboardComponent` placeholder exists in `features/dashboard/` as a protected route shell.

Password reset frontend:
- `/auth/forgot-password` — email form → calls `POST /api/auth/forgot-password` → navigates to reset-password.
- `/auth/reset-password?email=...` — 6-digit OTP (same style as verify-email), new password, confirm password → calls `POST /api/auth/reset-password`.
- "Resend verification code" link with 60-second countdown cooldown.
- Reuses existing SCSS classes: `.auth-center`, `.auth-card`, `.otp-grid`, `.input-with-icon`, `.link-button`, `.btn--primary`, `.auth-inline`.

Docker/frontend runtime:
- `client/Dockerfile` builds the Angular app and serves it with Nginx on container port `80`.
- `client/public/assets/env.js` is generated at container startup from `CLIENT_API_BASE_URL`.
- Root `docker-compose.yml` now starts `client`, `server`, `db`, `redis`, and `mailhog` together.

## Seeds / Testing Notes

Local email testing:
- Docker Compose includes MailHog for local SMTP capture.
- Backend SMTP inside Docker uses `mailhog:1025`.
- Backend SMTP from an IDE/local process uses `localhost:1025`.
- Local sender address uses `APP_MAIL_FROM=no-reply@collaboration-platform.local`.
- MailHog web UI is available at `http://localhost:8025`.

Seed data (Created when users table is empty):
- Users: `admin@test.com` / `Admin123!` (ADMIN), `alice@test.com` / `Alice123!` (USER)
- Projects: "Demo Project", "Mobile App"
- Teams: Engineering, Design (Demo); Mobile Team (Mobile App)
- Tasks: 4 tasks across teams with various priorities/statuses
- Subtasks: 8 total across tasks
- Comments: 4, Attachments: 3, Time entries: 3
- NotificationPrefs: 2 rows (default enabled for both users)
- Notifications: 4 (2 per user — TASK_ASSIGNED, STATUS_CHANGED, COMMENT_ADDED, DEADLINE_APPROACHING)
- ActivityLogs: 4 entries

### Work Management

Package: `server/src/main/java/com/inpt/collaborationplatform/tasks`

Folders:
- `task/` for `Task` entity, CRUD controller/service, DTOs, mapper, repository.
- `subtask/` for `SubTask` entity, CRUD controller/service, DTOs, mapper, repository.
- `label/` for `Label` entity, CRUD + task-assignment controller/service, DTOs, mapper, repository.
- `timeentry/` for `TimeEntry` entity, CRUD controller/service, DTOs, mapper, repository.
- `scheduler/` — `DeadlineScheduler` (cron: 8 AM daily, publishes `DeadlineApproachingEvent` for tasks due tomorrow)
- Shared lookups live in `TeamLookupService` (projects module) and `TaskLookupService` (tasks module) so all services reuse consistent "require team / task / team-leader" behavior.

Implemented:
- **Task** entity with title, description, priority, status, dueDate, assigneeId (references TeamMember ID).
- **SubTask** entity with title, isDone flag, assigneeId.
- **TimeEntry** entity with durationMinutes, date, description.
- **Label** entity with name, hex color, scoped to project, many-to-many with Task.
- Task response computed aggregates: `subTaskCount`, `completedSubTaskCount`, `commentCount`, `attachmentCount`, `totalTimeMinutes`.
- Aggregate computation lives in `TaskService.computeAggregates()`, not in the mapper.
- Comment/attachment counts are read through `CollaborationQueryService`; Work Management does not inject Collaboration repositories directly.
- Domain events published: `TaskAssignedEvent` (create + reassign), `TaskStatusChangedEvent`, `TaskDeletedEvent`.

Access control rules:
- **Task creation**: `OWNER` or `ADMIN` (`requireManager`).
- **Task update / delete**: `OWNER` or `ADMIN` (`requireManager`).
- **Task status update**: `MEMBER` or above (`requireContributor`).
- **SubTask CRUD**: team `LEADER` only (`requireTeamLeader`).
- **TimeEntry create**: `MEMBER` or above (`requireContributor`).
- **TimeEntry delete**: `MEMBER` or above **and** must be the author.
- **Label create**: `MEMBER` or above (`requireContributor`).
- **Label delete**: `OWNER` or `ADMIN` (`requireManager`).
- **Label–task assign / unassign**: `MEMBER` or above (`requireContributor`).
- **All list / read operations**: `VIEWER` or above (`requireViewer`).

Backend endpoints:

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/projects/{projectRef}/labels` | Create label | Contributor+ |
| GET | `/api/projects/{projectRef}/labels` | List labels | Viewer+ |
| DELETE | `/api/projects/{projectRef}/labels/{labelId}` | Delete label | Manager+ |
| POST | `/api/projects/{projectRef}/labels/{labelId}/tasks/{taskId}` | Add label to task | Contributor+ |
| DELETE | `/api/projects/{projectRef}/labels/{labelId}/tasks/{taskId}` | Remove label from task | Contributor+ |
| POST | `/api/projects/{projectRef}/teams/{teamRef}/tasks` | Create task | Manager+ |
| GET | `/api/projects/{projectRef}/teams/{teamRef}/tasks` | List tasks (paginated) | Viewer+ |
| GET | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Get task | Viewer+ |
| PATCH | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Update task | Manager+ |
| PATCH | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/status` | Update task status | Contributor+ |
| DELETE | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Delete task + cascade | Manager+ |
| POST | `.../tasks/{taskId}/subtasks` | Create sub-task | Team Leader only |
| GET | `.../tasks/{taskId}/subtasks` | List sub-tasks | Viewer+ |
| PATCH | `.../tasks/{taskId}/subtasks/{subTaskId}` | Update sub-task | Team Leader only |
| DELETE | `.../tasks/{taskId}/subtasks/{subTaskId}` | Delete sub-task | Team Leader only |
| POST | `.../tasks/{taskId}/time-entries` | Log time | Contributor+ |
| GET | `.../tasks/{taskId}/time-entries` | List time entries (paginated) | Viewer+ |
| DELETE | `.../tasks/{taskId}/time-entries/{entryId}` | Delete own time entry | Author only |

Current boundaries:
- Tasks owns `tasks`, `sub_tasks`, `time_entries`, `labels`.
- Task `assigneeId` references `TeamMember` ID (not `User` ID).
- Labels are scoped to a project, not a team.
- Aggregates are computed per-request (N+1 on list endpoint).

Known gaps:
- No batch aggregate query for the list endpoint (currently N+1).
- No integration tests for access rules or cascading deletes.
- Frontend for Work Management is not implemented yet.

Verification:
- `mvnw compile` passes.

### Collaboration

Package: `server/src/main/java/com/inpt/collaborationplatform/collaboration`

Folders:
- `comment/` for `Comment` entity, CRUD controller/service, DTOs, mapper, repository.
- `attachment/` for `Attachment` entity, CRUD controller/service, DTOs, mapper, repository.
- `service/` for module-level read APIs used by other modules, currently `CollaborationQueryService`.
- `listener/` for Collaboration event handlers, currently `CollaborationCleanupListener`.

Implemented:
- **Comment** entity with content and author user ID.
- **Attachment** entity with fileName, fileUrl, fileSize, and uploaded-by user ID.
- Task-scoped REST endpoints still live at `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/comments` and `/attachments` because that is the most natural client URL.
- Code ownership moved out of Work Management so comments/attachments can later be reused for project discussions, files, or other commentable targets.
- `CommentService` publishes `CommentAddedEvent`; Notification and Audit listen to it.
- `CollaborationCleanupListener` listens to `TaskDeletedEvent` and deletes comments/attachments before the task row is removed.

Access control rules:
- **Comment / Attachment create**: `MEMBER` or above (`requireContributor`).
- **Comment / Attachment delete**: `MEMBER` or above **and** must be the author/uploader.
- **Comment / Attachment list**: `VIEWER` or above (`requireViewer`).

Backend endpoints:

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `.../tasks/{taskId}/comments` | Create comment | Contributor+ |
| GET | `.../tasks/{taskId}/comments` | List comments (paginated) | Viewer+ |
| DELETE | `.../tasks/{taskId}/comments/{commentId}` | Delete own comment | Author only |
| POST | `.../tasks/{taskId}/attachments` | Add attachment | Contributor+ |
| GET | `.../tasks/{taskId}/attachments` | List attachments | Viewer+ |
| DELETE | `.../tasks/{taskId}/attachments/{attachmentId}` | Delete own attachment | Author only |

Current boundaries:
- Collaboration owns `comments` and `attachments`.
- Work Management may ask Collaboration for counts through `CollaborationQueryService`, but it does not directly use Collaboration repositories.
- Task deletion is coordinated by an in-process Spring event: Work Management publishes `TaskDeletedEvent`; Collaboration handles cleanup; Audit records the deletion.

### Notifications & Audit

Packages:

```
shared/
  event/          → TaskAssignedEvent, TaskStatusChangedEvent, CommentAddedEvent,
  │                  MemberInvitedEvent, DeadlineApproachingEvent (plain records)
  config/         → WebSocketConfig (STOMP/SockJS at /ws), CorsConfig, RedisConfig
  util/           → SecurityUtils

tasks/
  scheduler/      → DeadlineScheduler (@Scheduled daily 8AM)

notification/
  listener/       → NotificationListener (events → DB + WebSocket push + async email)
  entity/         → Notification, NotificationType, NotificationPrefs
  controller/     → NotificationController, NotificationPrefsController
  dto/response/   → NotificationResponse
  mapper/         → NotificationMapper
  service/        → NotificationService
  repository/     → NotificationRepository, NotificationPrefsRepository

audit/
  listener/       → ActivityLogListener (events → append-only audit rows)
  controller/     → ActivityLogController (REST read endpoint)
  entity/         → ActivityLog
  dto/response/   → ActivityLogResponse
  mapper/         → ActivityLogMapper
  service/        → ActivityLogService
  repository/     → ActivityLogRepository
```

Implemented:
- **Domain events** (plain records, zero dependencies) published from services via `ApplicationEventPublisher`.
- **ActivityLog** — append-only audit trail with actorId, projectId, entityType, entityId, action, details, timestamp.
- **Notification** — persisted with userId, type (enum), title, message, isRead, entity reference.
- **NotificationPrefs** — per-user email opt-in for each event type (default: all enabled).
- **WebSocket push** — `NotificationListener` injects `SimpMessagingTemplate`, pushes `NotificationResponse` to `/topic/notifications/{userId}` after every create.
- **Async email** — `@Async` method checks prefs, sends via `EmailService.sendNotification()`.
- **DeadlineScheduler** — `@Scheduled(cron = "0 0 8 * * ?")` queries tasks due tomorrow, publishes `DeadlineApproachingEvent`.
- **SecurityUtils** — `currentUserId(authentication)` extracted to `shared/util/SecurityUtils.java`, eliminates duplication across all 12 controllers.
- **TeamMember response enrichment** — `memberName` + `memberEmail` fields added to `TeamMemberResponse` via `requireUserUsername()` in `IdentityAccessService`.
- **Listener simplification** — switched from `@TransactionalEventListener` + `REQUIRES_NEW` to plain `@EventListener`; listeners run inside the publisher's transaction, no separate transaction needed.
- **Bug fix (onCommentAdded)** — the `triggeredByUserId` is a User ID but `event.taskAssigneeId()` is a TeamMember ID; `resolveUserId()` converts TeamMember → User before comparing, so the "skip self-notification" check works correctly.

Notification REST API:

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/notifications` | List user's notifications (paginated, `?unreadOnly=true`) | Authenticated |
| PATCH | `/api/notifications/{id}/read` | Mark one as read | Owner only |
| PATCH | `/api/notifications/read-all` | Mark all as read | Owner only |
| GET | `/api/notifications/unread-count` | Count unread | Authenticated |
| GET | `/api/notifications/prefs` | Get email notification preferences | Authenticated |
| PUT | `/api/notifications/prefs` | Update preferences | Authenticated |

Audit REST API:

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/projects/{projectRef}/activity-log` | Paginated project audit trail | Viewer+ |

Architecture flow:
```
TaskService.createTask(assigneeId=alice)   [@Transactional]
  ↓ publish TaskAssignedEvent
  ↓  (same transaction — @EventListener)
  ↓
  ActivityLogListener → ActivityLogRepository.save()        [append-only]
  NotificationListener:
    1. NotificationRepository.save()                        [in-app notification]
    2. messagingTemplate.convertAndSend()                    [WebSocket push]
    3. @Async emailService.sendNotification()                [email if opted in, runs after tx commits]

Note: @Async self-invocation (calling sendEmailIfOptedIn() from the same class)
bypasses the proxy — the method runs synchronously on the publishing thread.
```

Known gaps:
- `@Async` on `sendEmailIfOptedIn()` is ineffective due to self-invocation (same-class call bypasses the proxy) — email runs synchronously on the publishing thread.
- No rate limiting on the email methods.
- Failed emails are caught silently (intentional — secondary failures never break the client request).
- No frontend WebSocket client or notification UI implemented yet.

## 6 modules summary

Identity & Access
│
├── User
├── RefreshToken
└── PasswordResetToken

Project Management
│
├── Project
├── ProjectMember
├── Team
├── TeamMember
├── Invitation
├── ProjectRole
└── TeamRole

Work Management
│
├── Task
├── SubTask
├── TimeEntry
├── Label
├── Priority
├── TaskStatus
└── DeadlineScheduler

Collaboration
│
├── Comment
└── Attachment

Notifications
│
├── Notification
├── NotificationPrefs
├── NotificationListener
└── WebSocket push

Audit
│
├── ActivityLog
└── ActivityLogListener
