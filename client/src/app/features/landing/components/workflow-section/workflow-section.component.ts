import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-workflow-section',
  templateUrl: './workflow-section.component.html',
  styleUrl: './workflow-section.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowSectionComponent {}
