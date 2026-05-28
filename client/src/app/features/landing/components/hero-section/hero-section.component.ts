import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-hero-section',
  templateUrl: './hero-section.component.html',
  styleUrl: './hero-section.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeroSectionComponent {}
