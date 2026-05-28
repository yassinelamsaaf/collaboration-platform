import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-pricing-section',
  templateUrl: './pricing-section.component.html',
  styleUrl: './pricing-section.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PricingSectionComponent {}
