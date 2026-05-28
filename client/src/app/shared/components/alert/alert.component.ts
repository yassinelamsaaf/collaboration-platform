import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertComponent {
  @Input() type: 'success' | 'error' = 'success';
  @Input() message = '';
}
