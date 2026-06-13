import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Project, SearchResultItem } from '@shared/models/workspace.models';
import { WorkspaceService } from '@features/dashboard/services/workspace.service';

@Component({
  selector: 'app-global-search',
  templateUrl: './global-search.component.html',
  styleUrl: './global-search.component.scss',
  standalone: false
})
export class GlobalSearchComponent implements OnInit {
  projects: Project[] = [];
  results: SearchResultItem[] = [];
  selectedProjectRef = '';
  query = '';
  loading = true;
  searching = false;

  constructor(
    private readonly workspaceService: WorkspaceService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const applyParams = () => {
      this.route.queryParamMap.subscribe((params) => {
        this.query = params.get('q') ?? '';
        this.selectedProjectRef = params.get('project') ?? this.projects[0]?.slug ?? this.projects[0]?.id ?? '';
        this.loading = false;
        if (this.query && this.selectedProjectRef) {
          this.runSearch();
        }
      });
    };

    this.workspaceService.listProjects(1, 100).subscribe({
      next: (page) => {
        this.projects = page.content;
        applyParams();
      },
      error: () => {
        this.projects = [];
        applyParams();
      }
    });
  }

  submit(): void {
    this.router.navigate(['/dashboard/search'], {
      queryParams: {
        q: this.query.trim() || null,
        project: this.selectedProjectRef || null
      }
    });
  }

  runSearch(): void {
    if (!this.query.trim() || !this.selectedProjectRef) {
      this.results = [];
      return;
    }

    this.searching = true;
    this.workspaceService.searchProjectContent(this.selectedProjectRef, this.query).subscribe({
      next: (results) => {
        this.results = results;
        this.searching = false;
      },
      error: () => {
        this.searching = false;
      }
    });
  }
}
