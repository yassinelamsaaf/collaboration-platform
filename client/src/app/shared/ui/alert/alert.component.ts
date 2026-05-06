import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [NgIf],
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertComponent {
  @Input() type: 'success' | 'error' = 'success';
  @Input() message = '';
}
