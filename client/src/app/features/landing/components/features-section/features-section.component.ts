import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-features-section',
  templateUrl: './features-section.component.html',
  styleUrl: './features-section.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeaturesSectionComponent {}
