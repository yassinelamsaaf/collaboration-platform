package com.inpt.collaborationplatform.workmanagement.scheduler;

import com.inpt.collaborationplatform.shared.event.DeadlineApproachingEvent;
import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.workmanagement.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlineScheduler {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkApproachingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        var tasks = taskRepository.findByDueDate(tomorrow);

        for (Task task : tasks) {
            eventPublisher.publishEvent(new DeadlineApproachingEvent(
                    task.getId(),
                    task.getTitle(),
                    task.getProject().getId(),
                    task.getTeam().getId(),
                    task.getAssigneeId()
            ));
        }

        if (!tasks.isEmpty()) {
            log.info("Published {} deadline-approaching events", tasks.size());
        }
    }
}
