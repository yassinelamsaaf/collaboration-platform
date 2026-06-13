import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { InvitationAccessComponent } from './pages/invitation-access/invitation-access.component';

const routes: Routes = [{ path: '', component: InvitationAccessComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvitationsRoutingModule {}
