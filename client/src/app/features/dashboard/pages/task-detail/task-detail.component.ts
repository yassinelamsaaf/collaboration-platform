import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';

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
  project: Project | null = null;
  bundle: TaskBundle | null = null;
  loading = true;
  showSubTaskForm = false;
  showAttachmentForm = false;
  showTimeForm = false;

  readonly commentForm = {
    content: ''
  };

  readonly attachmentForm = {
    fileName: '',
    fileUrl: '',
    fileSize: 0
  };

  readonly subTaskForm = {
    title: '',
    assigneeId: ''
  };

  readonly timeEntryForm = {
    durationMinutes: 30,
    date: '',
    description: ''
  };

  constructor(
    private readonly route: ActivatedRoute,
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

  get assigneeOptions(): TeamMember[] {
    return this.bundle?.teamMembers ?? [];
  }

  loadTask(quiet = false): void {
    if (!quiet) {
      this.loading = true;
    }
    forkJoin({
      project: this.workspaceService.getProject(this.projectRef),
      bundle: this.workspaceService.loadTaskBundle(this.projectRef, this.teamRef, this.taskId)
    }).subscribe({
      next: ({ project, bundle }) => {
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
      return;
    }

    this.workspaceService.createComment(this.projectRef, this.teamRef, this.taskId, this.commentForm.content.trim()).subscribe({
      next: () => {
        this.commentForm.content = '';
        this.loadTask(true);
      },
      error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to add the comment.'))
    });
  }

  addAttachment(): void {
    if (!this.canEditTaskSpace || !this.attachmentForm.fileName.trim() || !this.attachmentForm.fileUrl.trim()) {
      return;
    }

    this.workspaceService
      .createAttachment(this.projectRef, this.teamRef, this.taskId, {
        fileName: this.attachmentForm.fileName.trim(),
        fileUrl: this.attachmentForm.fileUrl.trim(),
        fileSize: this.attachmentForm.fileSize || 0
      })
      .subscribe({
        next: () => {
          this.attachmentForm.fileName = '';
          this.attachmentForm.fileUrl = '';
          this.attachmentForm.fileSize = 0;
          this.showAttachmentForm = false;
          this.loadTask(true);
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to add the attachment.'))
      });
  }

  addSubTask(): void {
    if (!this.canEditTaskSpace || !this.subTaskForm.title.trim()) {
      return;
    }

    this.workspaceService
      .createSubTask(this.projectRef, this.teamRef, this.taskId, {
        title: this.subTaskForm.title.trim(),
        assigneeId: this.subTaskForm.assigneeId || null
      })
      .subscribe({
        next: () => {
          this.subTaskForm.title = '';
          this.subTaskForm.assigneeId = '';
          this.showSubTaskForm = false;
          this.loadTask(true);
        },
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to create the subtask.'))
      });
  }

  toggleSubTask(subTaskId: string, nextValue: boolean): void {
    if (!this.canEditTaskSpace) {
      return;
    }

    this.workspaceService
      .updateSubTask(this.projectRef, this.teamRef, this.taskId, subTaskId, {
        isDone: nextValue
      })
      .subscribe({
        next: () => this.loadTask(true),
        error: (error: unknown) => this.toast.error(mapHttpError(error, 'Unable to update the subtask.'))
      });
  }

  addTimeEntry(): void {
    if (!this.canEditTaskSpace || !this.timeEntryForm.date || this.timeEntryForm.durationMinutes <= 0) {
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
