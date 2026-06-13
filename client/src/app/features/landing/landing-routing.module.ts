import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ContactPageComponent } from './pages/contact-page/contact-page.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { PrivacyPageComponent } from './pages/privacy-page/privacy-page.component';
import { SupportPageComponent } from './pages/support-page/support-page.component';
import { TermsPageComponent } from './pages/terms-page/terms-page.component';

const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'contact', component: ContactPageComponent },
  { path: 'support', component: SupportPageComponent },
  { path: 'privacy', component: PrivacyPageComponent },
  { path: 'terms', component: TermsPageComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LandingRoutingModule {}
