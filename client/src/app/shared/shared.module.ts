import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideArrowLeft,
  LucideArrowRight,
  LucideCheckCircle,
  LucideChevronDown,
  LucideClock3,
  LucideDynamicIcon,
  LucideEye,
  LucideEyeOff,
  LucideGlobe,
  LucideLayers,
  LucideLayoutDashboard,
  LucideLogIn,
  LucideLogOut,
  LucideMail,
  LucideMenu,
  LucideSettings,
  LucideShieldCheck,
  LucideUserPlus,
  LucideX,
  provideLucideIcons
} from '@lucide/angular';
import type { LucideIconData } from '@lucide/angular';

import { AlertComponent } from './components/alert/alert.component';
import { LucideIconComponent } from './components/lucide-icon/lucide-icon.component';

const githubIcon: LucideIconData = {
  name: 'github',
  node: [
    [
      'path',
      {
        d: 'M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.4 5.4 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4'
      }
    ],
    ['path', { d: 'M9 18c-4.51 2-5-2-7-2' }]
  ]
};

@NgModule({
  declarations: [AlertComponent, LucideIconComponent],
  imports: [CommonModule, LucideDynamicIcon],
  exports: [CommonModule, AlertComponent, LucideIconComponent],
  providers: [
    provideLucideIcons(
      LucideArrowLeft,
      LucideArrowRight,
      LucideCheckCircle,
      LucideChevronDown,
      LucideClock3,
      LucideEye,
      LucideEyeOff,
      LucideGlobe,
      LucideLayers,
      LucideLayoutDashboard,
      LucideLogIn,
      LucideLogOut,
      LucideMail,
      LucideMenu,
      LucideSettings,
      LucideShieldCheck,
      LucideUserPlus,
      LucideX,
      githubIcon
    )
  ]
})
export class SharedModule {}