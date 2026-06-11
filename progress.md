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
