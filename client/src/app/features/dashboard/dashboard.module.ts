import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { DashboardComponent } from './dashboard.component';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { SharedModule } from '@shared/shared.module';
import { NotificationPanelComponent } from './components/notification-panel/notification-panel.component';
import { WorkspaceShellComponent } from './components/workspace-shell/workspace-shell.component';
import { GlobalSearchComponent } from './pages/global-search/global-search.component';
import { MyTasksComponent } from './pages/my-tasks/my-tasks.component';
import { ProjectKanbanComponent } from './pages/project-kanban/project-kanban.component';
import { ProjectMembersComponent } from './pages/project-members/project-members.component';
import { ProjectOverviewComponent } from './pages/project-overview/project-overview.component';
import { ProjectTeamsComponent } from './pages/project-teams/project-teams.component';
import { SettingsPageComponent } from './pages/settings/settings-page.component';
import { ProjectsPageComponent } from './pages/projects-page/projects-page.component';
import { TaskDetailComponent } from './pages/task-detail/task-detail.component';

@NgModule({
  declarations: [
    DashboardComponent,
    MyTasksComponent,
    NotificationPanelComponent,
    WorkspaceShellComponent,
    ProjectsPageComponent,
    ProjectOverviewComponent,
    ProjectMembersComponent,
    ProjectTeamsComponent,
    ProjectKanbanComponent,
    TaskDetailComponent,
    GlobalSearchComponent,
    SettingsPageComponent
  ],
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule, DashboardRoutingModule]
})
export class DashboardModule {}
