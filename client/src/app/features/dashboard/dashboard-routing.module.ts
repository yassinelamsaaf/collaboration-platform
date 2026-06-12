import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard.component';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { ProjectsPageComponent } from './pages/projects-page/projects-page.component';
import { ProjectDashboardPageComponent } from './pages/project-dashboard-page/project-dashboard-page.component';
import { KanbanPageComponent } from './pages/kanban-page/kanban-page.component';
import { ProjectMembersPageComponent } from './pages/project-members-page/project-members-page.component';
import { TasksPageComponent } from './pages/tasks-page/tasks-page.component';
import { ActivityPageComponent } from './pages/activity-page/activity-page.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    children: [
      { path: '', component: DashboardPageComponent },
      { path: 'projects', component: ProjectsPageComponent },
      { path: 'tasks', component: TasksPageComponent },
      { path: 'activity', component: ActivityPageComponent },
      { path: 'projects/:projectRef/dashboard', component: ProjectDashboardPageComponent },
      { path: 'projects/:projectRef/board', component: KanbanPageComponent },
      { path: 'projects/:projectRef/members', component: ProjectMembersPageComponent },
      { path: 'projects/:projectRef/activity', component: DashboardPageComponent },
      { path: 'projects/:projectRef/settings', component: DashboardPageComponent },
      { path: 'notifications', component: DashboardPageComponent },
      { path: 'settings', component: DashboardPageComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {}
