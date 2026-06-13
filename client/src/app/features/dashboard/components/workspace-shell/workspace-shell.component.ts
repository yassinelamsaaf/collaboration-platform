import { Component, HostListener, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

import { AuthService } from '@core/services/auth.service';
import { ToastService } from '@core/services/toast.service';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { Project } from '@shared/models/workspace.models';
import { mapHttpError } from '@shared/utils/error-mapper';

@Component({
  selector: 'app-workspace-shell',
  templateUrl: './workspace-shell.component.html',
  styleUrl: './workspace-shell.component.scss',
  standalone: false
})
export class WorkspaceShellComponent implements OnInit {
  projects: Project[] = [];
  profileName = 'Workspace';
  profileEmail = '';
  searchQuery = '';
  sidebarOpen = false;
  sidebarCollapsed = false;
  currentProjectRef = '';
  isCompactViewport = false;

  constructor(
    private readonly authService: AuthService,
    private readonly workspaceService: WorkspaceService,
    private readonly router: Router,
    private readonly toast: ToastService
  ) {
    const saved = localStorage.getItem('workspace:sidebarCollapsed');
    if (saved === 'true') {
      this.sidebarCollapsed = true;
    }
  }

  ngOnInit(): void {
    this.syncViewport();
    this.syncRouteContext(this.router.url);

    this.authService.getProfile().subscribe((profile) => {
      if (!profile) {
        return;
      }

      this.profileName = profile.username ?? 'Workspace';
      this.profileEmail = profile.email;
    });

    this.workspaceService.listProjects(1, 12).subscribe({
      next: (page) => {
        this.projects = page.content;
      },
      error: () => {
        this.projects = [];
      }
    });

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.syncRouteContext(event.urlAfterRedirects);
        this.closeSidebar();
      });
  }

  toggleCollapse(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    localStorage.setItem('workspace:sidebarCollapsed', String(this.sidebarCollapsed));
  }

  submitSearch(): void {
    const query = this.searchQuery.trim();
    if (!query) {
      return;
    }

    this.router.navigate(['/dashboard/search'], { queryParams: { q: query } });
    this.closeSidebar();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: (error: unknown) => {
        this.toast.error(mapHttpError(error, 'Unable to sign out cleanly.'));
      }
    });
  }

  firstLetter(value: string): string {
    return value ? value.charAt(0).toUpperCase() : 'W';
  }

  isCurrentProject(project: Project): boolean {
    return !!this.currentProjectRef && (project.slug === this.currentProjectRef || project.id === this.currentProjectRef);
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  get showProjectShortcuts(): boolean {
    return !!this.currentProjectRef;
  }

  @HostListener('window:resize')
  onResize(): void {
    this.syncViewport();
  }

  private syncViewport(): void {
    this.isCompactViewport = typeof window !== 'undefined' ? window.innerWidth <= 1080 : false;
    if (!this.isCompactViewport) {
      this.sidebarOpen = false;
    }
  }

  private syncRouteContext(url: string): void {
    const segments = url.split('?')[0].split('/').filter(Boolean);
    if (segments[0] === 'dashboard' && segments[1] === 'projects' && segments[2]) {
      this.currentProjectRef = decodeURIComponent(segments[2]);
      return;
    }

    this.currentProjectRef = '';
  }
}
