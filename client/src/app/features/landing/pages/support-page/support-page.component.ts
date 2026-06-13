import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-support-page',
  templateUrl: './support-page.component.html',
  styleUrl: './support-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SupportPageComponent {
  readonly supportEmail = 'support@collaboration-platform.local';
}
