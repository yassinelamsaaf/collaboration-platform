import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ToastService } from '@core/services/toast.service';
import { Project } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrl: './projects-page.component.scss',
  standalone: false
})
export class ProjectsPageComponent implements OnInit {
  projects: Project[] = [];
  loading = true;
  creating = false;
  form = {
    name: '',
    description: ''
  };

  constructor(
    private readonly workspaceService: WorkspaceService,
    private readonly toast: ToastService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.workspaceService.listProjects(1, 50).subscribe({
      next: (page) => {
        this.projects = page.content;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to load projects.'));
        this.loading = false;
      }
    });
  }

  submit(): void {
    if (!this.form.name.trim()) {
      return;
    }

    this.creating = true;
    this.workspaceService
      .createProject({
        name: this.form.name.trim(),
        description: this.form.description.trim() || undefined
      })
      .subscribe({
        next: (project) => {
          this.form = { name: '', description: '' };
          this.creating = false;
          this.toast.success('Project created. You can now define teams and invite members.');
          void this.router.navigate(['/dashboard/projects', project.slug || project.id, 'teams']);
        },
        error: (error: unknown) => {
          this.toast.error(mapHttpError(error, 'Unable to create the project.'));
          this.creating = false;
        }
      });
  }

  archive(project: Project): void {
    if (project.status === 'ARCHIVED') {
      return;
    }

    this.workspaceService.archiveProject(project.slug || project.id).subscribe({
      next: () => {
        this.toast.success(`Project "${project.name}" archived.`);
        this.reload();
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to archive the project.'));
      }
    });
  }
}
