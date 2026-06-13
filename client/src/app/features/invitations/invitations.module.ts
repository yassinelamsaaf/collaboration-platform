import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from '@shared/shared.module';
import { InvitationsRoutingModule } from './invitations-routing.module';
import { InvitationAccessComponent } from './pages/invitation-access/invitation-access.component';

@NgModule({
  declarations: [InvitationAccessComponent],
  imports: [SharedModule, RouterModule, InvitationsRoutingModule]
})
export class InvitationsModule {}
