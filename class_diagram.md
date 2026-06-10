
## Collaboration Platform — Class Diagram Description

### Interface: `Auditable`
A shared interface implemented by entities that need audit timestamps.

**Fields:**
- `createdAt: LocalDateTime` — timestamp when the record was created
- `updatedAt: LocalDateTime` — timestamp of the last modification

**Implemented by:** `User`, `Project`

---

### Entity: `User`
Represents a registered platform user. Implements `Auditable`.

**Fields:**
- `id: UUID` — primary key
- `email: String` — unique login email
- `passwordHash: String` — hashed user password
- `userName: String` — display name
- `avatarUrl: String` — profile picture URL

**Methods:** `register()`, `login()`, `resetPassword()`, `updateProfile()`

**Relationships:**
- **1 → 0..\*** with `ProjectMember` — a user can join multiple projects, each membership represented as a `ProjectMember` record ("joins project as")
- **1 → 0..\*** with `Notification` — a user receives multiple notifications ("receives")
- **1 → 0..\*** with `PasswordResetToken` — a user can have multiple (time-scoped) password reset tokens
- **1 → 0..\*** with `RefreshToken` — a user can hold multiple active refresh tokens (via `userId` in `RefreshToken`)

---

### Entity: `Project`
Represents a top-level project on the platform. Implements `Auditable`.

**Fields:**
- `id: UUID` — primary key
- `name: String`
- `description: String`
- `startDate: LocalDate`
- `endDate: LocalDate`
- `isArchived: boolean` — soft-archive flag

**Methods:** `create()`, `archive()`, `getProgress()`, `addTeam()`

**Relationships:**
- **1 → 1..\*** with `Team` — a project contains at least one team ("contains", composition)
- **1 → 1..\*** with `Task` — a project directly owns one or more tasks ("owns", composition)

---

### Entity: `ProjectMember`
Join table representing a user's membership in a specific project, with a role assigned.

**Fields:**
- `userId: UUID` — FK → `User`
- `role: ProjectRole` — enum value (OWNER, MEMBER, or OBSERVER)
- `joinedAt: LocalDateTime` — when the user joined the project

**Relationships:**
- **\* → 1** with `User` (via `userId`) — many project memberships belong to one user
- **1 → 1** with `TeamMember` — each `ProjectMember` has exactly one corresponding `TeamMember` record ("has team membership")
- **1 (OWNER) → 0..\*** with `Invitation` — a project owner can send multiple invitations
- **1 (OWNER) → \*** with `Team` — a project owner can create multiple teams ("creates — if owner")
- **1 (OWNER) → 1..\*** with `Task` — a project owner creates tasks ("Owner:Creates")
- Uses **`ProjectRole`** enum for its `role` field

---

### Enumeration: `ProjectRole`
Defines the roles a user can have within a project.

**Values:** `OWNER`, `MEMBER`, `OBSERVER`

---

### Entity: `Team`
A sub-group within a project, composed of members who work on assigned tasks.

**Fields:**
- `id: UUID` — primary key
- `name: String`
- `projectId: UUID` — FK → `Project`

**Methods:** `createTeam()` (by project owner), `removeMember()`, `addMember()`

**Relationships:**
- **\* → 1** with `Project` (via `projectId`) — multiple teams belong to one project
- **1 → \*** with `Task` — a team owns (is assigned) multiple tasks ("owns")
- **1 → 2..\*** with `TeamMember` — a team must have at least 2 members

---

### Entity: `TeamMember`
Represents a user's membership within a specific team, with a team-level role.

**Fields:**
- `id: UUID` — primary key
- `TeamRole: Enum<TeamRole>` — role within the team (LEADER or MEMBER)
- `joinedAt: LocalDateTime` — when the user joined the team

**Relationships:**
- **2..\* → 1** with `Team` — at least 2 team members per team
- **1 → 1** with `ProjectMember` — mirrors a project membership
- **1 (LEADER) → \*** with `Task` — a team leader is assigned to multiple tasks ("Team leader: Assigned To")
- **1 → \*** with `SubTask` — team members can be assigned subtasks ("assignedTo")

---

### Enumeration: `TeamRole`
Defines roles within a team.

**Values:** `LEADER`, `MEMBER`

---

### Entity: `Task`
A unit of work assigned to a team within a project.

**Fields:**
- `id: UUID` — primary key
- `title: String`
- `description: String`
- `priority: Priority` — enum (LOW / MEDIUM / HIGH / URGENT)
- `status: TaskStatus` — enum (TODO / IN_PROGRESS / IN_REVIEW / DONE)
- `dueDate: LocalDate`

**Methods:** `assignToTeam()` (project owner), `splitSubTasks()` (team leader), `assignSubTasks()` (team leader), `updateStatus()`, `addLabel()`, `addAttachment()`, `logTime()`

**Relationships:**
- **\* → 1** with `Team` — multiple tasks belong to one team
- **\* → 1** with `Project` — multiple tasks belong to one project
- **1 → 0..\*** with `SubTask` — a task can be split into zero or more subtasks ("breaks down into")
- **1 → \*** with `TimeEntry` — a task is tracked by multiple time entries ("tracks time via")
- **0..\* ↔ 0..\*** with `Label` — many-to-many relationship; a task can have many labels, and a label can be applied to many tasks ("tagged with")
- **1 → \*** with `Attachment` — a task can have multiple attachments (via `taskId` in `Attachment`)
- **1 → \*** with `Comment` — a task can have multiple comments ("Has")
- Uses **`Priority`** and **`TaskStatus`** enums

---

### Enumeration: `Priority`
Task urgency levels.

**Values:** `LOW`, `MEDIUM`, `HIGH`, `URGENT`

---

### Enumeration: `TaskStatus`
Lifecycle states of a task.

**Values:** `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`

---

### Entity: `SubTask`
A granular unit of work derived from a parent `Task`, assignable to individual team members.

**Fields:**
- `id: UUID` — primary key
- `title: String`
- `isDone: boolean` — completion flag

**Methods:** `complete()`, `reopen()`

**Relationships:**
- **\* → 1** with `Task` — many subtasks belong to one task
- **\* → 1** with `TeamMember` — a subtask is assigned to one team member ("assignedTo")

---

### Entity: `Label`
A colored tag scoped to a project, applied to tasks for categorization.

**Fields:**
- `id: UUID` — primary key
- `name: String`
- `color: String` — hex or named color
- `projectId: UUID` — FK → `Project`

**Methods:** `create()`, `delete()`

**Relationships:**
- **\* → 1** with `Project` (via `projectId`) — labels are scoped to a project
- **0..\* ↔ 0..\*** with `Task` — many-to-many with tasks

---

### Entity: `Attachment`
A file attached to a specific task, stored via URL (e.g., MinIO/S3 object storage).

**Fields:**
- `id: UUID` — primary key
- `fileName: String`
- `fileUrl: String` — URL to the stored file
- `taskId: UUID` — FK → `Task`

**Methods:** `upload()`, `delete()`

**Relationships:**
- **\* → 1** with `Task` (via `taskId`) — multiple attachments belong to one task

---

### Entity: `Comment`
A text comment posted on a task.

**Fields:**
- `id: UUID` — primary key
- `content: String`

**Relationships:**
- **\* → 1** with `Task` — many comments belong to one task ("Has")

---

### Entity: `TimeEntry`
Records time spent by a user on a specific task.

**Fields:**
- `id: UUID` — primary key
- `userId: UUID` — FK → `User`
- `taskId: UUID` — FK → `Task`
- `durationMinutes: int` — total time logged in minutes
- `date: LocalDate` — the date the time was logged

**Methods:** `start()`, `stop()`

**Relationships:**
- **\* → 1** with `Task` (via `taskId`) — multiple time entries per task
- **\* → 1** with `User` (via `userId`) — multiple time entries per user

---

### Entity: `Notification`
An in-app (and optionally email) notification sent to a user.

**Fields:**
- `id: UUID` — primary key
- `isRead: boolean` — read/unread flag
- `message: String` — notification body
- `type: String` — categorization (e.g., task assigned, invitation)
- `userId: UUID` — FK → `User`

**Methods:** `sendEmail()`, `send()`, `markAsRead()`

**Relationships:**
- **\* → 1** with `User` (via `userId`) — many notifications per user
- **\* → 1** with `ActivityLog` — activity logs trigger/use notifications ("Uses", * → 1)

---

### Entity: `ActivityLog`
An audit trail record capturing every meaningful action taken within the platform.

**Fields:**
- `id: UUID` — primary key
- `actorId: UUID` — FK → `User` (who performed the action)
- `projectId: UUID` — FK → `Project` (in which project)
- `entityType: String` — the type of entity affected (e.g., "Task", "Team")
- `action: String` — the action performed (e.g., "CREATED", "UPDATED")
- `timestamp: LocalDateTime` — when the action occurred

**Relationships:**
- **1 → \*** with `Notification` — one activity log entry can generate multiple notifications ("Uses")

---

### Entity: `Invitation`
A token-based invitation sent to an email address to join a project (and optionally a team).

**Fields:**
- `id: UUID` — primary key
- `email: String` — recipient email
- `token: String` — secure, unique invitation token
- `projectId: UUID` — FK → `Project`
- `teamId?: UUID` — optional FK → `Team` (nullable; only set if inviting directly into a team)
- `expiresAt: LocalDateTime` — token expiry time
- `status: InvitationStatus` — enum (PENDING / ACCEPTED / REVOKED / EXPIRED)

**Methods:** `sendInvite()` (by team owner), `revoke()`, `accept()`

**Relationships:**
- **\* → 1** with `Project` (via `projectId`) — multiple invitations can be sent for one project
- **\* → 0..1** with `Team` (via `teamId`) — optionally scoped to a specific team
- Uses **`InvitationStatus`** enum for its `status` field

---

### Enumeration: `InvitationStatus`
Lifecycle states of an invitation.

**Values:** `PENDING`, `ACCEPTED`, `REVOKED`, `EXPIRED`

---

### Entity: `RefreshToken`
A long-lived token used to obtain new JWT access tokens without re-authentication.

**Fields:**
- `id: UUID` — primary key
- `userId: UUID` — FK → `User`
- `token: String` — opaque refresh token string
- `expiresAt: LocalDateTime`
- `revoked: boolean` — whether the token has been explicitly revoked

**Relationships:**
- **\* → 1** with `User` (via `userId`) — a user can have multiple active refresh tokens

---

### Entity: `PasswordResetToken`
A short-lived, single-use token sent by email to allow a user to reset their password.

**Fields:**
- `id: UUID` — primary key
- `userId: UUID` — FK → `User`
- `token: String` — secure reset token
- `expiresAt: LocalDateTime`

**Relationships:**
- **\* → 1** with `User` (via `userId`) — a user can have multiple (time-scoped) reset tokens

---

### Summary of Key Relationship Chain

The overall flow of ownership and responsibility in the domain is:

**User → ProjectMember → Project → Team → Task → SubTask**

A `User` joins a `Project` as a `ProjectMember` (with a `ProjectRole`). The project owner creates `Team`s within the project and assigns `Task`s to them. A team leader (via `TeamMember` with `TeamRole.LEADER`) splits those tasks into `SubTask`s and assigns them to individual `TeamMember`s. Tasks can be enriched with `Attachment`s, `Label`s, `Comment`s, and `TimeEntry`s. Cross-cutting concerns like `Notification`, `ActivityLog`, `Invitation`, `RefreshToken`, and `PasswordResetToken` operate at the user/project level to support collaboration, auditing, and authentication.