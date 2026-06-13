package com.inpt.collaborationplatform.seed;

import com.inpt.collaborationplatform.Identity.entity.Role;
import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.Identity.repository.UserRepository;
import com.inpt.collaborationplatform.audit.entity.ActivityLog;
import com.inpt.collaborationplatform.audit.repository.ActivityLogRepository;
import com.inpt.collaborationplatform.notification.entity.Notification;
import com.inpt.collaborationplatform.notification.entity.NotificationPrefs;
import com.inpt.collaborationplatform.notification.entity.NotificationType;
import com.inpt.collaborationplatform.notification.repository.NotificationPrefsRepository;
import com.inpt.collaborationplatform.notification.repository.NotificationRepository;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitation;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.invitation.repository.ProjectInvitationRepository;
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
import com.inpt.collaborationplatform.collaboration.attachment.entity.Attachment;
import com.inpt.collaborationplatform.collaboration.attachment.repository.AttachmentRepository;
import com.inpt.collaborationplatform.collaboration.comment.entity.Comment;
import com.inpt.collaborationplatform.collaboration.comment.repository.CommentRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

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
    private final NotificationRepository notificationRepository;
    private final NotificationPrefsRepository notificationPrefsRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlugGenerator slugGenerator;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed data already exists, skipping initialization");
            return;
        }

        log.info("No users found -- creating seed data...");

        // ================================================================
        // USERS (10: 1 ADMIN + 9 regular)
        // ================================================================
        User admin = user("admin", "admin@test.com", "Admin123!", Role.ADMIN);
        User alice = user("alice", "alice@test.com", "Alice123!", Role.USER);
        User bob = user("bob", "bob@test.com", "Bob123!", Role.USER);
        User charlie = user("charlie", "charlie@test.com", "Charlie123!", Role.USER);
        User diana = user("diana", "diana@test.com", "Diana123!", Role.USER);
        User eve = user("eve", "eve@test.com", "Eve123!", Role.USER);
        User frank = user("frank", "frank@test.com", "Frank123!", Role.USER);
        User grace = user("grace", "grace@test.com", "Grace123!", Role.USER);
        User henry = user("henry", "henry@test.com", "Henry123!", Role.USER);
        User iris = user("iris", "iris@test.com", "Iris123!", Role.USER);
        log.info("Created 10 users");

        // ================================================================
        // PROJECTS (4: 3 ACTIVE, 1 ARCHIVED)
        // ================================================================
        Project demo = project("Demo Project", "A full-stack demo project for testing all features", admin);
        Project mobile = project("Mobile App", "Cross-platform mobile app built with Flutter", alice);
        Project web = project("Web Platform", "Customer-facing web dashboard and API gateway", bob);
        Project legacy = project("Legacy System", "Deprecated monolith -- archived for reference", admin);
        legacy.setStatus(com.inpt.collaborationplatform.projects.project.entity.ProjectStatus.ARCHIVED);
        projectRepository.save(legacy);
        log.info("Created 4 projects (3 active, 1 archived)");

        // ================================================================
        // PROJECT MEMBERS
        // ================================================================
        member(demo, admin, ProjectRole.OWNER);
        member(demo, alice, ProjectRole.ADMIN);
        member(demo, bob, ProjectRole.MEMBER);
        member(demo, charlie, ProjectRole.MEMBER);
        member(demo, diana, ProjectRole.MEMBER);
        member(demo, eve, ProjectRole.MEMBER);
        member(demo, frank, ProjectRole.MEMBER);
        member(demo, grace, ProjectRole.MEMBER);
        member(demo, henry, ProjectRole.MEMBER);
        member(demo, iris, ProjectRole.VIEWER);

        member(mobile, alice, ProjectRole.OWNER);
        member(mobile, admin, ProjectRole.ADMIN);
        member(mobile, bob, ProjectRole.MEMBER);
        member(mobile, eve, ProjectRole.MEMBER);
        member(mobile, frank, ProjectRole.MEMBER);
        member(mobile, charlie, ProjectRole.MEMBER);
        member(mobile, diana, ProjectRole.MEMBER);
        member(mobile, grace, ProjectRole.MEMBER);
        member(mobile, henry, ProjectRole.MEMBER);

        member(web, bob, ProjectRole.OWNER);
        member(web, alice, ProjectRole.ADMIN);
        member(web, charlie, ProjectRole.MEMBER);
        member(web, diana, ProjectRole.MEMBER);
        member(web, eve, ProjectRole.MEMBER);
        member(web, admin, ProjectRole.ADMIN);
        member(web, frank, ProjectRole.MEMBER);
        member(web, grace, ProjectRole.MEMBER);
        member(web, henry, ProjectRole.VIEWER);

        member(legacy, admin, ProjectRole.OWNER);
        member(legacy, charlie, ProjectRole.MEMBER);
        member(legacy, diana, ProjectRole.MEMBER);
        member(legacy, eve, ProjectRole.MEMBER);
        member(legacy, iris, ProjectRole.MEMBER);

        log.info("Created 34 project memberships");

        // ================================================================
        // TEAMS (8 -- each with exactly 5 members)
        // ================================================================
        Team eng = team(demo, "Engineering", "Core backend & frontend engineering squad", admin);
        Team design = team(demo, "Design", "UX research, visual design and prototyping", admin);
        Team qa = team(demo, "Quality Assurance", "Manual and automated testing", admin);

        Team mobileTeam = team(mobile, "Mobile Team", "Flutter mobile development", alice);
        Team backend = team(mobile, "Backend Team", "API and microservice development", alice);

        Team frontend = team(web, "Frontend Team", "React SPA and SSR development", bob);
        Team devops = team(web, "DevOps", "CI/CD, infrastructure and monitoring", bob);

        Team maintenance = team(legacy, "Maintenance", "Legacy system upkeep and patches", admin);

        // ================================================================
        // TEAM MEMBERS (5 per team = 40)
        // ================================================================
        TeamMember adminEng = tm(eng, admin, TeamRole.LEADER);
        TeamMember aliceEng = tm(eng, alice, TeamRole.MEMBER);
        TeamMember bobEng = tm(eng, bob, TeamRole.MEMBER);
        TeamMember charlieEng = tm(eng, charlie, TeamRole.MEMBER);
        TeamMember dianaEng = tm(eng, diana, TeamRole.MEMBER);

        TeamMember adminDes = tm(design, admin, TeamRole.LEADER);
        TeamMember aliceDes = tm(design, alice, TeamRole.MEMBER);
        TeamMember eveDes = tm(design, eve, TeamRole.MEMBER);
        TeamMember frankDes = tm(design, frank, TeamRole.MEMBER);
        TeamMember graceDes = tm(design, grace, TeamRole.MEMBER);

        TeamMember adminQA = tm(qa, admin, TeamRole.LEADER);
        TeamMember bobQA = tm(qa, bob, TeamRole.MEMBER);
        TeamMember charlieQA = tm(qa, charlie, TeamRole.MEMBER);
        TeamMember henryQA = tm(qa, henry, TeamRole.MEMBER);
        TeamMember irisQA = tm(qa, iris, TeamRole.MEMBER);

        TeamMember aliceMT = tm(mobileTeam, alice, TeamRole.LEADER);
        TeamMember adminMT = tm(mobileTeam, admin, TeamRole.MEMBER);
        TeamMember bobMT = tm(mobileTeam, bob, TeamRole.MEMBER);
        TeamMember eveMT = tm(mobileTeam, eve, TeamRole.MEMBER);
        TeamMember frankMT = tm(mobileTeam, frank, TeamRole.MEMBER);

        TeamMember aliceBE = tm(backend, alice, TeamRole.LEADER);
        TeamMember charlieBE = tm(backend, charlie, TeamRole.MEMBER);
        TeamMember dianaBE = tm(backend, diana, TeamRole.MEMBER);
        TeamMember graceBE = tm(backend, grace, TeamRole.MEMBER);
        TeamMember henryBE = tm(backend, henry, TeamRole.MEMBER);

        TeamMember bobFE = tm(frontend, bob, TeamRole.LEADER);
        TeamMember aliceFE = tm(frontend, alice, TeamRole.MEMBER);
        TeamMember charlieFE = tm(frontend, charlie, TeamRole.MEMBER);
        TeamMember dianaFE = tm(frontend, diana, TeamRole.MEMBER);
        TeamMember eveFE = tm(frontend, eve, TeamRole.MEMBER);

        TeamMember bobDO = tm(devops, bob, TeamRole.LEADER);
        TeamMember adminDO = tm(devops, admin, TeamRole.MEMBER);
        TeamMember frankDO = tm(devops, frank, TeamRole.MEMBER);
        TeamMember graceDO = tm(devops, grace, TeamRole.MEMBER);
        TeamMember henryDO = tm(devops, henry, TeamRole.MEMBER);

        TeamMember adminMaint = tm(maintenance, admin, TeamRole.LEADER);
        TeamMember charlieMaint = tm(maintenance, charlie, TeamRole.MEMBER);
        TeamMember dianaMaint = tm(maintenance, diana, TeamRole.MEMBER);
        TeamMember eveMaint = tm(maintenance, eve, TeamRole.MEMBER);
        TeamMember irisMaint = tm(maintenance, iris, TeamRole.MEMBER);

        log.info("Created 8 teams with 5 members each (40 memberships)");

        // ================================================================
        // LABELS (16 across projects)
        // ================================================================
        Label dBug = lbl(demo, "Bug", "#DC2626");
        Label dFeature = lbl(demo, "Feature", "#2563EB");
        Label dUrgent = lbl(demo, "Urgent", "#EA580C");
        Label dEnhance = lbl(demo, "Enhancement", "#7C3AED");
        Label dDocs = lbl(demo, "Documentation", "#0891B2");

        Label mBug = lbl(mobile, "Bug", "#DC2626");
        Label mFeature = lbl(mobile, "Feature", "#2563EB");
        Label mEnhance = lbl(mobile, "Enhancement", "#7C3AED");

        Label wBug = lbl(web, "Bug", "#DC2626");
        Label wFeature = lbl(web, "Feature", "#2563EB");
        Label wUrgent = lbl(web, "Urgent", "#EA580C");
        Label wDocs = lbl(web, "Documentation", "#0891B2");

        Label lBug = lbl(legacy, "Bug", "#DC2626");
        Label lSecurity = lbl(legacy, "Security", "#7C3AED");

        log.info("Created 16 labels");

        // ================================================================
        // TASKS (25 -- all statuses, priorities, due-date scenarios)
        // ================================================================
        Task t1 = task(demo, eng, "Set up CI/CD pipeline",
                "Configure GitHub Actions for automated builds, tests and deployments",
                Priority.HIGH, TaskStatus.IN_PROGRESS, adminEng, admin, plusDays(30), dBug, dUrgent);

        Task t2 = task(demo, eng, "Fix login page bug",
                "Users cannot log in after the last CSS refactor -- the form submit handler is broken",
                Priority.URGENT, TaskStatus.TODO, adminEng, admin, minusDays(1), dBug, dUrgent);

        Task t3 = task(demo, eng, "Refactor authentication module",
                "Replace legacy JWT library with a more modern, well-maintained alternative",
                Priority.HIGH, TaskStatus.TODO, bobEng, admin, plusDays(7), dEnhance);

        Task t4 = task(demo, eng, "Write unit tests for API",
                "Achieve at least 80 % code coverage on the REST controllers",
                Priority.MEDIUM, TaskStatus.IN_PROGRESS, aliceEng, admin, null);

        Task t5 = task(demo, eng, "Code review: PR #42",
                "Review the pagination refactor pull request submitted by the frontend team",
                Priority.LOW, TaskStatus.DONE, charlieEng, admin, minusDays(15), dFeature);

        Task t6 = task(demo, design, "Create landing page mockups",
                "Design high-fidelity mockups for the new public landing page in Figma",
                Priority.MEDIUM, TaskStatus.IN_REVIEW, aliceDes, admin, plusDays(5), dFeature);

        Task t7 = task(demo, design, "Design system documentation",
                "Document all components, tokens and usage guidelines in Storybook",
                Priority.LOW, TaskStatus.DONE, eveDes, admin, minusDays(30), dDocs);

        Task t8 = task(demo, design, "User research report",
                "Compile findings from the latest round of user interviews and usability tests",
                Priority.MEDIUM, TaskStatus.TODO, frankDes, admin, null);

        Task t9 = task(demo, design, "Design new dashboard UI",
                "Create mockups for the redesigned analytics dashboard with new chart components",
                Priority.HIGH, TaskStatus.IN_PROGRESS, graceDes, admin, plusDays(45), dFeature, dEnhance);

        Task t10 = task(demo, qa, "E2E testing for login flow",
                "Write and run Cypress E2E tests covering login, MFA and password reset",
                Priority.HIGH, TaskStatus.TODO, bobQA, admin, plusDays(3), dBug);

        Task t11 = task(demo, qa, "Performance benchmark",
                "Run load tests with k6 and report P50 / P95 / P99 latency metrics",
                Priority.MEDIUM, TaskStatus.IN_REVIEW, henryQA, admin, plusDays(20));

        Task t12 = task(demo, qa, "Regression test suite",
                "Automate the full regression suite and integrate it into the CI pipeline",
                Priority.LOW, TaskStatus.DONE, irisQA, admin, minusDays(10), dDocs);

        Task t13 = task(mobile, mobileTeam, "Implement push notifications",
                "Integrate Firebase Cloud Messaging for push notification delivery on both platforms",
                Priority.LOW, TaskStatus.DONE, aliceMT, alice, minusDays(7), mFeature);

        Task t14 = task(mobile, mobileTeam, "Fix splash screen crash",
                "App crashes on Android 12 when the splash screen tries to load the remote config",
                Priority.URGENT, TaskStatus.IN_PROGRESS, bobMT, alice, minusDays(1), mBug);

        Task t15 = task(mobile, mobileTeam, "Add biometric auth",
                "Implement fingerprint and face unlock as alternative login methods",
                Priority.HIGH, TaskStatus.TODO, eveMT, alice, plusDays(60), mFeature);

        Task t16 = task(mobile, backend, "Design REST API schema",
                "Document the new v2 API contract using OpenAPI 3.1",
                Priority.MEDIUM, TaskStatus.DONE, charlieBE, alice, minusDays(14), mFeature);

        Task t17 = task(mobile, backend, "Implement rate limiting",
                "Add configurable rate limiting per user and per endpoint using Redis",
                Priority.HIGH, TaskStatus.IN_PROGRESS, dianaBE, alice, plusDays(10));

        Task t18 = task(mobile, backend, "Database migration v2",
                "Write Flyway migration scripts for the new schema changes (users table split)",
                Priority.URGENT, TaskStatus.TODO, graceBE, alice, plusDays(2), mEnhance);

        Task t19 = task(web, frontend, "Implement dark mode",
                "Add theme switching with CSS custom properties and persist the preference",
                Priority.MEDIUM, TaskStatus.IN_PROGRESS, aliceFE, bob, plusDays(30), wFeature);

        Task t20 = task(web, frontend, "Fix responsive layout bugs",
                "The settings page breaks on tablet viewports -- fix the grid and overflowing sections",
                Priority.HIGH, TaskStatus.TODO, charlieFE, bob, plusDays(4), wBug);

        Task t21 = task(web, frontend, "Add page transitions",
                "Implement smooth route transitions using Framer Motion",
                Priority.LOW, TaskStatus.DONE, dianaFE, bob, minusDays(20), wFeature);

        Task t22 = task(web, devops, "Set up monitoring stack",
                "Deploy Prometheus + Grafana + Alertmanager for infrastructure and application monitoring",
                Priority.HIGH, TaskStatus.TODO, adminDO, bob, plusDays(7), wFeature, wUrgent);

        Task t23 = task(web, devops, "Migrate to Kubernetes",
                "Move the staging environment from Docker Compose to a K3s cluster",
                Priority.MEDIUM, TaskStatus.IN_REVIEW, frankDO, bob, plusDays(90));

        Task t24 = task(legacy, maintenance, "Update dependencies",
                "Upgrade all Maven dependencies to their latest patch versions to address CVEs",
                Priority.MEDIUM, TaskStatus.IN_PROGRESS, charlieMaint, admin, plusDays(7));

        Task t25 = task(legacy, maintenance, "Fix security vulnerability",
                "Patch the SQL injection vector in the legacy reporting endpoint (CVE-2024-1234)",
                Priority.URGENT, TaskStatus.TODO, dianaMaint, admin, minusDays(1), lBug, lSecurity);

        log.info("Created 25 tasks across all statuses, priorities and due-date scenarios");

        // ================================================================
        // SUBTASKS (many tasks, mix of done/pending)
        // ================================================================
        st(t1, "Set up GitHub Actions workflow", adminEng, true);
        st(t1, "Configure Docker image build", adminEng, false);
        st(t1, "Add deployment script", aliceEng, false);
        st(t1, "Configure environment secrets", adminEng, false);

        st(t2, "Reproduce the bug locally", adminEng, true);
        st(t2, "Identify root cause in login-form.tsx", adminEng, true);
        st(t2, "Implement fix and open PR", adminEng, false);
        st(t2, "Run regression tests", bobEng, false);

        st(t4, "Write controller tests", aliceEng, true);
        st(t4, "Write service layer tests", aliceEng, true);
        st(t4, "Write repository integration tests", aliceEng, false);
        st(t4, "Set up JaCoCo coverage gate", adminEng, false);

        st(t6, "Create wireframes", aliceDes, true);
        st(t6, "Design high-fidelity mockups", aliceDes, false);
        st(t6, "Prepare prototype in Figma", aliceDes, false);

        st(t9, "Audit current dashboard UX", graceDes, true);
        st(t9, "Sketch new layout options", graceDes, true);
        st(t9, "Design new chart components", graceDes, false);
        st(t9, "Build interactive prototype", graceDes, false);

        st(t10, "Set up Cypress", bobQA, true);
        st(t10, "Write login flow tests", bobQA, false);
        st(t10, "Write MFA flow tests", bobQA, false);
        st(t10, "Add CI integration", henryQA, false);

        st(t13, "Set up FCM SDK", aliceMT, true);
        st(t13, "Handle incoming notifications", aliceMT, true);
        st(t13, "Test on Android", aliceMT, true);
        st(t13, "Test on iOS", aliceMT, true);

        st(t14, "Identify crash stack trace", bobMT, true);
        st(t14, "Fix RemoteConfig init order", bobMT, false);

        st(t17, "Design rate-limit algorithm", dianaBE, true);
        st(t17, "Implement Redis-backed counter", dianaBE, false);
        st(t17, "Add response headers", dianaBE, false);
        st(t17, "Write integration tests", graceBE, false);

        st(t18, "Review new schema design", graceBE, true);
        st(t18, "Write Flyway migration V2__split_users", graceBE, false);
        st(t18, "Write rollback script", charlieBE, false);

        st(t19, "Define CSS custom properties", aliceFE, true);
        st(t19, "Implement theme toggle component", aliceFE, true);
        st(t19, "Persist preference in localStorage", aliceFE, false);
        st(t19, "Add system-theme detection", aliceFE, false);

        st(t20, "Identify broken viewport ranges", charlieFE, true);
        st(t20, "Fix grid layout for tablet", charlieFE, false);
        st(t20, "Fix overflowing sections", charlieFE, false);

        st(t22, "Deploy Prometheus server", adminDO, true);
        st(t22, "Configure Grafana dashboards", adminDO, false);
        st(t22, "Set up Alertmanager rules", frankDO, false);
        st(t22, "Add application metrics export", graceDO, false);

        st(t24, "Audit current dependency versions", charlieMaint, true);
        st(t24, "Update pom.xml versions", charlieMaint, false);
        st(t24, "Run full test suite", dianaMaint, false);

        st(t25, "Identify affected endpoints", dianaMaint, true);
        st(t25, "Apply parameterized query fix", dianaMaint, false);
        st(t25, "Run security scan", charlieMaint, false);

        log.info("Created 60+ subtasks");

        // ================================================================
        // COMMENTS
        // ================================================================
        cm(t1, admin, "I've started working on the GitHub Actions workflow. The YAML config is in the .github directory.");
        cm(t1, admin, "Docker build is failing on the test stage -- need to check the test configuration.");
        cm(t1, alice, "I can help with the Docker config. Let me push a fix to the branch.");
        cm(t2, admin, "I can reproduce this consistently. The issue is in login-form.tsx line 42.");
        cm(t2, bob, "I'll take a look at the CSS side. The form styles might need a z-index adjustment too.");
        cm(t6, admin, "Great progress on the mockups! Can we also add mobile variants?");
        cm(t6, alice, "Sure, I'll include responsive breakpoints for tablet and mobile.");
        cm(t9, admin, "The new dashboard looks much cleaner. Let's schedule a review session.");
        cm(t9, grace, "I've shared the Figma link in the team channel. Feedback welcome!");
        cm(t10, bob, "Cypress is set up locally. Starting with the login flow tests.");
        cm(t13, alice, "FCM integration is complete. Notifications are working on both platforms.");
        cm(t13, admin, "Great work! Can we add click handling to open specific screens?");
        cm(t14, bob, "Found the issue: RemoteConfig.init() is called after the splash screen widget builds.");
        cm(t14, alice, "Good catch. Let's move the init to main.dart before runApp().");
        cm(t17, diana, "Rate limiting design doc is ready for review.");
        cm(t17, alice, "Looks good. Let's use a sliding window approach with Redis Sorted Sets.");
        cm(t19, alice, "Dark mode is ready for review on the staging environment.");
        cm(t19, bob, "Looks great! One minor issue -- the contrast on the sidebar is a bit low.");
        cm(t22, admin, "Prometheus is deployed. Working on Grafana dashboards now.");
        cm(t22, bob, "Can we add a SLA / SLO dashboard as well?");
        cm(t25, diana, "The SQL injection fix is applied. Running the security scan now.");
        cm(t25, admin, "Make sure to also update the CI pipeline to include SAST scanning.");

        log.info("Created 22 comments");

        // ================================================================
        // ATTACHMENTS
        // ================================================================
        at(t1, admin, "ci-config.yml", "https://example.com/uploads/ci-config.yml", 2048);
        at(t1, admin, "Dockerfile", "https://example.com/uploads/Dockerfile", 1536);
        at(t6, admin, "landing-wireframe.png", "https://example.com/uploads/landing-wireframe.png", 245760);
        at(t6, admin, "design-system.pdf", "https://example.com/uploads/design-system.pdf", 1048576);
        at(t9, grace, "dashboard-redesign-v2.fig", "https://example.com/uploads/dashboard-redesign-v2.fig", 4194304);
        at(t10, bob, "cypress-test-results.html", "https://example.com/uploads/cypress-results.html", 56000);
        at(t13, alice, "fcm-integration-guide.pdf", "https://example.com/uploads/fcm-guide.pdf", 892000);
        at(t17, diana, "rate-limiting-design.md", "https://example.com/uploads/rate-limit-design.md", 4200);
        at(t22, admin, "prometheus-rules.yml", "https://example.com/uploads/prometheus-rules.yml", 3100);
        at(t25, diana, "security-scan-report.html", "https://example.com/uploads/security-scan.html", 128000);

        log.info("Created 10 attachments");

        // ================================================================
        // TIME ENTRIES
        // ================================================================
        te(t1, admin, 120, LocalDate.now(), "Initial CI/CD setup and configuration");
        te(t1, admin, 45, LocalDate.now().minusDays(1), "Debugging Docker build issues");
        te(t1, alice, 60, LocalDate.now().minusDays(2), "Helping with Docker config fix");
        te(t2, admin, 90, LocalDate.now().minusDays(1), "Bug investigation and reproduction");
        te(t2, bob, 45, LocalDate.now(), "CSS fix and testing");
        te(t6, alice, 180, LocalDate.now().minusDays(3), "Landing page mockup design");
        te(t6, alice, 120, LocalDate.now().minusDays(1), "Mobile responsive variants");
        te(t9, grace, 240, LocalDate.now().minusDays(5), "Dashboard redesign initial concepts");
        te(t9, grace, 180, LocalDate.now().minusDays(2), "Interactive prototype in Figma");
        te(t10, bob, 90, LocalDate.now().minusDays(1), "Cypress setup and initial tests");
        te(t13, alice, 60, LocalDate.now().minusDays(7), "FCM SDK integration");
        te(t13, alice, 45, LocalDate.now().minusDays(6), "Testing notifications on Android");
        te(t14, bob, 120, LocalDate.now().minusDays(1), "Crash investigation and fix");
        te(t17, diana, 150, LocalDate.now().minusDays(3), "Rate limiting design and implementation");
        te(t19, alice, 90, LocalDate.now().minusDays(4), "Dark mode CSS variables setup");
        te(t19, alice, 60, LocalDate.now().minusDays(2), "Theme toggle component");
        te(t22, admin, 180, LocalDate.now().minusDays(2), "Prometheus + Grafana deployment");
        te(t24, charlie, 60, LocalDate.now().minusDays(1), "Dependency audit");
        te(t25, diana, 90, LocalDate.now(), "SQL injection fix implementation");
        te(t25, charlie, 30, LocalDate.now(), "Security scan execution");

        log.info("Created 20 time entries");

        // ================================================================
        // NOTIFICATION PREFS (defaults = all true via @Builder.Default)
        // ================================================================
        for (User u : new User[]{admin, alice, bob, charlie, diana, eve, frank, grace, henry, iris}) {
            notificationPrefsRepository.save(NotificationPrefs.builder().userId(u.getId()).build());
        }
        log.info("Created notification prefs for all 10 users");

        // ================================================================
        // NOTIFICATIONS (mix of read + unread, all types)
        // ================================================================
        ntf(admin, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Set up CI/CD pipeline\"", "TASK", t1.getId(), false);
        ntf(admin, NotificationType.STATUS_CHANGED, "Status Changed",
                "Task \"Fix login page bug\" moved from TODO to URGENT", "TASK", t2.getId(), false);
        ntf(admin, NotificationType.COMMENT_ADDED, "New Comment",
                "alice commented on task \"Set up CI/CD pipeline\"", "COMMENT", t1.getId(), false);
        ntf(admin, NotificationType.DEADLINE_APPROACHING, "Deadline Approaching",
                "Task \"Fix login page bug\" is overdue", "TASK", t2.getId(), false);
        ntf(admin, NotificationType.MEMBER_INVITED, "Member Invited",
                "You were added to the DevOps team in Web Platform", "TEAM", devops.getId(), true);

        ntf(alice, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Write unit tests for API\"", "TASK", t4.getId(), false);
        ntf(alice, NotificationType.COMMENT_ADDED, "New Comment",
                "admin commented on task \"Create landing page mockups\"", "COMMENT", t6.getId(), false);
        ntf(alice, NotificationType.DEADLINE_APPROACHING, "Deadline Approaching",
                "Task \"Implement push notifications\" is due soon", "TASK", t13.getId(), true);
        ntf(alice, NotificationType.STATUS_CHANGED, "Status Changed",
                "Task \"Fix splash screen crash\" moved from TODO to IN_PROGRESS", "TASK", t14.getId(), false);

        ntf(bob, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Refactor authentication module\"", "TASK", t3.getId(), false);
        ntf(bob, NotificationType.MEMBER_INVITED, "Member Invited",
                "You were added to the Mobile Team in Mobile App", "TEAM", mobileTeam.getId(), true);
        ntf(bob, NotificationType.COMMENT_ADDED, "New Comment",
                "admin commented on task \"Fix login page bug\"", "COMMENT", t2.getId(), false);

        ntf(charlie, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Code review: PR #42\"", "TASK", t5.getId(), true);
        ntf(charlie, NotificationType.STATUS_CHANGED, "Status Changed",
                "Task \"Design REST API schema\" moved to DONE", "TASK", t16.getId(), false);

        ntf(diana, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Implement rate limiting\"", "TASK", t17.getId(), false);

        ntf(eve, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Add biometric auth\"", "TASK", t15.getId(), false);

        ntf(grace, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Database migration v2\"", "TASK", t18.getId(), false);

        ntf(iris, NotificationType.TASK_ASSIGNED, "Task Assigned",
                "You were assigned to task \"Regression test suite\"", "TASK", t12.getId(), true);

        log.info("Created 17 notifications (mix of read/unread, all types)");

        // ================================================================
        // ACTIVITY LOGS
        // ================================================================
        al(demo, admin, "TASK", t1.getId(), "CREATED", "Task \"Set up CI/CD pipeline\" created");
        al(demo, admin, "TASK", t1.getId(), "ASSIGNED", "Task \"Set up CI/CD pipeline\" assigned");
        al(demo, admin, "TASK", t2.getId(), "CREATED", "Task \"Fix login page bug\" created");
        al(demo, admin, "TASK", t2.getId(), "PRIORITY_CHANGED", "Task \"Fix login page bug\" set to URGENT");
        al(demo, admin, "TASK", t6.getId(), "CREATED", "Task \"Create landing page mockups\" created");
        al(demo, admin, "TASK", t9.getId(), "CREATED", "Task \"Design new dashboard UI\" created");
        al(demo, bob, "TASK", t10.getId(), "CREATED", "Task \"E2E testing for login flow\" created");
        al(demo, admin, "TEAM", eng.getId(), "CREATED", "Team \"Engineering\" created");
        al(demo, admin, "TEAM", design.getId(), "CREATED", "Team \"Design\" created");
        al(demo, admin, "TEAM", qa.getId(), "CREATED", "Team \"Quality Assurance\" created");
        al(demo, admin, "MEMBER", admin.getId(), "JOINED", "admin joined Demo Project as OWNER");
        al(demo, alice, "MEMBER", alice.getId(), "JOINED", "alice joined Demo Project as ADMIN");

        al(mobile, alice, "TASK", t13.getId(), "CREATED", "Task \"Implement push notifications\" created");
        al(mobile, alice, "TASK", t13.getId(), "STATUS_CHANGED", "Task \"Implement push notifications\" moved to DONE");
        al(mobile, alice, "TASK", t14.getId(), "CREATED", "Task \"Fix splash screen crash\" created");
        al(mobile, alice, "TASK", t15.getId(), "CREATED", "Task \"Add biometric auth\" created");
        al(mobile, alice, "TEAM", mobileTeam.getId(), "CREATED", "Team \"Mobile Team\" created");
        al(mobile, alice, "TEAM", backend.getId(), "CREATED", "Team \"Backend Team\" created");

        al(web, bob, "TASK", t19.getId(), "CREATED", "Task \"Implement dark mode\" created");
        al(web, bob, "TASK", t20.getId(), "CREATED", "Task \"Fix responsive layout bugs\" created");
        al(web, bob, "TASK", t22.getId(), "CREATED", "Task \"Set up monitoring stack\" created");
        al(web, bob, "TEAM", frontend.getId(), "CREATED", "Team \"Frontend Team\" created");
        al(web, bob, "TEAM", devops.getId(), "CREATED", "Team \"DevOps\" created");

        al(legacy, admin, "TASK", t24.getId(), "CREATED", "Task \"Update dependencies\" created");
        al(legacy, admin, "TASK", t25.getId(), "CREATED", "Task \"Fix security vulnerability\" created");
        al(legacy, admin, "TEAM", maintenance.getId(), "CREATED", "Team \"Maintenance\" created");
        al(legacy, admin, "PROJECT", legacy.getId(), "ARCHIVED", "Project \"Legacy System\" archived");

        log.info("Created 28 activity log entries");

        // ================================================================
        // INVITATIONS (PENDING, EXPIRED, CANCELLED for edge-case coverage)
        // ================================================================
        inv(demo, "pending@test.com", ProjectRole.MEMBER, admin, plusDaysLocal(7), ProjectInvitationStatus.PENDING);
        inv(demo, "expired@test.com", ProjectRole.VIEWER, admin, minusDaysLocal(5), ProjectInvitationStatus.EXPIRED);
        inv(mobile, "cancelled@test.com", ProjectRole.MEMBER, alice, plusDaysLocal(14), ProjectInvitationStatus.CANCELLED);
        inv(web, "pending2@test.com", ProjectRole.ADMIN, bob, plusDaysLocal(3), ProjectInvitationStatus.PENDING);
        inv(legacy, "pending3@test.com", ProjectRole.MEMBER, admin, plusDaysLocal(10), ProjectInvitationStatus.PENDING);

        log.info("Created 5 invitations (3 PENDING, 1 EXPIRED, 1 CANCELLED)");

        // ================================================================
        // SUMMARY
        // ================================================================
        log.info("========== Seed data created ==========");
        log.info("Users: 10 (1 admin, 9 regular)");
        log.info("Projects: 4 (3 active, 1 archived)");
        log.info("Project memberships: 34");
        log.info("Teams: 8 (5 members each = 40 memberships)");
        log.info("Labels: 16");
        log.info("Tasks: 25");
        log.info("Subtasks: 60+");
        log.info("Comments: 22");
        log.info("Attachments: 10");
        log.info("Time entries: 20");
        log.info("Notifications: 17");
        log.info("Activity logs: 28");
        log.info("Invitations: 5");
        log.info("========================================");
    }

    // ================================================================
    // Helper methods (sugar to keep the main flow concise)
    // ================================================================

    private User user(String username, String email, String rawPw, Role role) {
        return userRepository.save(User.builder()
                .email(email).username(username)
                .password(passwordEncoder.encode(rawPw))
                .enabled(true).role(role).build());
    }

    private Project project(String name, String description, User owner) {
        return projectRepository.save(Project.builder()
                .name(name)
                .slug(slugGenerator.uniqueSlug(name, slug -> projectRepository.existsBySlug(slug)))
                .description(description)
                .createdByUserId(owner.getId())
                .build());
    }

    private void member(Project project, User user, ProjectRole role) {
        projectMemberRepository.save(ProjectMember.builder()
                .project(project).userId(user.getId()).role(role).build());
    }

    private Team team(Project project, String name, String description, User creator) {
        return teamRepository.save(Team.builder()
                .project(project).name(name)
                .slug(slugGenerator.uniqueSlug(name,
                        slug -> teamRepository.existsByProject_IdAndSlug(project.getId(), slug)))
                .description(description)
                .createdByUserId(creator.getId())
                .build());
    }

    private TeamMember tm(Team team, User user, TeamRole role) {
        return teamMemberRepository.save(TeamMember.builder()
                .team(team).userId(user.getId()).role(role).build());
    }

    private Label lbl(Project project, String name, String color) {
        return labelRepository.save(Label.builder().project(project).name(name).color(color).build());
    }

    private Task task(Project project, Team team, String title, String description,
                      Priority priority, TaskStatus status, TeamMember assignee,
                      User creator, LocalDate dueDate, Label... labels) {
        Task t = taskRepository.save(Task.builder()
                .project(project).team(team)
                .title(title).description(description)
                .priority(priority).status(status)
                .assigneeId(assignee.getId())
                .createdByUserId(creator.getId())
                .dueDate(dueDate)
                .build());
        for (Label l : labels) {
            t.getLabels().add(l);
        }
        return taskRepository.save(t);
    }

    private void st(Task task, String title, TeamMember assignee, boolean done) {
        String doneStatus = done ? "DONE" : "TODO";
        subTaskRepository.save(SubTask.builder()
                .task(task).title(title).assigneeId(assignee.getId())
                .isDone(done).status(TaskStatus.valueOf(doneStatus)).build());
    }

    private void cm(Task task, User author, String content) {
        commentRepository.save(Comment.builder()
                .task(task).userId(author.getId()).content(content).build());
    }

    private void at(Task task, User uploader, String fileName, String fileUrl, int size) {
        attachmentRepository.save(Attachment.builder()
                .task(task).userId(uploader.getId())
                .fileName(fileName).fileUrl(fileUrl).fileSize(size).build());
    }

    private void te(Task task, User user, int minutes, LocalDate date, String description) {
        timeEntryRepository.save(TimeEntry.builder()
                .task(task).userId(user.getId())
                .durationMinutes(minutes).date(date).description(description).build());
    }

    private void ntf(User user, NotificationType type, String title, String message,
                     String relatedType, String relatedId, boolean read) {
        notificationRepository.save(Notification.builder()
                .userId(user.getId()).type(type)
                .title(title).message(message)
                .relatedEntityType(relatedType).relatedEntityId(relatedId)
                .isRead(read).build());
    }

    private void al(Project project, User actor, String entityType, String entityId,
                    String action, String details) {
        activityLogRepository.save(ActivityLog.builder()
                .actorId(actor.getId()).projectId(project.getId())
                .entityType(entityType).entityId(entityId)
                .action(action).details(details).build());
    }

    private void inv(Project project, String email, ProjectRole role, User invitedBy,
                     LocalDateTime expiresAt, ProjectInvitationStatus status) {
        projectInvitationRepository.save(ProjectInvitation.builder()
                .project(project).email(email).role(role)
                .token(UUID.randomUUID().toString())
                .status(status)
                .invitedByUserId(invitedBy.getId())
                .expiresAt(expiresAt)
                .build());
    }

    private static LocalDate plusDays(int n) {
        return LocalDate.now().plusDays(n);
    }

    private static LocalDate minusDays(int n) {
        return LocalDate.now().minusDays(n);
    }

    private static LocalDateTime plusDaysLocal(int n) {
        return LocalDateTime.now().plusDays(n);
    }

    private static LocalDateTime minusDaysLocal(int n) {
        return LocalDateTime.now().minusDays(n);
    }
}
