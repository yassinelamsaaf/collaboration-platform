import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { WorkspaceShellComponent } from './components/workspace-shell/workspace-shell.component';
import { DashboardComponent } from './dashboard.component';
import { GlobalSearchComponent } from './pages/global-search/global-search.component';
import { MyTasksComponent } from './pages/my-tasks/my-tasks.component';
import { ProjectKanbanComponent } from './pages/project-kanban/project-kanban.component';
import { ProjectOverviewComponent } from './pages/project-overview/project-overview.component';
import { ProjectTeamsComponent } from './pages/project-teams/project-teams.component';
import { ProjectsPageComponent } from './pages/projects-page/projects-page.component';
import { TaskDetailComponent } from './pages/task-detail/task-detail.component';

const routes: Routes = [
  {
    path: '',
    component: WorkspaceShellComponent,
    children: [
      { path: '', component: DashboardComponent },
      { path: 'my-tasks', component: MyTasksComponent },
      { path: 'projects', component: ProjectsPageComponent },
      { path: 'projects/:projectRef', component: ProjectOverviewComponent },
      { path: 'projects/:projectRef/teams', component: ProjectTeamsComponent },
      { path: 'projects/:projectRef/kanban', component: ProjectKanbanComponent },
      { path: 'projects/:projectRef/tasks/:teamRef/:taskId', component: TaskDetailComponent },
      { path: 'search', component: GlobalSearchComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {}
