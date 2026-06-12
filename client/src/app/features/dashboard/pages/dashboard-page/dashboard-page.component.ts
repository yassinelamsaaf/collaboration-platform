import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProjectService } from '../../../../core/services/project.service';
import { ProjectResponse } from '../../../../shared/models/project.models';

@Component({
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPageComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  projects: ProjectResponse[] = [];

  ngOnInit(): void {
    this.projectService.list({ page: 0, size: 10 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (res) => {
        this.projects = res.content;
        this.cdr.markForCheck();
      }});
  }

  getMemberInitials(id: string): string {
    return id.slice(0, 2).toUpperCase();
  }
}
