import { NgModule } from '@angular/core';

import { SharedModule } from '../../shared/shared.module';
import { LandingRoutingModule } from './landing-routing.module';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { HeroSectionComponent } from './components/hero-section/hero-section.component';
import { FeaturesSectionComponent } from './components/features-section/features-section.component';
import { WorkflowSectionComponent } from './components/workflow-section/workflow-section.component';
import { PricingSectionComponent } from './components/pricing-section/pricing-section.component';
import { ContactSectionComponent } from './components/contact-section/contact-section.component';
import { FooterComponent } from './components/footer/footer.component';

@NgModule({
  declarations: [
    LandingPageComponent,
    NavbarComponent,
    HeroSectionComponent,
    FeaturesSectionComponent,
    WorkflowSectionComponent,
    PricingSectionComponent,
    ContactSectionComponent,
    FooterComponent
  ],
  imports: [SharedModule, LandingRoutingModule]
})
export class LandingModule {}
