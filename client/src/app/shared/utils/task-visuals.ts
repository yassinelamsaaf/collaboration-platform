import { Priority, PriorityBreakdown, StatusBreakdown, Task, TaskStatus } from '@shared/models/workspace.models';

export interface DonutSegment {
  status: TaskStatus;
  label: string;
  value: number;
  color: string;
  dash: number;
  offset: number;
}

export interface PriorityBar {
  priority: Priority;
  label: string;
  value: number;
  percent: number;
  color: string;
}

export const STATUS_META: Record<TaskStatus, { label: string; color: string }> = {
  TODO: { label: 'To do', color: '#94a3b8' },
  IN_PROGRESS: { label: 'In progress', color: '#2563eb' },
  IN_REVIEW: { label: 'In review', color: '#a855f7' },
  DONE: { label: 'Done', color: '#0f766e' }
};

export const PRIORITY_META: Record<Priority, { label: string; color: string }> = {
  URGENT: { label: 'Urgent', color: '#b91c1c' },
  HIGH: { label: 'High', color: '#ea580c' },
  MEDIUM: { label: 'Medium', color: '#2563eb' },
  LOW: { label: 'Low', color: '#0f766e' }
};

export const DONUT_RADIUS = 52;
export const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_RADIUS;

export function emptyStatusBreakdown(): StatusBreakdown {
  return { TODO: 0, IN_PROGRESS: 0, IN_REVIEW: 0, DONE: 0 };
}

export function emptyPriorityBreakdown(): PriorityBreakdown {
  return { LOW: 0, MEDIUM: 0, HIGH: 0, URGENT: 0 };
}

export function statusBreakdownOf(tasks: Task[]): StatusBreakdown {
  const breakdown = emptyStatusBreakdown();
  for (const task of tasks) {
    breakdown[task.status] = (breakdown[task.status] ?? 0) + 1;
  }
  return breakdown;
}

export function priorityBreakdownOf(tasks: Task[]): PriorityBreakdown {
  const breakdown = emptyPriorityBreakdown();
  for (const task of tasks) {
    breakdown[task.priority] = (breakdown[task.priority] ?? 0) + 1;
  }
  return breakdown;
}

export function donutSegmentsOf(breakdown: StatusBreakdown, circumference = DONUT_CIRCUMFERENCE): DonutSegment[] {
  const total = (Object.values(breakdown) as number[]).reduce((sum, value) => sum + value, 0);
  if (!total) {
    return [];
  }

  let offset = 0;
  return (Object.keys(STATUS_META) as TaskStatus[])
    .map((status) => {
      const value = breakdown[status] ?? 0;
      const dash = (value / total) * circumference;
      const segment: DonutSegment = { status, ...STATUS_META[status], value, dash, offset };
      offset += dash;
      return segment;
    })
    .filter((segment) => segment.value > 0);
}

export function priorityBarsOf(breakdown: PriorityBreakdown): PriorityBar[] {
  const max = Math.max(1, ...(Object.values(breakdown) as number[]));
  return (Object.keys(PRIORITY_META) as Priority[]).map((priority) => ({
    priority,
    label: PRIORITY_META[priority].label,
    value: breakdown[priority] ?? 0,
    percent: Math.round(((breakdown[priority] ?? 0) / max) * 100),
    color: PRIORITY_META[priority].color
  }));
}

export function completionRateOf(breakdown: StatusBreakdown): number {
  const total = (Object.values(breakdown) as number[]).reduce((sum, value) => sum + value, 0);
  return total ? Math.round((breakdown.DONE / total) * 100) : 0;
}
