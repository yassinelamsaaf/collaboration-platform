import { Component, HostListener } from '@angular/core';

import { WorkspaceService } from '@features/dashboard/services/workspace.service';
import { NotificationItem } from '@shared/models/workspace.models';

const PAGE_SIZE = 6;

@Component({
  selector: 'app-notification-panel',
  templateUrl: './notification-panel.component.html',
  styleUrl: './notification-panel.component.scss',
  standalone: false
})
export class NotificationPanelComponent {
  notifications: NotificationItem[] = [];
  unreadCount = 0;
  isOpen = false;
  hasMore = false;
  private currentPage = 0;
  private loaded = false;
  private loadingMore = false;

  constructor(private readonly workspaceService: WorkspaceService) {
    this.workspaceService.countUnreadNotifications().subscribe({
      next: (count) => (this.unreadCount = count),
      error: () => (this.unreadCount = 0)
    });
  }

  toggle(event: MouseEvent): void {
    event.stopPropagation();
    this.isOpen = !this.isOpen;
    if (this.isOpen && !this.loaded) {
      this.load();
    }
  }

  close(): void {
    this.isOpen = false;
  }

  loadMore(): void {
    if (this.loadingMore) {
      return;
    }
    this.loadingMore = true;
    this.currentPage++;
    this.workspaceService.listNotifications(this.currentPage + 1, PAGE_SIZE).subscribe({
      next: (page) => {
        this.notifications.push(...page.content);
        this.hasMore = page.totalElements > this.notifications.length;
        this.loadingMore = false;
      },
      error: () => {
        this.loadingMore = false;
      }
    });
  }

  markAsRead(notificationId: string): void {
    this.workspaceService.markNotificationAsRead(notificationId).subscribe({
      next: () => {
        const n = this.notifications.find((x) => x.id === notificationId);
        if (n) {
          n.isRead = true;
        }
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      }
    });
  }

  markAllAsRead(): void {
    this.workspaceService.markAllNotificationsAsRead().subscribe({
      next: () => {
        this.notifications.forEach((n) => (n.isRead = true));
        this.unreadCount = 0;
      }
    });
  }

  notificationTime(item: NotificationItem): string {
    return new Date(item.createdAt).toLocaleString();
  }

  private load(): void {
    this.currentPage = 0;
    this.workspaceService.listNotifications(1, PAGE_SIZE).subscribe({
      next: (page) => {
        this.notifications = page.content;
        this.hasMore = page.totalElements > page.content.length;
        this.loaded = true;
      },
      error: () => {
        this.notifications = [];
        this.hasMore = false;
        this.loaded = true;
      }
    });
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.isOpen = false;
  }
}
