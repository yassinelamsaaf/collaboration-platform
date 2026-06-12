import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardComponent } from './dashboard.component';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { ProjectsPageComponent } from './pages/projects-page/projects-page.component';
import { ProjectDashboardPageComponent } from './pages/project-dashboard-page/project-dashboard-page.component';
import { KanbanPageComponent } from './pages/kanban-page/kanban-page.component';
import { ProjectMembersPageComponent } from './pages/project-members-page/project-members-page.component';
import { TasksPageComponent } from './pages/tasks-page/tasks-page.component';
import { ActivityPageComponent } from './pages/activity-page/activity-page.component';

@NgModule({
  declarations: [
    DashboardComponent,
    DashboardPageComponent,
    ProjectsPageComponent,
    ProjectDashboardPageComponent,
    KanbanPageComponent,
    ProjectMembersPageComponent,
    TasksPageComponent,
    ActivityPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    DashboardRoutingModule
  ]
})
export class DashboardModule {}
