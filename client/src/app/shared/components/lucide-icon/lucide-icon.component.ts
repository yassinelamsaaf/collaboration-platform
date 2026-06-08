import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'lucide-icon',
  template: '<svg [lucideIcon]="name"></svg>',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LucideIconComponent {
  @Input({ required: true }) name = '';
}
