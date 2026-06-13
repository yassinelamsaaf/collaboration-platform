import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-contact-page',
  templateUrl: './contact-page.component.html',
  styleUrl: './contact-page.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContactPageComponent {
  readonly supportEmail = 'support@collaboration-platform.local';
  readonly currentYear = new Date().getFullYear();
}
