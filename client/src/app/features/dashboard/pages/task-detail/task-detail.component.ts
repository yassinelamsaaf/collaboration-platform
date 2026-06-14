import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@core/services/toast.service';
import { mapHttpError } from '@shared/utils/error-mapper';
import { Priority, Project, TaskStatus, TeamMember } from '@shared/models/workspace.models';
import { TaskBundle, WorkspaceService } from '@features/dashboard/services/workspace.service';

@Component({
  selector: 'app-task-detail',
  templateUrl: './task-detail.component.html',
  styleUrl: './task-detail.component.scss',
  standalone: false
})
export class TaskDetailComponent implements OnInit {
  readonly priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

  projectRef = '';
  teamRef = '';
  taskId = '';
  currentUserId = '';
  project: Project | null = null;
  bundle: TaskBundle | null = null;
  loading = true;
  savingAttachment = false;
  savingSubTask = false;
  showSubTaskForm = false;
  showAttachmentForm = false;
  showTimeForm = false;

  readonly commentForm = {
    content: ''
  };
  commentError = '';

  readonly attachmentForm = {
    fileName: '',
    fileUrl: '',
    fileSize: 1
  };
  attachmentErrors = {
    fileName: '',
    fileUrl: '',
    fileSize: ''
  };

  readonly subTaskForm = {
    title: '',
    assigneeId: ''
  };
  subTaskTitleError = '';

  readonly timeEntryForm = {
    durationMinutes: 30,
    date: '',
    description: ''
  };
  timeEntryErrors = {
    durationMinutes: '',
    date: ''
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService,
    private readonly workspaceService: WorkspaceService,
    private readonly toast: ToastService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const projectRef = params.get('projectRef');
      const teamRef = params.get('teamRef');
      const taskId = params.get('taskId');

      if (!projectRef || !teamRef || !taskId) {
        return;
      }

      this.projectRef = projectRef;
      this.teamRef = teamRef;
      this.taskId = taskId;
      this.loadTask();
    });
  }

  get canEditTaskSpace(): boolean {
    const role = this.project?.currentUserRole;
    return role === 'OWNER' || role === 'ADMIN' || role === 'MEMBER';
  }

  get canManageSubTasks(): boolean {
    return (this.bundle?.teamMembers ?? []).some(
      (member) => member.userId === this.currentUserId && member.role === 'LEADER'
    );
  }

  get canMoveSubTasks(): boolean {
    const role = this.project?.currentUserRole;
    if (!this.currentUserId || role === 'VIEWER') {
      return false;
    }

    return (this.bundle?.teamMembers ?? []).some((member) => member.userId === this.currentUserId);
  }

  get canSubmitAttachment(): boolean {
    return (
      this.canEditTaskSpace &&
      !!this.attachmentForm.fileName.trim() &&
      !!this.attachmentForm.fileUrl.trim() &&
      Number(this.attachmentForm.fileSize) > 0 &&
      !this.savingAttachment
    );
  }

  get assigneeOptions(): TeamMember[] {
    return this.bundle?.teamMembers ?? [];
  }

  loadTask(quiet = false): void {
    if (!quiet) {
      this.loading = true;
    }
    forkJoin({
      profile: this.authService.getProfile(),
      project: this.workspaceService.getProject(this.projectRef),
      bundle: this.workspaceService.loadTaskBundle(this.projectRef, this.teamRef, this.taskId)
    }).subscribe({
      next: ({ profile, project, bundle }) => {
        this.currentUserId = profile?.id ?? '';
        this.project = project;
        this.bundle = bundle;
        if (!this.timeEntryForm.date) {
          this.timeEntryForm.date = new Date().toISOString().slice(0, 10);
        }
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load task details.'));
        this.loading = false;
      }
    });
  }

  updateStatus(status: TaskStatus): void {
    if (!this.canEditTaskSpace || !this.bundle || this.bundle.task.status === status) {
      return;
    }

    this.workspaceService.updateTaskStatus(this.projectRef, this.teamRef, this.taskId, status).subscribe({
      next: () => this.loadTask(true),
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to update the task status.'))
    });
  }

  addComment(): void {
    if (!this.canEditTaskSpace || !this.commentForm.content.trim()) {
      this.commentError = 'Comment content is required.';
      this.toast.warning('Please write a comment before posting.');
      return;
    }

    this.commentError = '';
    this.workspaceService.createComment(this.projectRef, this.teamRef, this.taskId, this.commentForm.content.trim()).subscribe({
      next: () => {
        this.commentForm.content = '';
        this.commentError = '';
        this.loadTask(true);
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to add the comment.'))
    });
  }

  addAttachment(): void {
    this.attachmentErrors = {
      fileName: this.attachmentForm.fileName.trim() ? '' : 'File name is required.',
      fileUrl: this.attachmentForm.fileUrl.trim() ? '' : 'File URL is required.',
      fileSize: Number(this.attachmentForm.fileSize) > 0 ? '' : 'File size must be greater than 0.'
    };

    if (!this.canEditTaskSpace || this.attachmentErrors.fileName || this.attachmentErrors.fileUrl || this.attachmentErrors.fileSize || this.savingAttachment) {
      this.toast.warning('Please complete the attachment fields.');
      return;
    }

    this.savingAttachment = true;
    this.workspaceService
      .createAttachment(this.projectRef, this.teamRef, this.taskId, {
        fileName: this.attachmentForm.fileName.trim(),
        fileUrl: this.attachmentForm.fileUrl.trim(),
        fileSize: Number(this.attachmentForm.fileSize)
      })
      .subscribe({
        next: () => {
          this.attachmentForm.fileName = '';
          this.attachmentForm.fileUrl = '';
          this.attachmentForm.fileSize = 1;
          this.attachmentErrors = { fileName: '', fileUrl: '', fileSize: '' };
          this.showAttachmentForm = false;
          this.savingAttachment = false;
          this.loadTask(true);
        },
        error: (error: unknown) => {
          this.savingAttachment = false;
          this.toast.error(mapHttpError(error, 'Unable to add the attachment.'));
        }
      });
  }

  addSubTask(): void {
    this.subTaskTitleError = this.subTaskForm.title.trim() ? '' : 'Subtask title is required.';
    if (!this.canManageSubTasks || this.subTaskTitleError || this.savingSubTask) {
      this.toast.warning(this.subTaskTitleError || 'You cannot create subtasks in this team.');
      return;
    }

    this.savingSubTask = true;
    this.workspaceService
      .createSubTask(this.projectRef, this.teamRef, this.taskId, {
        title: this.subTaskForm.title.trim(),
        assigneeId: this.subTaskForm.assigneeId || null
      })
      .subscribe({
        next: () => {
          this.subTaskForm.title = '';
          this.subTaskForm.assigneeId = '';
          this.subTaskTitleError = '';
          this.showSubTaskForm = false;
          this.savingSubTask = false;
          this.loadTask(true);
        },
        error: (error: unknown) => {
          this.savingSubTask = false;
          this.toast.error(mapHttpError(error, 'Unable to create the subtask.'));
          this.loadTask(true);
        }
      });
  }

  toggleSubTask(subTaskId: string, nextValue: boolean, event: Event): void {
    const target = event.target as HTMLInputElement | null;
    if (!this.canMoveSubTasks) {
      if (target) {
        target.checked = !nextValue;
      }
      return;
    }

    this.workspaceService
      .updateSubTask(this.projectRef, this.teamRef, this.taskId, subTaskId, {
        isDone: nextValue
      })
      .subscribe({
        next: () => this.loadTask(true),
        error: (error: unknown) => {
          if (target) {
            target.checked = !nextValue;
          }
          this.toast.error(mapHttpError(error, 'Unable to update the subtask.'));
          this.loadTask(true);
        }
      });
  }

  addTimeEntry(): void {
    this.timeEntryErrors = {
      durationMinutes: Number(this.timeEntryForm.durationMinutes) > 0 ? '' : 'Minutes must be greater than 0.',
      date: this.timeEntryForm.date ? '' : 'Date is required.'
    };

    if (!this.canEditTaskSpace || this.timeEntryErrors.durationMinutes || this.timeEntryErrors.date) {
      this.toast.warning('Please complete the time entry fields.');
      return;
    }

    this.workspaceService
      .createTimeEntry(this.projectRef, this.teamRef, this.taskId, {
        durationMinutes: this.timeEntryForm.durationMinutes,
        date: this.timeEntryForm.date,
        description: this.timeEntryForm.description.trim() || undefined
      })
      .subscribe({
        next: () => {
          this.timeEntryForm.durationMinutes = 30;
          this.timeEntryForm.description = '';
          this.timeEntryErrors = { durationMinutes: '', date: '' };
          this.showTimeForm = false;
          this.loadTask(true);
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to log time for this task.'))
      });
  }

  assigneeLabel(teamMemberId: string | null): string {
    if (!teamMemberId) {
      return 'Unassigned';
    }

    const member = this.bundle?.teamMembers.find((entry) => entry.id === teamMemberId);
    return member ? member.memberName : teamMemberId;
  }

  actorLabel(userId: string | null): string {
    if (!userId) {
      return 'Unknown member';
    }

    const member = this.bundle?.projectMembers.find((entry) => entry.userId === userId);
    return member ? member.memberName : userId;
  }
}
