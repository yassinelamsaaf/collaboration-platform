import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle,
  ChevronDown,
  Clock3,
  Globe,
  Github,
  Layers,
  LayoutDashboard,
  LogIn,
  LogOut,
  Mail,
  Menu,
  Settings,
  ShieldCheck,
  X,
  UserPlus,
  LucideAngularModule
} from 'lucide-angular';

import { AlertComponent } from './components/alert/alert.component';

const lucideIcons = {
  ArrowLeft,
  ArrowRight,
  CheckCircle,
  ChevronDown,
  Clock3,
  Globe,
  Github,
  Layers,
  LayoutDashboard,
  LogIn,
  LogOut,
  Mail,
  Menu,
  Settings,
  ShieldCheck,
  X,
  UserPlus
};

@NgModule({
  declarations: [AlertComponent],
  imports: [CommonModule, LucideAngularModule.pick(lucideIcons)],
  exports: [CommonModule, AlertComponent, LucideAngularModule]
})
export class SharedModule {}
