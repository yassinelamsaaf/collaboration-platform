import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-landing-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FooterComponent {}
