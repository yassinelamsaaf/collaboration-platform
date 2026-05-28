import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
}
