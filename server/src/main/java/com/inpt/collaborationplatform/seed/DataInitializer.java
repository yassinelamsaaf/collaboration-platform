package com.inpt.collaborationplatform.seed;

import com.inpt.collaborationplatform.Identity.entity.Role;
import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.Identity.repository.UserRepository;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.project.repository.ProjectRepository;
import com.inpt.collaborationplatform.projects.shared.SlugGenerator;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import com.inpt.collaborationplatform.projects.team.entity.TeamRole;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.projects.team.repository.TeamRepository;
import com.inpt.collaborationplatform.tasks.attachment.entity.Attachment;
import com.inpt.collaborationplatform.tasks.attachment.repository.AttachmentRepository;
import com.inpt.collaborationplatform.tasks.comment.entity.Comment;
import com.inpt.collaborationplatform.tasks.comment.repository.CommentRepository;
import com.inpt.collaborationplatform.tasks.label.entity.Label;
import com.inpt.collaborationplatform.tasks.label.repository.LabelRepository;
import com.inpt.collaborationplatform.tasks.subtask.entity.SubTask;
import com.inpt.collaborationplatform.tasks.subtask.repository.SubTaskRepository;
import com.inpt.collaborationplatform.tasks.task.entity.Priority;
import com.inpt.collaborationplatform.tasks.task.entity.Task;
import com.inpt.collaborationplatform.tasks.task.entity.TaskStatus;
import com.inpt.collaborationplatform.tasks.task.repository.TaskRepository;
import com.inpt.collaborationplatform.tasks.timeentry.entity.TimeEntry;
import com.inpt.collaborationplatform.tasks.timeentry.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlugGenerator slugGenerator;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed data already exists, skipping initialization");
            return;
        }

        log.info("No users found — creating seed data...");

        // =====================================================
        // USERS (2: ADMIN + regular USER)
        // =====================================================
        User admin = userRepository.save(User.builder()
                .email("admin@test.com")
                .username("admin")
                .password(passwordEncoder.encode("Admin123!"))
                .enabled(true)
                .role(Role.ADMIN)
                .build());

        User alice = userRepository.save(User.builder()
                .email("alice@test.com")
                .username("alice")
                .password(passwordEncoder.encode("Alice123!"))
                .enabled(true)
                .role(Role.USER)
                .build());

        // =====================================================
        // PROJECTS (2)
        // =====================================================
        Project demoProject = projectRepository.save(Project.builder()
                .name("Demo Project")
                .slug(slugGenerator.uniqueSlug("Demo Project", slug -> projectRepository.existsBySlug(slug)))
                .description("A sample project for testing the Work Management module")
                .createdByUserId(admin.getId())
                .build());

        Project mobileApp = projectRepository.save(Project.builder()
                .name("Mobile App")
                .slug(slugGenerator.uniqueSlug("Mobile App", slug -> projectRepository.existsBySlug(slug)))
                .description("Cross-platform mobile application built with Flutter")
                .createdByUserId(alice.getId())
                .build());

        // =====================================================
        // PROJECT MEMBERS (3: OWNER on each, MEMBER cross-project)
        // =====================================================
        projectMemberRepository.save(ProjectMember.builder()
                .project(demoProject)
                .userId(admin.getId())
                .role(ProjectRole.OWNER)
                .build());

        projectMemberRepository.save(ProjectMember.builder()
                .project(mobileApp)
                .userId(alice.getId())
                .role(ProjectRole.OWNER)
                .build());

        projectMemberRepository.save(ProjectMember.builder()
                .project(demoProject)
                .userId(alice.getId())
                .role(ProjectRole.MEMBER)
                .build());

        // =====================================================
        // TEAMS (3: Engineering, Design in Demo; Mobile Team in Mobile App)
        // =====================================================
        Team engineering = teamRepository.save(Team.builder()
                .project(demoProject)
                .name("Engineering")
                .slug(slugGenerator.uniqueSlug("Engineering",
                        slug -> teamRepository.existsByProject_IdAndSlug(demoProject.getId(), slug)))
                .description("The core engineering team")
                .createdByUserId(admin.getId())
                .build());

        Team design = teamRepository.save(Team.builder()
                .project(demoProject)
                .name("Design")
                .slug(slugGenerator.uniqueSlug("Design",
                        slug -> teamRepository.existsByProject_IdAndSlug(demoProject.getId(), slug)))
                .description("UX and visual design team")
                .createdByUserId(admin.getId())
                .build());

        Team mobileTeam = teamRepository.save(Team.builder()
                .project(mobileApp)
                .name("Mobile Team")
                .slug(slugGenerator.uniqueSlug("Mobile Team",
                        slug -> teamRepository.existsByProject_IdAndSlug(mobileApp.getId(), slug)))
                .description("Mobile development squad")
                .createdByUserId(alice.getId())
                .build());

        // =====================================================
        // TEAM MEMBERS (6: each team has LEADER + MEMBER)
        // =====================================================
        // Engineering: admin (LEADER), alice (MEMBER)
        TeamMember adminEng = teamMemberRepository.save(TeamMember.builder()
                .team(engineering)
                .userId(admin.getId())
                .role(TeamRole.LEADER)
                .build());

        TeamMember aliceEng = teamMemberRepository.save(TeamMember.builder()
                .team(engineering)
                .userId(alice.getId())
                .role(TeamRole.MEMBER)
                .build());

        // Design: admin (LEADER), alice (MEMBER)
        TeamMember adminDesign = teamMemberRepository.save(TeamMember.builder()
                .team(design)
                .userId(admin.getId())
                .role(TeamRole.LEADER)
                .build());

        TeamMember aliceDesign = teamMemberRepository.save(TeamMember.builder()
                .team(design)
                .userId(alice.getId())
                .role(TeamRole.MEMBER)
                .build());

        // Mobile Team: alice (LEADER), admin (MEMBER)
        TeamMember aliceMobile = teamMemberRepository.save(TeamMember.builder()
                .team(mobileTeam)
                .userId(alice.getId())
                .role(TeamRole.LEADER)
                .build());

        TeamMember adminMobile = teamMemberRepository.save(TeamMember.builder()
                .team(mobileTeam)
                .userId(admin.getId())
                .role(TeamRole.MEMBER)
                .build());

        // =====================================================
        // LABELS (4: Bug, Feature, Urgent, Enhancement)
        // =====================================================
        Label bugLbl = labelRepository.save(Label.builder().project(demoProject).name("Bug").color("#DC2626").build());
        Label featureLbl = labelRepository.save(Label.builder().project(demoProject).name("Feature").color("#2563EB").build());
        Label urgentLbl = labelRepository.save(Label.builder().project(demoProject).name("Urgent").color("#EA580C").build());
        Label enhanceLbl = labelRepository.save(Label.builder().project(demoProject).name("Enhancement").color("#7C3AED").build());

        Label mobileBug = labelRepository.save(Label.builder().project(mobileApp).name("Bug").color("#DC2626").build());
        Label mobileFeature = labelRepository.save(Label.builder().project(mobileApp).name("Feature").color("#2563EB").build());

        // =====================================================
        // TASKS (4: all priorities, all statuses)
        // =====================================================
        Task task1 = taskRepository.save(Task.builder()
                .project(demoProject).team(engineering)
                .title("Set up CI/CD pipeline")
                .description("Configure GitHub Actions for automated builds and deployments")
                .priority(Priority.HIGH).status(TaskStatus.IN_PROGRESS)
                .assigneeId(adminEng.getId())
                .createdByUserId(admin.getId())
                .build());
        task1.getLabels().add(bugLbl);
        task1.getLabels().add(urgentLbl);
        taskRepository.save(task1);

        Task task2 = taskRepository.save(Task.builder()
                .project(demoProject).team(engineering)
                .title("Fix login page bug")
                .description("Users cannot log in after the last CSS refactor — the form submit handler is broken")
                .priority(Priority.URGENT).status(TaskStatus.TODO)
                .assigneeId(adminEng.getId())
                .createdByUserId(admin.getId())
                .build());
        task2.getLabels().add(bugLbl);
        task2.getLabels().add(urgentLbl);
        taskRepository.save(task2);

        Task task3 = taskRepository.save(Task.builder()
                .project(demoProject).team(design)
                .title("Create landing page mockups")
                .description("Design high-fidelity mockups for the new public landing page in Figma")
                .priority(Priority.MEDIUM).status(TaskStatus.IN_REVIEW)
                .assigneeId(aliceDesign.getId())
                .createdByUserId(admin.getId())
                .build());
        task3.getLabels().add(featureLbl);
        taskRepository.save(task3);

        Task task4 = taskRepository.save(Task.builder()
                .project(mobileApp).team(mobileTeam)
                .title("Implement push notifications")
                .description("Integrate Firebase Cloud Messaging for push notification delivery")
                .priority(Priority.LOW).status(TaskStatus.DONE)
                .assigneeId(aliceMobile.getId())
                .createdByUserId(alice.getId())
                .build());
        task4.getLabels().add(mobileFeature);
        taskRepository.save(task4);

        // =====================================================
        // SUBTASKS (3 tasks, 2-3 each, mix of done/pending)
        // =====================================================
        subTaskRepository.save(SubTask.builder()
                .task(task1).title("Set up GitHub Actions workflow").assigneeId(adminEng.getId()).isDone(true).build());
        subTaskRepository.save(SubTask.builder()
                .task(task1).title("Configure Docker image build").assigneeId(adminEng.getId()).isDone(false).build());
        subTaskRepository.save(SubTask.builder()
                .task(task1).title("Add deployment script").assigneeId(aliceEng.getId()).isDone(false).build());

        subTaskRepository.save(SubTask.builder()
                .task(task2).title("Reproduce the bug locally").assigneeId(adminEng.getId()).isDone(true).build());
        subTaskRepository.save(SubTask.builder()
                .task(task2).title("Fix the CSS form handler").assigneeId(adminEng.getId()).isDone(false).build());

        subTaskRepository.save(SubTask.builder()
                .task(task4).title("Set up FCM SDK").assigneeId(aliceMobile.getId()).isDone(true).build());
        subTaskRepository.save(SubTask.builder()
                .task(task4).title("Handle incoming notifications").assigneeId(aliceMobile.getId()).isDone(true).build());
        subTaskRepository.save(SubTask.builder()
                .task(task4).title("Test on Android and iOS devices").assigneeId(aliceMobile.getId()).isDone(true).build());

        // =====================================================
        // COMMENTS (3-4 across tasks)
        // =====================================================
        commentRepository.save(Comment.builder()
                .task(task1).userId(admin.getId())
                .content("I've started working on the GitHub Actions workflow. The YAML config is in the .github directory.")
                .build());
        commentRepository.save(Comment.builder()
                .task(task1).userId(admin.getId())
                .content("Docker build is failing on the test stage — need to fix the test configuration first.")
                .build());
        commentRepository.save(Comment.builder()
                .task(task2).userId(admin.getId())
                .content("I can reproduce this consistently. The issue is in login-form.tsx line 42 — the event.preventDefault is missing.")
                .build());
        commentRepository.save(Comment.builder()
                .task(task4).userId(alice.getId())
                .content("FCM integration is complete. Notifications are working on both platforms.")
                .build());

        // =====================================================
        // ATTACHMENTS (3)
        // =====================================================
        attachmentRepository.save(Attachment.builder()
                .task(task1).userId(admin.getId())
                .fileName("ci-config.yml").fileUrl("https://example.com/uploads/ci-config.yml").fileSize(2048).build());
        attachmentRepository.save(Attachment.builder()
                .task(task3).userId(admin.getId())
                .fileName("landing-wireframe.png").fileUrl("https://example.com/uploads/landing-wireframe.png").fileSize(245760).build());
        attachmentRepository.save(Attachment.builder()
                .task(task3).userId(admin.getId())
                .fileName("design-system.pdf").fileUrl("https://example.com/uploads/design-system.pdf").fileSize(1048576).build());

        // =====================================================
        // TIME ENTRIES (3)
        // =====================================================
        timeEntryRepository.save(TimeEntry.builder()
                .task(task1).userId(admin.getId()).durationMinutes(120).date(LocalDate.now())
                .description("Initial CI/CD setup and configuration").build());
        timeEntryRepository.save(TimeEntry.builder()
                .task(task1).userId(admin.getId()).durationMinutes(45).date(LocalDate.now().minusDays(1))
                .description("Debugging Docker build issues").build());
        timeEntryRepository.save(TimeEntry.builder()
                .task(task4).userId(alice.getId()).durationMinutes(60).date(LocalDate.now().minusDays(2))
                .description("FCM integration and testing").build());

        log.info("========== Seed data created ==========");
        log.info("Users: admin@test.com / Admin123!  |  alice@test.com / Alice123!");
        log.info("Projects: Demo Project (slug={}), Mobile App (slug={})",
                demoProject.getSlug(), mobileApp.getSlug());
        log.info("Teams: Engineering (slug={}), Design (slug={}), Mobile Team (slug={})",
                engineering.getSlug(), design.getSlug(), mobileTeam.getSlug());
        log.info("Team member IDs: adminEngineering={}, aliceEngineering={}, adminDesign={}, aliceDesign={}, aliceMobile={}, adminMobile={}",
                adminEng.getId(), aliceEng.getId(), adminDesign.getId(), aliceDesign.getId(), aliceMobile.getId(), adminMobile.getId());
        log.info("Labels: 4 in Demo, 2 in Mobile App");
        log.info("Tasks: 3 in Demo Project, 1 in Mobile App");
        log.info("Subtasks: 3 on task1, 2 on task2, 3 on task4");
        log.info("Comments: 4 total | Attachments: 3 total | Time entries: 3 total");
        log.info("========================================");
    }
}
