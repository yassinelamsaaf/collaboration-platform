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
- `GET /api/projects/{projectRef}/teams` lists teams for any project member, using pagination.
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

Known gaps:
- Projects frontend is not implemented yet.
- API service is currently auth-centric and should be generalized before adding project screens.

Auth guard:
- `core/guards/auth.guard.ts` — calls `AuthService.checkSession()` (POST /api/auth/refresh), redirects to `/auth/login` on failure.
- Applied via `canActivate` on `/dashboard` route in `app-routing.module.ts`.
- A minimal `DashboardComponent` placeholder exists in `features/dashboard/` as a protected route shell.

Password reset frontend:
- `/auth/forgot-password` — email form → calls `POST /api/auth/forgot-password` → navigates to reset-password.
- `/auth/reset-password?email=...` — 6-digit OTP (same style as verify-email), new password, confirm password → calls `POST /api/auth/reset-password`.
- "Resend verification code" link with 60-second countdown cooldown.
- Reuses existing SCSS classes: `.auth-center`, `.auth-card`, `.otp-grid`, `.input-with-icon`, `.link-button`, `.btn--primary`, `.auth-inline`.

## Seeds / Testing Notes

Planned:
- Add development seed data for users, projects, and project memberships once the backend module shape settles.
- Add backend tests for project access rules before building task management.

Local email testing:
- Docker Compose includes MailHog for local SMTP capture.
- Backend SMTP inside Docker uses `mailhog:1025`.
- Backend SMTP from an IDE/local process uses `localhost:1025`.
- Local sender address uses `APP_MAIL_FROM=no-reply@collaboration-platform.local`.
- MailHog web UI is available at `http://localhost:8025`.

### Work Management

Package: `server/src/main/java/com/inpt/collaborationplatform/tasks`

Folders:
- `task/` for `Task` entity, CRUD controller/service, DTOs, mapper, repository.
- `subtask/` for `SubTask` entity, CRUD controller/service, DTOs, mapper, repository.
- `label/` for `Label` entity, CRUD + task-assignment controller/service, DTOs, mapper, repository.
- `comment/` for `Comment` entity, CRUD controller/service, DTOs, mapper, repository.
- `attachment/` for `Attachment` entity, CRUD controller/service, DTOs, mapper, repository.
- `timeentry/` for `TimeEntry` entity, CRUD controller/service, DTOs, mapper, repository.
- Shared lookups live in `TeamLookupService` (projects module) and `TaskLookupService` (tasks module) so all services reuse consistent "require team / task / team-leader" behavior.

Implemented:
- **Task** entity with title, description, priority (`LOW`/`MEDIUM`/`HIGH`/`URGENT`), status (`TODO`/`IN_PROGRESS`/`IN_REVIEW`/`DONE`), dueDate (LocalDate), assigneeId (references TeamMember ID).
- **SubTask** entity with title, isDone flag, assigneeId (references TeamMember ID).
- **TimeEntry** entity with durationMinutes, date, description, linked to task + user.
- **Label** entity with name, hex color, scoped to project, many-to-many with Task via `task_labels` join table.
- **Comment** entity with content, linked to task + user.
- **Attachment** entity with fileName, fileUrl, fileSize, linked to task + user.
- Task response computed aggregates: `subTaskCount`, `completedSubTaskCount`, `commentCount`, `attachmentCount`, `totalTimeMinutes`.
- Sub-tasks, comments, attachments, and time entries cascade-deleted when a task is deleted.
- Own-resource ownership checks for comment, attachment, and time-entry deletion (author-only).
- Label ↔ Task many-to-many add/remove endpoints.
- Shared lookup services for Team, TeamMember, and Task eliminate private-helper duplication.
- Aggregate computation lives in `TaskService.computeAggregates()`, not in the mapper.

Access control rules:
- **Task creation**: `OWNER` or `ADMIN` (`requireManager`).
- **Task update / delete**: `OWNER` or `ADMIN` (`requireManager`).
- **Task status update**: `MEMBER` or above (`requireContributor`).
- **SubTask CRUD**: team `LEADER` only (`requireTeamLeader`).
- **Comment / Attachment / TimeEntry create**: `MEMBER` or above (`requireContributor`).
- **Comment / Attachment / TimeEntry delete**: `MEMBER` or above **and** must be the author.
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
| POST | `/api/projects/{projectRef}/teams/{teamRef}/tasks` | Create task | Manager+ (OWNER/ADMIN) |
| GET | `/api/projects/{projectRef}/teams/{teamRef}/tasks` | List tasks (paginated) | Viewer+ |
| GET | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Get task | Viewer+ |
| PATCH | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Update task | Manager+ |
| PATCH | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/status` | Update task status | Contributor+ |
| DELETE | `/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}` | Delete task + cascade | Manager+ |
| POST | `.../tasks/{taskId}/subtasks` | Create sub-task | Team Leader only |
| GET | `.../tasks/{taskId}/subtasks` | List sub-tasks | Viewer+ |
| PATCH | `.../tasks/{taskId}/subtasks/{subTaskId}` | Update sub-task | Team Leader only |
| DELETE | `.../tasks/{taskId}/subtasks/{subTaskId}` | Delete sub-task | Team Leader only |
| POST | `.../tasks/{taskId}/comments` | Create comment | Contributor+ |
| GET | `.../tasks/{taskId}/comments` | List comments (paginated) | Viewer+ |
| DELETE | `.../tasks/{taskId}/comments/{commentId}` | Delete own comment | Author only |
| POST | `.../tasks/{taskId}/attachments` | Add attachment | Contributor+ |
| GET | `.../tasks/{taskId}/attachments` | List attachments | Viewer+ |
| DELETE | `.../tasks/{taskId}/attachments/{attachmentId}` | Delete own attachment | Author only |
| POST | `.../tasks/{taskId}/time-entries` | Log time | Contributor+ |
| GET | `.../tasks/{taskId}/time-entries` | List time entries (paginated) | Viewer+ |
| DELETE | `.../tasks/{taskId}/time-entries/{entryId}` | Delete own time entry | Author only |

Current boundaries:
- Tasks owns `tasks`, `sub_tasks`, `time_entries`, `labels`, `comments`, `attachments`.
- Task `assigneeId` references `TeamMember` ID (not `User` ID), consistent with class diagram.
- Labels are scoped to a project, not a team; label–task assignment uses project-scoped task lookup.
- Subtasks are not deletable by the task assignee — only the team leader can manage them.
- Comments, attachments, and time entries are owned by the creating user; only the author or a manager can delete them.
- Aggregates are computed per-request (no caching); list endpoint does N+1 aggregate queries.

Known gaps:
- No batch aggregate query for the list endpoint (currently N+1).
- `currentUserId()` utility duplicated across all 6 controllers — should be extracted.
- No integration tests for access rules or cascading deletes.
- Frontend for Work Management is not implemented yet.

Verification:
- `mvnw compile` passes.








# 6 modules summary :
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
└── TaskStatus

Collaboration
│
├── Comment
└── Attachment

Notifications
│
└── Notification

Audit
│
└── ActivityLog
