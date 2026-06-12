import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProjectService } from '../../../../core/services/project.service';
import { ProjectResponse } from '../../../../shared/models/project.models';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrl: './projects-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectsPageComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  projects: ProjectResponse[] = [];
  gridView = true;
  search = '';

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.list({ page: 0, size: 50 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.projects = res.content;
        this.cdr.markForCheck();
      }});
  }

  get filtered(): ProjectResponse[] {
    if (!this.search) return this.projects;
    const q = this.search.toLowerCase();
    return this.projects.filter(p => p.name.toLowerCase().includes(q) || (p.description && p.description.toLowerCase().includes(q)));
  }

  toggleView(): void {
    this.gridView = !this.gridView;
  }

  getProjectInitials(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  getProjectIconBg(name: string): string {
    const colors = ['#2563eb', '#3b2f1e', '#c0392b', '#059669', '#d97706', '#7c3aed'];
    const idx = name.length % colors.length;
    return colors[idx];
  }
}
