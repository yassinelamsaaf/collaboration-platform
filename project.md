# Collaboration Platform — Project Architecture (Exhaustive)

---

## 1. General Context

This is a **full-stack collaboration platform** (SaaS-style) built as a school project (PFA - Projet de Fin d'Années/Études). The platform allows teams to manage projects and tasks with a Kanban workflow, real-time collaboration, role-based access control, and notifications.

The project follows a **monorepo structure** with two main modules:

| Module | Tech Stack | Port |
|--------|------------|------|
| **client/** | Angular 21.2.11 (NgModule-based, with Standalone-ready patterns) | `4200` |
| **server/** | Spring Boot 3.5 + Java 17 + Maven | `8080` |
| **db** | PostgreSQL 16 Alpine (via Docker) | `5432` |
| **cache** | Redis 7 Alpine (via Docker) | `6379` |

All infrastructure is managed with **Docker Compose** (`docker-compose.yml` at root).

Deployment target: Docker Compose locally, Railway/Render/Fly.io in production.

---

## 2. Global Architecture — Main Folders & Their Roles

This section gives a high-level map of every top-level folder in both `client/` and `server/`, explaining what goes in each one and how files should be organized going forward.

---

### 2.1 Client (`client/`) — Angular Frontend Folder Structure

```
client/
├── angular.json                  # Angular CLI config (builder, assets, budgets, environments)
├── package.json                  # npm dependencies & scripts (ng serve, build, test)
├── tsconfig.json                 # Root TypeScript config (strict mode, ES2022)
├── tsconfig.app.json             # App TS config (extends root, excludes spec files)
├── tsconfig.spec.json            # Test TS config (vitest globals, jsdom)
├── .env                          # NOT consumed by Angular (UI-only reference)
├── .env.example                  # Template for environment variables
├── .prettierrc                   # Code formatting rules
├── .editorconfig                 # Editor settings
│
├── public/                       # Static assets copied as-is to build output
│   ├── favicon.ico               #   Browser tab icon
│   └── assets/images/            #   Icons, backgrounds, illustrations
│
├── node_modules/                 # npm dependencies (installed, not committed)
├── dist/                         # Build output (generated, not committed)
│
└── src/                          # Application source code
    ├── index.html                #   SPA shell (root HTML)
    ├── main.ts                   #   Angular bootstrap entry point
    ├── styles.scss               #   Root SCSS entry (imports global styles)
    │
    ├── styles/                   #   Global SCSS architecture (NOT component-scoped)
    │   ├── abstracts/            #     Variables, colors, functions, mixins
    │   ├── base/                 #     Reset, typography, global utilities
    │   ├── themes/               #     Theme CSS custom properties (light/dark)
    │   └── components/           #     Global BEM component styles (buttons, forms, alerts, auth, landing)
    │
    └── app/                      # All Angular application code
        ├── app.module.ts         #   Root module (imports BrowserModule, HttpClientModule, etc.)
        ├── app-routing.module.ts #   Root routing (lazy-loads features)
        ├── app.component.ts      #   Root component (<router-outlet> shell)
        ├── app.component.html    #   Root template
        ├── app.component.scss    #   Root styles
        │
        ├── core/                 #   ★ Singleton services & cross-cutting concerns
        │   ├── services/         #       ApiService, AuthService, ToastService
        │   └── interceptors/     #       AuthInterceptor (withCredentials)
        │
        ├── shared/               #   ★ Reusable UI & utilities across features
        │   ├── shared.module.ts  #       Exports CommonModule, AlertComponent, icons
        │   ├── components/       #       Reusable dumb components (AlertComponent)
        │   ├── models/           #       TypeScript interfaces & types
        │   └── utils/            #       Pure functions (error-mapper, validators, form-helpers)
        │
        └── features/             #   ★ Lazy-loaded feature modules
            ├── landing/          #       Landing page (public, no auth required)
            │   ├── landing.module.ts
            │   ├── landing-routing.module.ts
            │   ├── pages/        #         Page-level components (composition roots)
            │   └── components/   #         Section components (navbar, hero, features, etc.)
            │
            ├── auth/             #       Authentication pages
            │   ├── auth.module.ts
            │   ├── auth-routing.module.ts
            │   └── pages/        #         Login, Register, VerifyEmail, ForgotPassword
            │
            ├── dashboard/        #   ⬚ NOT YET CREATED — Personal & project dashboards
            ├── projects/         #   ⬚ NOT YET CREATED — Project CRUD, settings, member management
            ├── tasks/            #   ⬚ NOT YET CREATED — Task CRUD, Kanban board view
            └── notifications/    #   ⬚ NOT YET CREATED — In-app notification center
```

#### Rule of thumb for the frontend:
- **`core/`** → Services used app-wide (auth state, API wrapper, toast). Only import in `AppModule`. Singleton services.
- **`shared/`** → Dumb components, models, utility functions. Import in feature modules. Never import `core/` services here.
- **`features/`** → Each business domain gets its own lazy-loaded module. Contains `pages/` (routed, page-level) and `components/` (section-level, reused within the feature). Never import `features/` into each other.
- Each feature module imports `SharedModule` for common components/icons and `ReactiveFormsModule` if it uses forms.

---

### 2.2 Server (`server/`) — Spring Boot Backend Folder Structure

```
server/
├── pom.xml                       # Maven build config (dependencies, plugins, Java 17)
├── Dockerfile                    # Multi-stage Docker build (Maven → JRE)
├── mvnw / mvnw.cmd               # Maven wrapper scripts
├── HELP.md                       # Spring Boot generated help
│
├── .env                          # Environment variables (read by application.yaml at runtime)
├── .env.example                  # Template for environment variables
│
├── target/                       # Build artifacts (JAR, compiled classes)
│
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.yaml  # All Spring Boot config (DB, Redis, Mail, JWT, CORS)
    │   │
    │   └── java/com/inpt/collaborationplatform/
    │       ├── CollaborationPlatformApplication.java   # @SpringBootApplication entry point
    │       │
    │       ├── auth/             #   ★ Authentication domain
    │       │   ├── entity/       #       JPA entities (User, Role enum)
    │       │   ├── repository/   #       Spring Data repositories (UserRepository)
    │       │   ├── dto/          #       Request/Response DTOs
    │       │   │   ├── request/  #         RegisterRequest, LoginRequest, VerifyCodeRequest
    │       │   │   └── response/ #         AuthResponse
    │       │   ├── service/      #       Business logic (AuthService, EmailService)
    │       │   └── controller/   #       REST endpoints (AuthController — /api/auth)
    │       │
    │       ├── projects/         #   ⬚ NOT YET CREATED — Project domain (entity, repo, dto, service, controller)
    │       ├── tasks/            #   ⬚ NOT YET CREATED — Task domain
    │       ├── teams/            #   ⬚ NOT YET CREATED — Team & role domain
    │       └── notifications/    #   ⬚ NOT YET CREATED — Notification domain
    │       │
    │       └── shared/           #   ★ Cross-cutting infrastructure
    │           ├── config/       #       Bean configurations (CorsConfig, RedisConfig)
    │           ├── dto/          #       Shared DTOs (MessageResponse)
    │           ├── exception/    #       Custom exceptions & GlobalExceptionHandler
    │           └── security/     #       JWT, cookies, filters, SecurityConfig
    │
    └── test/
        └── java/com/inpt/collaborationplatform/
            └── CollaborationPlatformApplicationTests.java   # Default context load test
```

#### Rule of thumb for the backend:
- **Domain packages** (`auth/`, `projects/`, `tasks/`, `teams/`, `notifications/`) → Each business domain has its own package containing `entity/`, `repository/`, `dto/` (with `request/` and `response/` sub-packages), `service/`, and `controller/`. Domains never import each other's internals — they reference only entity classes by ID.
- **`shared/`** → Infrastructure used by all domain packages: security (JWT, filters, cookies), configuration (CORS, Redis), shared DTOs, and the global exception handler. Domain services may import `shared/` classes but `shared/` never imports domain packages.
- **DTOs are split by direction** → `request/` for incoming payloads (with validation annotations), `response/` for outgoing payloads. Never use entities directly as request/response bodies.
- **Controllers are thin** → They validate input, call a service method, and return the response. All business logic lives in services.
- **Test structure mirrors `main/`** → Each domain should have corresponding test classes in the same package structure under `test/`.

---

### 2.3 Standard Package Layout (New Domain Example)

When adding a new domain (e.g., `projects/`), follow this exact pattern:

```
com.inpt.collaborationplatform.projects/
├── entity/
│   ├── Project.java             # JPA entity with @Entity, @Table, relationships
│   └── ProjectStatus.java       # Enum (ACTIVE, ARCHIVED, etc.)
├── repository/
│   └── ProjectRepository.java   # Extends JpaRepository<Project, UUID>
├── dto/
│   ├── request/
│   │   ├── CreateProjectRequest.java    # @NotBlank, @Size, etc.
│   │   └── UpdateProjectRequest.java
│   └── response/
│       └── ProjectResponse.java         # Safe DTO (no internal fields exposed)
├── service/
│   └── ProjectService.java      # @Service — business logic, transaction management
└── controller/
    └── ProjectController.java   # @RestController — /api/projects CRUD endpoints
```

---

## 3. MVP Specification (5 Phases)

### PHASE 1 : Fondations
- Authentification : Inscription → Confirmation email → Connexion → Déconnexion → Reset mot de passe
- Gestion des Projets : Créer → Modifier → Archiver → Liste des projets

### PHASE 2 : Cœur Métier
- Gestion des Tâches : Créer → Lire → Modifier → Supprimer → Priorité + Échéance
- Vue Kanban : Affichage par statut → Drag & Drop

### PHASE 3 : Suivi & Visibilité
- Dashboard : Dashboard personnel → Dashboard projet
- Notifications : In-app → Email

### PHASE 4 : Collaboration
- Équipes & Rôles : Invitations → 4 rôles → Équipes multiples

### PHASE 5 : Valeur Ajoutée
- Recherche & Filtres : Recherche globale → Filtres Kanban

---

## 3. Current Implementation Status

**Completed:** Only a subset of **Phase 1** — Authentication is fully implemented (register, verify email, login, logout, token refresh). Password reset is UI-only with no backend API call.

**Not implemented (everything else):** Projects CRUD, Tasks CRUD, Kanban board, Dashboards, Notifications (in-app + email), Teams & Roles system, Search & Filters, File storage, WebSocket/real-time, Rate limiting, Pagination, Password reset API.

---

## 4. Complete Project Tree (Root Level)

```
C:\Users\USER\Desktop\ASEDS INE2\PFA\
├── .env                                  # Shared environment variables for Docker Compose
├── .env.example                          # Example environment variables template
├── .git/
├── .github/
├── .gitignore
├── .vscode/
├── docker-compose.yml                    # Orchestrates db (postgres), redis, server
├── README.md                             # Project readme (binary)
├── client/                               # Angular 21 frontend
└── server/                               # Spring Boot 3.5 backend
```

---

## 5. Frontend Architecture — `client/` (Angular 21.2.11)

### 5.1 Root Configuration Files

#### `client/package.json`
- **name**: "client", **version**: "0.0.0", **private**: true
- **packageManager**: npm@11.13.0
- **Scripts**: `ng` (ng), `start` (ng serve), `build` (ng build), `watch` (ng build --watch --configuration development), `test` (ng test)
- **Dependencies**: @angular/animations ^21.2.11, @angular/common 21.2.11, @angular/compiler 21.2.11, @angular/core 21.2.11, @angular/forms 21.2.11, @angular/platform-browser 21.2.11, @angular/platform-browser-dynamic 21.2.11, @angular/router 21.2.11, lucide-angular ^0.525.0, ngx-toastr ^20.0.5, rxjs ~7.8.0, sweetalert2 ^11.26.25, tslib ^2.3.0
- **DevDependencies**: @angular/build 21.2.9, @angular/cli 21.2.9, @angular/compiler-cli 21.2.11, jsdom ^28.0.0, prettier ^3.8.1, typescript ~5.9.2, vitest ^4.0.8

#### `client/angular.json`
- Builder: `@angular/build:application`
- Root: `./src`
- Main entry: `src/main.ts`
- Polyfills: `zone.js`
- Styles: `src/styles.scss`
- Assets: `public/*` (glob pattern, copied to build output)
- Prefix: `app`
- Budgets: Initial 500kB warning / 1MB error; Component style 4kB warning / 8kB error
- Configurations: `production` (output hashing, optimization, source maps off) / `development` (source maps, no optimization)
- Server: port 4200 by default

#### `client/tsconfig.json`
- **compilerOptions**: strict mode, ES2022 target, `preserve` module, ES2022 module resolution
- **angularCompilerOptions**: strictTemplates, strictInjectionParameters, strictInputAccessModifiers

#### `client/tsconfig.app.json`
- Extends `tsconfig.json`, includes `src/**/*.ts`, excludes `*.spec.ts`, rootDir `./src`

#### `client/tsconfig.spec.json`
- Extends `tsconfig.json`, includes `**/*.d.ts` and `**/*.spec.ts`, types `vitest/globals`

#### `client/.editorconfig`, `client/.prettierrc`, `client/.gitignore`, `client/.vscode/`
- Standard IDE and formatting configuration

#### `client/.env`
- Contains: `AUTH_API_BASE_URL=http://localhost:8080/api/auth`
- **Note:** This file is NOT consumed by Angular CLI (Angular does not auto-load .env). The URL is hardcoded in source code instead.

#### `client/.env.example`
- Contains: `AUTH_API_BASE_URL=http://localhost:8080/api/auth`

---

### 5.2 Public Assets

#### `client/public/favicon.ico`
- Browser tab icon

#### `client/public/assets/images/`
- `icon-back.png` — Back arrow icon
- `icon-brand.png` — Brand logo icon
- `icon-email-input.png` — Email input field decoration
- `icon-github-light.png` — GitHub icon (light variant for social login)
- `icon-github.png` — GitHub icon (dark variant for social login)
- `icon-google.png` — Google icon for social login
- `icon-mail.png` — Mail icon for email verification
- `icon-star.png` — Star icon for testimonials/pricing
- `landing-dashboard.svg` — Dashboard preview illustration on landing
- `landing-workflow.svg` — Workflow preview illustration on landing
- `login-avatar.png` — Avatar image on login page
- `login-bg.png` — Background image for login page
- `register-bg.png` — Background image for register page

---

### 5.3 Global Styles Architecture

#### `client/src/styles.scss`
- Root SCSS entry point. Imports `styles/styles.scss` and Google Fonts (Inter).

#### `client/src/styles/styles.scss`
- Master stylesheet loader that imports all partials in order.

##### `client/src/styles/abstracts/`
- **`_colors.scss`** — Color palette: `$navy-900` (#0a1929), `$blue-600` (#1976d2), `$blue-500` (#2196f3), `$blue-100` (#bbdefb), `$gray-50` (#f8f9fa), `$gray-100` (#f0f0f0), `$gray-200` (#e0e0e0), `$gray-300` (#bdbdbd), `$gray-500` (#9e9e9e), `$gray-600` (#757575), `$gray-700` (#616161), `$gray-900` (#212121), `$green-500` (#4caf50), `$red-500` (#f44336), `$orange-500` (#ff9800), `$white` (#ffffff), `$black` (#000000)
- **`_variables.scss`** — Design tokens: `$font-family` (Inter, system-ui), `$font-size-xs` (0.75rem), `$font-size-sm` (0.875rem), `$font-size-base` (1rem), `$font-size-lg` (1.125rem), `$font-size-xl` (1.25rem), `$font-size-2xl` (1.5rem), `$font-size-3xl` (1.875rem), `$font-size-4xl` (2.25rem), `$border-radius-sm` (4px), `$border-radius-md` (8px), `$border-radius-lg` (12px), `$border-radius-xl` (16px), `$border-radius-full` (9999px), `$shadow-sm`, `$shadow-md`, `$shadow-lg`, `$shadow-xl`, `$transition-base` (0.2s ease), `$spacing-unit` (8px)
- **`_functions.scss`** — SCSS functions: `rem($px)` — Converts pixels to rem (base 16px)
- **`_mixins.scss`** — SCSS mixins: `flex-center` (flexbox centering), `text-truncate` (ellipsis), `responsive($breakpoint)` (media queries)

##### `client/src/styles/base/`
- **`_reset.scss`** — CSS reset: box-sizing border-box, margin/padding 0, font smoothing, scroll behavior smooth, focus outlines
- **`_typography.scss`** — Body defaults: font-family Inter, color navy-900, line-height 1.6, heading styles (h1–h6), link styles with hover underline
- **`_globals.scss`** — Global utility classes: `.container` (max-width 1200px, centered), `.text-center`, `.text-gradient` (blue gradient), `.sr-only` (screen reader only)

##### `client/src/styles/themes/`
- **`_light.scss`** — Light theme CSS custom properties on `:root`: `--color-primary` (#1976d2), `--color-primary-hover` (#1565c0), `--color-bg` (#ffffff), `--color-bg-secondary` (#f8f9fa), `--color-text` (#0a1929), `--color-text-secondary` (#616161), `--color-border` (#e0e0e0), `--shadow-sm`/`--shadow-md`/`--shadow-lg`, `--radius-md`/`--radius-lg`/`--radius-xl`

##### `client/src/styles/components/`
- **`_buttons.scss`** — Button system: `.btn`, `.btn--primary` (blue filled), `.btn--outline` (bordered), `.btn--ghost` (transparent), `.btn--danger` (red), `.btn--sm`/`.btn--lg` (sizes), `.btn--full` (100% width), `.btn--icon` (icon-only), hover/focus/disabled states, transition animations
- **`_forms.scss`** — Form system: `.form-group`, `.form-label`, `.form-input` (with focus ring, error state `.form-input--error`), `.form-error` (red error text), `.form-checkbox` (custom styled checkbox), `.otp-container` / `.otp-input` (6-digit OTP grid with individual boxes)
- **`_alerts.scss`** — Alert system: `.alert`, `.alert--success` (green), `.alert--error` (red), icon positioning, fade animation
- **`_auth.scss`** — Auth pages layout: `.auth-page` (full viewport, flex row), `.auth-form` (centered card), `.auth-illustration` (right side image panel), `.auth-divider` (OR separator with lines), `.social-btn` (Google/GitHub buttons), `.otp-section`
- **`_landing.scss`** — Landing page section styles: `.landing-page`, `.hero-section`, `.features-section`, `.workflow-section`, `.pricing-section`, `.contact-section`, `.landing-footer`, section spacing, gradient backgrounds, card hover effects, responsive breakpoints

---

### 5.4 Application Source Code — `client/src/app/`

#### 5.4.1 Bootstrap & Entry Point

##### `client/src/main.ts`
- Calls `platformBrowserDynamic().bootstrapModule(AppModule)`
- Sets up Angular platform browser dynamic bootstrapping

##### `client/src/index.html`
- `<html lang="en">`, meta viewport, title "Collaboration Platform"
- Link to Google Fonts (Inter: 400,500,600,700)
- `<body>` with `<app-root></app-root>` tag

---

#### 5.4.2 Root Module — `app.module.ts`

```typescript
@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
    }),
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
```

---

#### 5.4.3 Root Routing — `app-routing.module.ts`

```typescript
const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./features/landing/landing.module').then(m => m.LandingModule),
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule),
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
```

**Routes:**
- `''` (empty) → lazy loads `LandingModule`
- `'auth'` → lazy loads `AuthModule`
- `'**'` (wildcard) → redirects to `''`

---

#### 5.4.4 Root Component — `app.component.ts` / `app.html` / `app.scss`

##### `app.component.ts`
- Selector: `app-root`
- Template: `app.html`
- Style: `app.scss`
- ChangeDetection: OnPush
- Standalone: false (belongs to AppModule)

##### `app.html`
```html
<div class="app-shell">
  <router-outlet />
</div>
```

##### `app.scss`
- `.app-shell`: min-height 100vh, display flex, flex-direction column

##### `app.spec.ts`
- Basic test: "should create the app" — checks that AppComponent is created successfully

---

#### 5.4.5 Core Layer — `client/src/app/core/`

##### `core/services/api.constants.ts`
```typescript
export const AUTH_API_BASE_URL = 'http://localhost:8080/api/auth';
```
- Hardcoded base URL for all authentication API calls

##### `core/services/api.service.ts`
- **Dependency:** `HttpClient` (from `@angular/common/http`)
- **Provided in:** `root`
- **Methods:**
  - `get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Observable<T>` — Sends GET request to `buildUrl(path)` with optional `HttpParams`
  - `post<T>(path: string, body?: any, params?: Record<string, string | number | boolean | undefined>): Observable<T>` — Sends POST request to `buildUrl(path)` with body and optional params
  - `private buildUrl(path: string): string` — If path starts with `http`, returns as-is (absolute URL). Otherwise prepends `AUTH_API_BASE_URL`.
  - `private buildParams(params: Record<string, string | number | boolean | undefined>): HttpParams` — Converts record to `HttpParams`, filtering out undefined values

##### `core/services/auth.service.ts`
- **Dependency:** `ApiService`
- **Provided in:** `root`
- **State management (BehaviorSubject pattern):**
  - `private authenticatedSubject = new BehaviorSubject<boolean>(false)` → public `isAuthenticated$: Observable<boolean>`
  - `private profileSubject = new BehaviorSubject<UserProfile | null>(null)` → public `profile$: Observable<UserProfile | null>`
- **Methods:**
  - `register(payload: RegisterRequest): Observable<MessageResponse>` — POST `'register'` with body → returns `MessageResponse`
  - `verifyCode(payload: VerifyCodeRequest): Observable<MessageResponse>` — POST `'verify-code'` with body → returns `MessageResponse`
  - `resendCode(email: string): Observable<MessageResponse>` — POST `'resend-code'` with email as query param → returns `MessageResponse`
  - `login(payload: LoginRequest): Observable<AuthResponse>` — POST `'login'` with body, on success: sets `authenticatedSubject.next(true)`, `profileSubject.next(response)`, returns `AuthResponse`
  - `logout(): Observable<MessageResponse>` — POST `'logout'`, on success: resets both subjects to false/null
  - `checkSession(): Observable<MessageResponse>` — POST `'refresh'`, on success: `authenticatedSubject.next(true)`, on error: resets and re-throws
  - `getProfile(): Observable<UserProfile | null>` — GET `'me'`, on success: updates `profileSubject.next(user)`, on error: sets profile to null, returns `of(null)`

##### `core/services/toast.service.ts`
- **Dependency:** `ToastrService` (from `ngx-toastr`)
- **Provided in:** `root`
- **Methods:**
  - `success(message: string, title?: string): void` — Green success toast
  - `error(message: string, title?: string): void` — Red error toast
  - `info(message: string, title?: string): void` — Blue info toast
  - `warning(message: string, title?: string): void` — Yellow warning toast
  - `neutral(message: string, title?: string): void` — Gray neutral toast (custom class `toast-neutral`)

##### `core/interceptors/auth.interceptor.ts`
- **Implements:** `HttpInterceptor`
- **Logic:** Intercepts all outgoing HTTP requests. If `req.url.startsWith(AUTH_API_BASE_URL)`, clones the request with `withCredentials: true` (enables cookie sending). Otherwise passes the request through unchanged.
- **Purpose:** Ensures HttpOnly cookies (access_token, refresh_token) are sent with every API call to the backend.

---

#### 5.4.6 Shared Layer — `client/src/app/shared/`

##### `shared/shared.module.ts`
- **Imports:** `CommonModule`, `LucideAngularModule` (picks 17 icons)
- **Declarations:** `AlertComponent`
- **Exports:** `CommonModule`, `AlertComponent`, `LucideAngularModule`
- **Icons imported:** ArrowLeft, ArrowRight, CheckCircle, ChevronDown, Clock3, Globe, Github, Layers, LayoutDashboard, LogIn, LogOut, Mail, Menu, Settings, ShieldCheck, X, UserPlus

##### `shared/models/auth.models.ts`
```typescript
export interface MessageResponse {
  message: string;
}

export interface AuthResponse {
  id: string;
  email: string;
  role: string;
  username?: string;
}

export interface UserProfile {
  id: string;
  email: string;
  role: string;
  username?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface VerifyCodeRequest {
  email: string;
  code: string;
}
```

##### `shared/components/alert/alert.component.ts`
- **Selector:** `app-alert`
- **Inputs:** `type: 'success' | 'error'`, `message: string | null`
- **ChangeDetection:** OnPush
- **Template:** Conditionally renders `<div class="alert alert--success/alert--error">` with icon and message when `message` is truthy

##### `shared/components/alert/alert.component.html`
- Structure: `<div *ngIf="message" [class]="'alert alert--' + type">` with icon (CheckCircle for success, X for error) and message text, close button

##### `shared/components/alert/alert.component.scss`
- Styles for `.alert`: padding, border-radius (8px), flex layout, icon sizing, color variants (green for success, red for error), close button, slide-in animation

##### `shared/utils/error-mapper.ts`
- **Exports:** `mapHttpError(error: HttpErrorResponse, fallback?: string): string`
- **Logic:** Handles:
  - Status 0 → Network error ("Unable to connect to the server")
  - Parsed JSON error message from `error.error`
  - Field-level validation errors from `error.error.fields`
  - Status codes: 400, 401, 403, 404, 409, 500
  - Generic fallback

##### `shared/utils/validators.ts`
- **Exports:** `matchFields(field: string, confirmField: string): ValidatorFn`
- **Logic:** Custom validator that checks if two form control values match. Used for password confirmation validation.

##### `shared/utils/form-helpers.ts`
- **Exports:**
  - `markAllTouched(control: AbstractControl): void` — Calls `markAllAsTouched()` recursively on the control and all its children
  - `getControlError(control: AbstractControl, label: string): string | null` — Returns human-readable error string for `required`, `email`, or `minlength` validation errors

---

#### 5.4.7 Feature: Landing — `client/src/app/features/landing/`

##### `landing/landing.module.ts`
- **Declarations:** LandingPageComponent, NavbarComponent, HeroSectionComponent, FeaturesSectionComponent, WorkflowSectionComponent, PricingSectionComponent, ContactSectionComponent, FooterComponent
- **Imports:** SharedModule, LandingRoutingModule

##### `landing/landing-routing.module.ts`
```typescript
const routes: Routes = [{ path: '', component: LandingPageComponent }];
```
- Root path inside this module renders `LandingPageComponent`

##### `landing/pages/landing-page/landing-page.component.ts`
- **Selector:** `app-landing-page`
- **Composition:** Renders all landing sections in order: `<app-landing-navbar>`, `<app-hero-section>`, `<app-features-section>`, `<app-workflow-section>`, `<app-pricing-section>`, `<app-contact-section>`, `<app-landing-footer>`
- **ChangeDetection:** OnPush

##### `landing/pages/landing-page/landing-page.component.html`
- Sequential rendering of all 7 child components

##### `landing/pages/landing-page/landing-page.component.scss`
- Minimal: `:host { display: block; }`

##### `landing/components/navbar/navbar.component.ts`
- **Selector:** `app-landing-navbar`
- **Dependencies:** Router, AuthService, ToastService
- **Logic:**
  - Subscribes to `AuthService.profile$` reactively for profile state
  - On init: calls `AuthService.getProfile()` to check existing session
  - Displays state: logged out (Login button) vs logged in (profile badge with initials + dropdown: Dashboard, Settings, Log out)
  - Logout: shows SweetAlert2 confirmation dialog → calls `AuthService.logout()` → navigates to `/auth/login`
  - Mobile: hamburger toggle with slide-out panel
  - `@HostListener('document:click')` — Closes profile dropdown on outside click
  - `initials` getter — Derives initials from username or email first letter
  - `scrollToSection(sectionId: string)` — Smooth scrolls to landing section by ID

##### `landing/components/navbar/navbar.component.html`
- Brand logo ("CP" text), desktop nav links (Features, Workflow, Pricing, Contact Us), auth-aware right section (login button or profile dropdown), mobile hamburger menu

##### `landing/components/navbar/navbar.component.scss`
- Fixed positioning, backdrop blur, flex layout, dropdown positioning, mobile responsive breakpoint, smooth transitions

##### `landing/components/hero-section/hero-section.component.ts`
- **Selector:** `app-hero-section`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational, no inputs or outputs

##### `landing/components/hero-section/hero-section.component.html`
- Headline: "Collaborate Smarter, Build Faster", subtext paragraph, CTA buttons ("Get Started" → `/auth/register`, "Request a Demo" → mailto), dashboard preview SVG image

##### `landing/components/hero-section/hero-section.component.scss`
- Gradient background (navy to blue), large heading with gradient text, responsive grid layout for text + image

##### `landing/components/features-section/features-section.component.ts`
- **Selector:** `app-features-section`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational

##### `landing/components/features-section/features-section.component.html`
- Section title "Why Choose Our Platform?", 3 feature cards with Lucide icons:
  - Layers icon — "Unified Workspaces" — description
  - Clock3 icon — "Realtime Sync" — description
  - ShieldCheck icon — "Enterprise Security" — description

##### `landing/components/features-section/features-section.component.scss`
- 3-column grid, card styles with hover elevation, icon styling

##### `landing/components/workflow-section/workflow-section.component.ts`
- **Selector:** `app-workflow-section`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational

##### `landing/components/workflow-section/workflow-section.component.html`
- Split layout: left side has title + checklist (Roadmaps, Smart updates, Reporting), right side has workflow preview SVG image

##### `landing/components/workflow-section/workflow-section.component.scss`
- Two-column grid, checklist with check icons, responsive stacking

##### `landing/components/pricing-section/pricing-section.component.ts`
- **Selector:** `app-pricing-section`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational

##### `landing/components/pricing-section/pricing-section.component.html`
- Section title "Simple, Transparent Pricing", 3 pricing cards:
  - "Starter" — Free, basic features, "Get Started" button
  - "Growth" — $29/seat, advanced features, "Start Free Trial" button (highlighted)
  - "Enterprise" — Custom pricing, all features, "Contact Us" button

##### `landing/components/pricing-section/pricing-section.component.scss`
- 3-column grid, card styles with shadow, featured card accent border, button variants

##### `landing/components/contact-section/contact-section.component.ts`
- **Selector:** `app-contact-section`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational

##### `landing/components/contact-section/contact-section.component.html`
- Dark background banner: "Ready to Transform Your Workflow?" text + "Contact Us" button → mailto link

##### `landing/components/contact-section/contact-section.component.scss`
- Dark gradient background, white text, centered layout, button contrast

##### `landing/components/footer/footer.component.ts`
- **Selector:** `app-landing-footer`
- **ChangeDetection:** OnPush
- **Logic:** Pure presentational

##### `landing/components/footer/footer.component.html`
- Copyright text, legal links: Privacy Policy, Terms of Service, Security

##### `landing/components/footer/footer.component.scss`
- Dark background, centered text, small font, link styling

---

#### 5.4.8 Feature: Auth — `client/src/app/features/auth/`

##### `auth/auth.module.ts`
- **Declarations:** LoginComponent, RegisterComponent, VerifyEmailComponent, ForgotPasswordComponent
- **Imports:** SharedModule, ReactiveFormsModule, RouterModule, AuthRoutingModule

##### `auth/auth-routing.module.ts`
```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
```

**Full URL paths:**
- `/auth/login` — Login page
- `/auth/register` — Registration page
- `/auth/verify-email?email=...` — Email verification (OTP)
- `/auth/forgot-password` — Forgot password
- `/auth` — Redirects to `/auth/login`

##### `auth/pages/login/login.component.ts`
- **Selector:** `app-login`
- **Dependencies:** FormBuilder, AuthService, ToastService, Router
- **Form fields:** `email` (required, email validator), `password` (required, minLength 8), `rememberMe` (checkbox)
- **Logic:**
  - On submit: calls `AuthService.login({ email, password })` with `markAllTouched()` first
  - On success: toast "Signed in successfully", navigate to `/` after 700ms delay
  - On error: `mapHttpError()` → `ToastService.error()`
  - `emailError` getter: returns "Please enter a valid email" or "Email is required"
  - `passwordError` getter: returns "Password must be at least 8 characters" or "Password is required"
- **Social buttons:** Google and GitHub buttons present but have no click handlers (UI-only)

##### `auth/pages/login/login.component.html`
- Split layout: left side has form (brand logo, title "Welcome back", subtitle, email input, password input, remember me checkbox, login button, "OR" divider, Google + GitHub social buttons, "Don't have an account? Sign up" link, "Forgot password?" link)

##### `auth/pages/login/login.component.scss`
- Auth page layout classes, form card styling, social button grid

##### `auth/pages/register/register.component.ts`
- **Selector:** `app-register`
- **Dependencies:** FormBuilder, AuthService, ToastService, Router
- **Form fields:** `username` (required, minLength 3), `email` (required, email), `password` (required, minLength 8), `confirmPassword` (required)
- **Custom validator:** `matchFields('password', 'confirmPassword')` on the form group
- **Logic:**
  - On submit: calls `AuthService.register({ username, email, password })`
  - On success: toast with server message, navigate to `/auth/verify-email?email=...` after 900ms delay
  - On error: `mapHttpError()` → `ToastService.error()`
  - Error getters: `usernameError`, `emailError`, `passwordError`, `confirmPasswordError` (including mismatch detection with "Passwords do not match")

##### `auth/pages/register/register.component.html`
- Split layout: left side has form (brand logo, title "Create Account", subtitle, username input, email input, password input, confirm password input, register button, "OR" divider, Google + GitHub buttons, "Already have an account? Sign in" link)

##### `auth/pages/register/register.component.scss`
- Auth page layout classes, similar to login styling

##### `auth/pages/verify-email/verify-email.component.ts`
- **Selector:** `app-verify-email`
- **Dependencies:** FormBuilder, AuthService, ToastService, Router, ActivatedRoute
- **Form fields:** `email` (pre-filled from query param, hidden if present), `code1` through `code6` (6 individual digit inputs, each maxLength 1)
- **Query param:** reads `email` from route query params
- **Logic:**
  - On submit: concatenates 6 digit controls into a single code string, calls `AuthService.verifyCode({ email, code })`
  - On success: toast, navigate to `/auth/login`
  - On error: `mapHttpError()` → `ToastService.error()`
  - `resendCode()`: calls `AuthService.resendCode(email)` with loading state
  - **OTP UX features:**
    - Auto-focus next input on digit entry (keyup)
    - Backspace goes to previous input and clears current
    - Arrow keys (ArrowLeft, ArrowRight) navigate between inputs
    - Paste event: parses 6-digit string from clipboard into all 6 fields
  - `@ViewChildren('otpInput')` — `QueryList<ElementRef>` for DOM access to OTP inputs

##### `auth/pages/verify-email/verify-email.component.html`
- Centered card: brand icon, title "Verify your email", subtitle with email display, 6 OTP input boxes in a row, "Verify Email" button, "Resend Code" link with loading spinner

##### `auth/pages/verify-email/verify-email.component.scss`
- OTP container grid, individual input styling (large, centered text, border focus), resend link

##### `auth/pages/forgot-password/forgot-password.component.ts`
- **Selector:** `app-forgot-password`
- **Dependencies:** FormBuilder, ToastService, Router
- **Form field:** `email` (required, email)
- **Logic:** On submit, if valid, shows success toast "If an account exists, a reset link has been sent to this email."
- **Note:** This component does NOT make any API call. It's entirely UI-only.

##### `auth/pages/forgot-password/forgot-password.component.html`
- Centered card: mail icon, title "Forgot Password?", subtitle, email input, "Send Reset Link" button, "Back to Login" link

##### `auth/pages/forgot-password/forgot-password.component.scss`
- Auth page styling, centered layout

---

### 5.5 Frontend Libraries — Complete Usage Map

| Library | Version | Imported/Used In | Purpose |
|---------|---------|-----------------|---------|
| `@angular/core` | 21.2.11 | Every component, service, module | Framework core (components, DI, lifecycle, signals, ChangeDetection) |
| `@angular/common` | 21.2.11 | SharedModule, all templates | CommonModule (ngIf, ngFor, etc.), ngClass, pipes |
| `@angular/forms` | 21.2.11 | AuthModule (LoginComponent, RegisterComponent, VerifyEmailComponent, ForgotPasswordComponent) | ReactiveFormsModule, FormBuilder, Validators, FormGroup, FormControl |
| `@angular/router` | 21.2.11 | AppRoutingModule, LandingRoutingModule, AuthRoutingModule, NavbarComponent | RouterModule, Router, ActivatedRoute, Routes, Navigation |
| `@angular/platform-browser` | 21.2.11 | AppModule | BrowserModule, BrowserAnimationsModule |
| `@angular/common/http` | 21.2.11 | AppModule (HttpClientModule), ApiService, AuthInterceptor | HttpClient, HttpParams, HttpErrorResponse, HttpInterceptor |
| `@angular/animations` | 21.2.11 | AppModule | BrowserAnimationsModule (enables ngx-toastr animations) |
| `@angular/platform-browser-dynamic` | 21.2.11 | main.ts | platformBrowserDynamic (bootstrap) |
| `rxjs` | ~7.8.0 | AuthService (BehaviorSubject, catchError, tap, finalize, of, throwError, Observable) | Reactive state management, HTTP observable handling |
| `ngx-toastr` | ^20.0.5 | AppModule (ToastrModule.forRoot), ToastService | Toast notifications (success, error, info, warning) |
| `sweetalert2` | ^11.26.25 | NavbarComponent | Confirmation dialog for logout |
| `lucide-angular` | ^0.525.0 | SharedModule (17 icons) | SVG icon set for UI |
| `@angular/build` (dev) | 21.2.9 | angular.json (builder) | Build system |
| `@angular/cli` (dev) | 21.2.9 | angular.json | CLI tooling |
| `@angular/compiler-cli` (dev) | 21.2.9 | tsconfig.json | Template type-checking |
| `vitest` (dev) | ^4.0.8 | tsconfig.spec.json, tests | Unit test runner |
| `jsdom` (dev) | ^28.0.0 | tsconfig.spec.json | DOM environment for tests |
| `prettier` (dev) | ^3.8.1 | .prettierrc | Code formatting |
| `typescript` (dev) | ~5.9.2 | tsconfig.json | Language |

---

## 6. Backend Architecture — `server/` (Spring Boot 3.5 + Java 17)

### 6.1 Root Configuration Files

#### `server/pom.xml`
- **Group/Artifact:** com.inpt / server
- **Name:** collaboration-platform
- **Version:** 0.0.1-SNAPSHOT
- **Java version:** 17
- **Parent:** spring-boot-starter-parent 3.5.14

#### `server/Dockerfile`
- **Multi-stage build:**
  - **Stage 1 (build):** `maven:3.9-eclipse-temurin-17`, copies `pom.xml` then `src/`, runs `mvn -q -DskipTests package`
  - **Stage 2 (runtime):** `eclipse-temurin:17-jre`, copies JAR from build stage, exposes port 8080, entrypoint `java -jar /app/app.jar`

#### `server/mvnw` / `server/mvnw.cmd`
- Maven wrapper scripts for building without system Maven

#### `server/HELP.md`
- Spring Boot generated help file

#### `server/.gitignore`, `server/.gitattributes`, `server/.idea/`
- IDE and version control configuration

#### `server/.env`
- **Contains:**
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/CP`
  - `SPRING_DATASOURCE_USERNAME=postgres`
  - `SPRING_DATASOURCE_PASSWORD=postgres`
  - `SPRING_JPA_HIBERNATE_DDL_AUTO=create`
  - `SPRING_JPA_SHOW_SQL=true`
  - `SPRING_DATA_REDIS_HOST=redis`
  - `SPRING_DATA_REDIS_PORT=6379`
  - `SPRING_MAIL_HOST=smtp.gmail.com`
  - `SPRING_MAIL_PORT=587`
  - `SPRING_MAIL_USERNAME=yassinecool71@gmail.com`
  - `SPRING_MAIL_PASSWORD=feuzmgknpgepycfn` (Gmail App Password)
  - `APP_JWT_SECRET=Tzaa599GPWUzpQc/84CqVhiRnZt7+jIehtmC6UgSdxM=` (Base64 HMAC key)
  - `APP_JWT_ACCESS_TOKEN_EXPIRATION=900000` (15 min in ms)
  - `APP_JWT_REFRESH_TOKEN_EXPIRATION=604800000` (7 days in ms)
  - `APP_FRONTEND_URL=http://localhost:4200`
  - `APP_EMAIL_VERIFICATION_EXPIRY_HOURS=24`
  - `APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000`

#### `server/.env.example`
- Same structure as `.env` but with placeholder values (`you@example.com`, `your-app-password`, `replace-with-strong-secret`)

---

### 6.2 Application Configuration

#### `server/src/main/resources/application.yaml`

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO}
    show-sql: ${SPRING_JPA_SHOW_SQL}
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT}
  mail:
    host: ${SPRING_MAIL_HOST}
    port: ${SPRING_MAIL_PORT}
    username: ${SPRING_MAIL_USERNAME}
    password: ${SPRING_MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
app:
  jwt:
    secret: ${APP_JWT_SECRET}
    access-token-expiration: ${APP_JWT_ACCESS_TOKEN_EXPIRATION}
    refresh-token-expiration: ${APP_JWT_REFRESH_TOKEN_EXPIRATION}
  frontend-url: ${APP_FRONTEND_URL}
  email-verification-expiry-hours: ${APP_EMAIL_VERIFICATION_EXPIRY_HOURS}
  cors:
    allowed-origins: ${APP_CORS_ALLOWED_ORIGINS}
```

All values are injected from environment variables (loaded via `.env` file import).

---

### 6.3 Source Code — `server/src/main/java/com/inpt/collaborationplatform/`

#### `CollaborationPlatformApplication.java`
- **Package:** `com.inpt.collaborationplatform`
- **Annotations:** `@SpringBootApplication`
- **Purpose:** Main entry point with `public static void main(String[] args)` calling `SpringApplication.run()`

---

### 6.4 Auth Package — `auth/`

#### `auth/entity/User.java`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Entity`, `@Table(name = "users")`
- **Fields:**
  - `id: String` — `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)` → UUID primary key
  - `email: String` — `@Column(unique = true, nullable = false)` → unique, required
  - `username: String` — `@Column(unique = true, nullable = false)` → unique, required
  - `password: String` — `@Column(nullable = false)` → BCrypt-encoded
  - `enabled: boolean` — `@Builder.Default = false` → email verification gate
  - `verificationCode: String` — nullable, stores 6-digit OTP as string (preserves leading zeros)
  - `verificationCodeExpiry: LocalDateTime` — nullable, OTP expiration timestamp
  - `role: Role` — `@Enumerated(EnumType.STRING)`, `@Builder.Default = Role.USER` → USER or ADMIN
  - `createdAt: LocalDateTime` — set via `@PrePersist` method
- **Relationships:** None (standalone entity)
- **Table:** `users`

#### `auth/entity/Role.java`
- **Package:** `com.inpt.collaborationplatform.auth.entity`
- **Type:** Enum
- **Values:** `USER`, `ADMIN`
- **Usage:** Stored as string in database via `@Enumerated(EnumType.STRING)`

#### `auth/repository/UserRepository.java`
- **Package:** `com.inpt.collaborationplatform.auth.repository`
- **Extends:** `JpaRepository<User, String>`
- **Custom methods:**
  - `Optional<User> findByEmail(String email)` — Lookup by email
  - `boolean existsByEmail(String email)` — Check email existence
  - `boolean existsByUsername(String username)` — Check username existence

#### `auth/dto/request/RegisterRequest.java`
- **Package:** `com.inpt.collaborationplatform.auth.dto.request`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Fields:**
  - `username: String` — `@NotBlank`, `@Size(min = 3)`
  - `email: String` — `@NotBlank`, `@Email`
  - `password: String` — `@NotBlank`, `@Size(min = 8)`

#### `auth/dto/request/LoginRequest.java`
- **Package:** `com.inpt.collaborationplatform.auth.dto.request`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Fields:**
  - `email: String` — `@NotBlank`, `@Email`
  - `password: String` — `@NotBlank`

#### `auth/dto/request/VerifyCodeRequest.java`
- **Package:** `com.inpt.collaborationplatform.auth.dto.request`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Fields:**
  - `email: String` — `@NotBlank`, `@Email`
  - `code: String` — `@NotBlank`, `@Size(min = 6, max = 6)`, `@Pattern(regexp = "\\d{6}")` (exactly 6 digits)

#### `auth/dto/response/AuthResponse.java`
- **Package:** `com.inpt.collaborationplatform.auth.dto.response`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Fields:** `id: String`, `email: String`, `role: String`, `username: String`
- **Usage:** Returned on login and GET /me endpoints

#### `auth/controller/AuthController.java`
- **Package:** `com.inpt.collaborationplatform.auth.controller`
- **Annotations:** `@RestController`, `@RequestMapping("/api/auth")`
- **Dependencies:** AuthService
- **Endpoints:**

| HTTP | Route | Method Signature | Response | Auth |
|------|-------|-----------------|----------|------|
| POST | `/api/auth/register` | `register(@RequestBody @Valid RegisterRequest)` | `ResponseEntity<MessageResponse>` (201) | No |
| POST | `/api/auth/verify-code` | `verifyCode(@RequestBody @Valid VerifyCodeRequest)` | `ResponseEntity<MessageResponse>` (200) | No |
| POST | `/api/auth/resend-code` | `resendCode(@RequestParam String email)` | `ResponseEntity<MessageResponse>` (200) | No |
| POST | `/api/auth/login` | `login(@RequestBody @Valid LoginRequest, HttpServletResponse)` | `ResponseEntity<AuthResponse>` (200) | No |
| POST | `/api/auth/refresh` | `refresh(HttpServletRequest, HttpServletResponse)` | `ResponseEntity<MessageResponse>` (200) | Cookie |
| POST | `/api/auth/logout` | `logout(HttpServletRequest, HttpServletResponse)` | `ResponseEntity<MessageResponse>` (200) | JWT |
| GET | `/api/auth/me` | `me(Authentication authentication)` | `ResponseEntity<AuthResponse>` (200) | JWT |

#### `auth/service/AuthService.java`
- **Package:** `com.inpt.collaborationplatform.auth.service`
- **Annotations:** `@Service`, `@RequiredArgsConstructor`
- **Dependencies injected:**
  - `UserRepository userRepository`
  - `PasswordEncoder passwordEncoder`
  - `JwtService jwtService`
  - `EmailService emailService`
  - `RedisTemplate<String, String> redisTemplate`
  - `CookieService cookieService`
  - `@Value("${app.email-verification-expiry-hours}") int expiryHours`
- **Methods:**

**`register(RegisterRequest request): MessageResponse`**
1. Check `existsByEmail` → throw `EmailAlreadyExistsException` (409)
2. Check `existsByUsername` → throw `EmailAlreadyExistsException` (409)
3. Generate 6-digit code via `String.format("%06d", new SecureRandom().nextInt(999999))`
4. Build User: `builder().email().username().password(encoded).enabled(false).verificationCode(code).verificationCodeExpiry(now + 10 min).role(USER)`
5. Save user
6. Send verification email via `EmailService.sendVerificationCode()`
7. Return `MessageResponse("Registration successful. Please check your email...")`

**`verifyCode(VerifyCodeRequest request): MessageResponse`**
1. Find user by email → throw `InvalidTokenException` (401) if not found
2. If already enabled → return success
3. If code mismatch → throw `InvalidTokenException` (401)
4. If expiry is before now → throw `InvalidTokenException` (401)
5. Set `enabled = true`, clear `verificationCode` and `verificationCodeExpiry`
6. Save user
7. Return `MessageResponse("Email verified successfully. You can now log in.")`

**`resendCode(String email): MessageResponse`**
1. Find user by email → throw `InvalidTokenException` (401) if not found
2. If already enabled → return success
3. Generate new 6-digit code
4. Set new code and expiry (now + 10 min)
5. Save user
6. Send new verification email
7. Return `MessageResponse("New verification code sent to your email.")`

**`login(LoginRequest request, HttpServletResponse response): AuthResponse`**
1. Find user by email → throw `BadCredentialsException` (401 vague message)
2. Check password with `passwordEncoder.matches()` → throw `BadCredentialsException` (401)
3. Check `enabled` → throw `AccountNotVerifiedException` (403)
4. Generate access token (15 min) and refresh token (7 days) via `JwtService`
5. Hash refresh token with `DigestUtils.sha256Hex()`
6. Store hash in Redis: `redisTemplate.opsForValue().set("refresh:" + userId, hash, refreshExpiration, TimeUnit.MILLISECONDS)`
7. Set cookies via `CookieService`
8. Return `AuthResponse(id, email, role, username)`

**`refresh(HttpServletRequest request, HttpServletResponse response): MessageResponse`**
1. Extract `refresh_token` from cookies
2. Validate JWT → throw `InvalidTokenException` (401) if expired/invalid
3. Lookup `refresh:<userId>` in Redis → throw `InvalidTokenException` (401) if not found
4. Hash the provided token and compare to stored hash → throw `InvalidTokenException` (401) if mismatch
5. Load user from DB
6. Generate new access token + refresh token
7. Hash new refresh token, overwrite Redis key (rotation)
8. Set new cookies
9. Return `MessageResponse("Token refreshed successfully")`

**`logout(HttpServletRequest request, HttpServletResponse response): MessageResponse`**
1. Extract `access_token` from cookies
2. Extract JTI from access token
3. Get remaining TTL from token
4. Store in Redis: `redisTemplate.opsForValue().set("blacklist:" + jti, "true", remainingTtlMs, TimeUnit.MILLISECONDS)`
5. Extract user ID from access token
6. Delete `refresh:<userId>` from Redis
7. Clear both cookies via `CookieService`
8. Return `MessageResponse("Logged out successfully")`

**Private helpers:**
- `hashToken(String token): String` — `DigestUtils.sha256Hex(token)`
- `extractCookie(HttpServletRequest, String name): String` — Parses `Cookie` header manually
- `getCurrentUserId(): String` — Reads from `SecurityContextHolder.getContext().getAuthentication()`

#### `auth/service/EmailService.java`
- **Package:** `com.inpt.collaborationplatform.auth.service`
- **Annotations:** `@Service`, `@RequiredArgsConstructor`
- **Dependencies:** `JavaMailSender`, `@Value("${spring.mail.username}") String fromEmail`
- **Method:**
  - `sendVerificationCode(String toEmail, String code): void`
    1. Create `SimpleMailMessage`
    2. Set from: `fromEmail`
    3. Set to: `toEmail`
    4. Set subject: "Your verification code"
    5. Set text: "Your verification code is: " + code + " (expires in 10 minutes)"
    6. Send via `JavaMailSender.send()`
    7. Log success
    8. On exception: log error, throw `RuntimeException("Failed to send email")`

---

### 6.5 Shared Package — `shared/`

#### `shared/config/CorsConfig.java`
- **Package:** `com.inpt.collaborationplatform.shared.config`
- **Annotations:** `@Configuration`
- **Bean:** `CorsConfigurationSource`
- **Configuration:**
  - `allowedOrigins`: from `${app.cors.allowed-origins}` (comma-separated)
  - `allowedMethods`: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - `allowedHeaders`: Authorization, Content-Type, Accept
  - `allowCredentials`: true (enables cookies)
  - Applied to path pattern `/**`

#### `shared/config/RedisConfig.java`
- **Package:** `com.inpt.collaborationplatform.shared.config`
- **Annotations:** `@Configuration`
- **Bean:** `RedisTemplate<String, String>`
- **Configuration:**
  - Key serializer: `StringRedisSerializer`
  - Value serializer: `StringRedisSerializer`
  - Connection factory: injected via Spring Boot auto-configuration

#### `shared/dto/MessageResponse.java`
- **Package:** `com.inpt.collaborationplatform.shared.dto`
- **Annotations:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Fields:** `message: String`
- **Purpose:** Generic wrapper for status/success response messages

#### `shared/exception/EmailAlreadyExistsException.java`
- **Package:** `com.inpt.collaborationplatform.shared.exception`
- **Extends:** `RuntimeException`
- **Constructor:** `EmailAlreadyExistsException(String message)`
- **Purpose:** Thrown when email or username already exists (→ 409 Conflict)

#### `shared/exception/AccountNotVerifiedException.java`
- **Package:** `com.inpt.collaborationplatform.shared.exception`
- **Extends:** `RuntimeException`
- **Constructor:** `AccountNotVerifiedException(String message)`
- **Purpose:** Thrown when unverified user tries to login (→ 403 Forbidden)

#### `shared/exception/InvalidTokenException.java`
- **Package:** `com.inpt.collaborationplatform.shared.exception`
- **Extends:** `RuntimeException`
- **Constructor:** `InvalidTokenException(String message)`
- **Purpose:** Thrown for invalid/expired verification codes or refresh tokens (→ 401 Unauthorized)

#### `shared/exception/GlobalExceptionHandler.java`
- **Package:** `com.inpt.collaborationplatform.shared.exception`
- **Annotations:** `@RestControllerAdvice`
- **Exception handlers:**

| Exception | HTTP Status | Response Body |
|-----------|-------------|---------------|
| `EmailAlreadyExistsException` | 409 CONFLICT | `{ "status": 409, "error": "Conflict", "message": ..., "timestamp": ... }` |
| `InvalidTokenException` | 401 UNAUTHORIZED | `{ "status": 401, "error": "Unauthorized", "message": ..., "timestamp": ... }` |
| `AccountNotVerifiedException` | 403 FORBIDDEN | `{ "status": 403, "error": "Forbidden", "message": ..., "timestamp": ... }` |
| `BadCredentialsException` | 401 UNAUTHORIZED | Always "Invalid email or password" (prevents info leakage) |
| `MethodArgumentNotValidException` | 400 BAD REQUEST | `{ "status": 400, "error": "Validation Failed", "fields": { "fieldName": "error message" }, "timestamp": ... }` |
| `Exception` (catch-all) | 500 INTERNAL SERVER ERROR | `{ "status": 500, "error": "Internal Server Error", "message": "An unexpected error occurred", "timestamp": ... }` |

#### `shared/security/SecurityConfig.java`
- **Package:** `com.inpt.collaborationplatform.shared.security`
- **Annotations:** `@Configuration`, `@EnableWebSecurity`
- **Beans:**
  - `SecurityFilterChain securityFilterChain(HttpSecurity http)`: Configures HTTP security
    - Disables CSRF
    - Sets session management to `STATELESS`
    - Sets CORS to use `CorsConfigurationSource` bean
    - Authorizes: `/api/auth/**` → `permitAll()`, any other request → `authenticated()`
    - Adds `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
  - `PasswordEncoder passwordEncoder()`: Returns `BCryptPasswordEncoder`
  - `AuthenticationManager authenticationManager(AuthenticationConfiguration)`: From config

#### `shared/security/JwtService.java`
- **Package:** `com.inpt.collaborationplatform.shared.security`
- **Annotations:** `@Service`
- **Dependencies:**
  - `@Value("${app.jwt.secret}") String secret` — Base64-encoded HMAC key
  - `@Value("${app.jwt.access-token-expiration}") long accessExpiration` — in ms
  - `@Value("${app.jwt.refresh-token-expiration}") long refreshExpiration` — in ms
- **Methods:**
  - `private SecretKey getSigningKey()` — Decodes base64 secret, returns `Keys.hmacShaKeyFor(decoded)`
  - `generateAccessToken(User user): String` — Creates JWT with: `jti` (UUID), `sub` (user ID), `email` claim, `role` claim, `iat`, `exp` (now + accessExpiration ms)
  - `generateRefreshToken(User user): String` — Creates JWT with: `jti` (UUID), `sub` (user ID), `iat`, `exp` (now + refreshExpiration ms) — no extra claims
  - `extractAllClaims(String token): Claims` — Parses/verifies JWT with `Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()`
  - `extractUserId(String token): String` — Returns `getSubject()` from claims
  - `extractJti(String token): String` — Returns `getId()` from claims
  - `extractExpiration(String token): Date` — Returns `getExpiration()` from claims
  - `isTokenExpired(String token): boolean` — `extractExpiration().before(new Date())`
  - `getRemainingTtl(String token): long` — `extractExpiration().getTime() - System.currentTimeMillis()`
- **Algorithm:** HMAC-SHA (inferred from `Keys.hmacShaKeyFor`)

#### `shared/security/JwtAuthFilter.java`
- **Package:** `com.inpt.collaborationplatform.shared.security`
- **Extends:** `OncePerRequestFilter`
- **Annotations:** `@RequiredArgsConstructor`
- **Dependencies:** `JwtService`, `UserRepository`, `RedisTemplate<String, String>`
- **Filter logic (doFilterInternal):**
  1. Extract `access_token` cookie from request
  2. If no token → `filterChain.doFilter()` (skip, request proceeds unauthenticated)
  3. If token present:
     a. Try to parse claims via `JwtService.extractAllClaims(token)`
     b. Check Redis: if `blacklist:<jti>` exists → skip auth (token was revoked)
     c. Load User from DB by `sub` claim
     d. If user exists and is enabled:
        - Create `UsernamePasswordAuthenticationToken` with `SimpleGrantedAuthority("ROLE_" + user.getRole().name())`
        - Set `SecurityContextHolder.getContext().setAuthentication(auth)`
     e. On `JwtException` → skip without setting auth
  4. Always: `filterChain.doFilter()` (even if no auth, filter chain continues)

#### `shared/security/CookieService.java`
- **Package:** `com.inpt.collaborationplatform.shared.security`
- **Annotations:** `@Service`
- **Dependencies:**
  - `@Value("${app.jwt.access-token-expiration}") long accessExpiration`
  - `@Value("${app.jwt.refresh-token-expiration}") long refreshExpiration`
- **Methods:**
  - `createAccessTokenCookie(String token): ResponseCookie` — name=`access_token`, HttpOnly, secure=false, SameSite=Lax, path=/, maxAge=accessExpiration/1000
  - `createRefreshTokenCookie(String token): ResponseCookie` — name=`refresh_token`, HttpOnly, secure=true, SameSite=Lax, path=/api/auth/refresh, maxAge=refreshExpiration/1000
  - `clearAccessTokenCookie(): ResponseCookie` — name=`access_token`, empty value, path=/, maxAge=0
  - `clearRefreshTokenCookie(): ResponseCookie` — name=`refresh_token`, empty value, path=/api/auth/refresh, maxAge=0

---

### 6.6 Test Files

#### `server/src/test/java/com/inpt/collaborationplatform/CollaborationPlatformApplicationTests.java`
- Default Spring Boot test with `@SpringBootTest` and empty `contextLoads()` method

---

### 6.7 Backend Libraries — Complete Usage Map

| Dependency | GroupId | Version | Used In | Purpose |
|------------|---------|---------|---------|---------|
| `spring-boot-starter-data-jpa` | org.springframework.boot | 3.5.14 (parent) | User (JPA), UserRepository | Hibernate ORM, JPA repositories, entity management |
| `spring-boot-starter-data-redis` | org.springframework.boot | 3.5.14 | RedisConfig, AuthService, JwtAuthFilter | RedisTemplate, token storage, blacklist |
| `spring-boot-starter-security` | org.springframework.boot | 3.5.14 | SecurityConfig, JwtAuthFilter, AuthService | Authentication, authorization, BCrypt, SecurityContext |
| `spring-boot-starter-mail` | org.springframework.boot | 3.5.14 | EmailService | JavaMailSender, SMTP email sending |
| `spring-boot-starter-validation` | org.springframework.boot | 3.5.14 | RegisterRequest, LoginRequest, VerifyCodeRequest | @Valid, @NotBlank, @Email, @Size, @Pattern |
| `spring-boot-starter-web` | org.springframework.boot | 3.5.14 | AuthController, GlobalExceptionHandler | REST controllers, Tomcat, Jackson serialization |
| `commons-codec` | commons-codec | (managed) | AuthService | DigestUtils.sha256Hex for refresh token hashing |
| `postgresql` (runtime) | org.postgresql | (managed) | application.yaml (driver) | PostgreSQL JDBC driver |
| `lombok` (optional) | org.projectlombok | (managed) | User, all DTOs, AuthService, etc. | @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor, @RequiredArgsConstructor |
| `spring-boot-devtools` (runtime) | org.springframework.boot | 3.5.14 | — | Hot reload, live restart in development |
| `spring-boot-starter-test` (test) | org.springframework.boot | 3.5.14 | CollaborationPlatformApplicationTests | JUnit 5, Mockito, Spring test utilities |
| `spring-security-test` (test) | org.springframework.security | (managed) | — | Security test annotations |
| `jjwt-api` | io.jsonwebtoken | 0.12.3 | JwtService | JWT creation API (Jwts.builder, Jwts.parser) |
| `jjwt-impl` (runtime) | io.jsonwebtoken | 0.12.3 | (runtime) | JWT implementation |
| `jjwt-jackson` (runtime) | io.jsonwebtoken | 0.12.3 | (runtime) | JSON serialization for JWT claims |

---

## 7. Infrastructure — `docker-compose.yml`

```yaml
services:
  db:
    image: postgres:16-alpine
    container_name: cp-postgres
    environment:
      POSTGRES_DB: CP
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - cp_postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: cp-redis
    ports:
      - "6379:6379"

  server:
    build:
      context: ./server
      dockerfile: Dockerfile
    container_name: cp-server
    env_file:
      - ./.env
    ports:
      - "8080:8080"
    depends_on:
      - db
      - redis

volumes:
  cp_postgres_data:
```

**3 services:**
- **db** (PostgreSQL 16 Alpine) — persistent data volume, port 5432
- **redis** (Redis 7 Alpine) — in-memory cache, port 6379
- **server** (Spring Boot, built from `./server/Dockerfile`) — loads env from `./.env`, port 8080, depends on db + redis

**Notable:** There is NO `client` service. The Angular frontend is not dockerized and must be run separately with `ng serve`.

---

## 8. Data Flow Diagram

```
┌─────────────┐         HTTP (CORS)         ┌──────────────────┐
│   Browser   │ ◄─────────────────────────► │  Angular 21 FE   │
│ (Angular)   │   localhost:4200            │  localhost:4200  │
└─────────────┘                             └────────┬─────────┘
                                                     │
                                              HTTP (withCredentials)
                                                     │
                                                     ▼
┌─────────────┐                              ┌──────────────────┐
│  PostgreSQL │ ◄───── JDBC ─────►           │  Spring Boot 3.5 │
│  :5432      │                              │  :8080           │
│  DB: CP     │                              │                  │
│  Table:     │                              │  AuthController  │
│   users     │                              │  AuthService     │
│             │                              │  JwtAuthFilter   │
└─────────────┘                              │  JwtService      │
                                             │  EmailService    │
┌─────────────┐                              │  CookieService   │
│  Redis 7    │ ◄───── Lettuce ────►         │  GlobalException │
│  :6379      │                              └────────┬─────────┘
│  Keys:      │                                       │
│  refresh:*  │                                SMTP (Gmail)
│  blacklist:*│                                       │
└─────────────┘                                       ▼
                                               ┌──────────────┐
                                               │  Gmail SMTP  │
                                               │  (Email      │
                                               │   verification│
                                               │   codes)     │
                                               └──────────────┘
```

---

## 9. Authentication Flow (Full Step-by-Step)

### Registration
```
Frontend (RegisterComponent)
  → POST /api/auth/register { username, email, password }
  → Backend validates uniqueness, BCrypts password
  → Generates 6-digit code
  → Saves User (enabled=false, code, expiry=now+10min)
  → Sends email via Gmail SMTP
  → Returns 201 { message: "Registration successful..." }
```

### Email Verification
```
Frontend (VerifyEmailComponent)
  → POST /api/auth/verify-code { email, code }
  → Backend finds user, validates code match & expiry
  → Sets enabled=true, clears code fields
  → Returns 200 { message: "Email verified successfully..." }
```

### Login
```
Frontend (LoginComponent)
  → POST /api/auth/login { email, password }
  → Backend validates credentials, checks enabled
  → Generates access_token (15min JWT) + refresh_token (7day JWT)
  → Stores refresh token SHA-256 hash in Redis "refresh:<userId>"
  → Sets HttpOnly cookies:
      access_token (path=/, secure=false, SameSite=Lax)
      refresh_token (path=/api/auth/refresh, secure=true, SameSite=Lax)
  → Returns AuthResponse { id, email, role, username }
  → Frontend stores in BehaviorSubject (profile$)
```

### Authenticated Request
```
Frontend (any component)
  → GET /api/auth/me (cookie sent automatically via withCredentials)
  → Backend JwtAuthFilter reads access_token cookie
  → Validates JWT signature + expiry
  → Checks Redis "blacklist:<jti>" (returns 401 if blacklisted)
  → Loads User from DB, sets SecurityContext
  → Controller returns AuthResponse
```

### Token Refresh
```
Frontend (AuthService.checkSession)
  → POST /api/auth/refresh (refresh_token cookie sent)
  → Backend validates refresh_token JWT
  → Looks up Redis "refresh:<userId>", compares SHA-256 hash
  → Generates NEW access_token + NEW refresh_token (rotation)
  → Overwrites Redis "refresh:<userId>" with new hash
  → Sets both new cookies
  → Returns 200 { message: "Token refreshed successfully" }
```

### Logout
```
Frontend (NavbarComponent)
  → POST /api/auth/logout (access_token cookie sent)
  → Backend extracts JTI from access_token
  → Stores "blacklist:<jti>" in Redis with remaining TTL
  → Deletes "refresh:<userId>" from Redis
  → Clears both cookies (maxAge=0)
  → Returns 200 { message: "Logged out successfully" }
  → Frontend resets BehaviorSubject (profile$=null)
```

---

## 10. Database Schema

### Current (only table: `users`)

| Column | Type | Constraints | Default | Notes |
|--------|------|-------------|---------|-------|
| `id` | VARCHAR(255) | PK, NOT NULL | UUID (generated) | JPA UUID strategy |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | — | User email |
| `username` | VARCHAR(255) | UNIQUE, NOT NULL | — | Display name |
| `password` | VARCHAR(255) | NOT NULL | — | BCrypt hash |
| `enabled` | BOOLEAN | NOT NULL | FALSE | Email verified? |
| `verification_code` | VARCHAR(255) | nullable | — | 6-digit OTP |
| `verification_code_expiry` | TIMESTAMP | nullable | — | OTP expiration |
| `role` | VARCHAR(255) | NOT NULL | 'USER' | USER or ADMIN |
| `created_at` | TIMESTAMP | NOT NULL | now() | Auto-set |

---

## 11. Redis Key Structure

| Key Pattern | Type | TTL | Purpose |
|-------------|------|-----|---------|
| `refresh:<userId>` | String (SHA-256 hash) | Refresh token expiration (7 days) | Store validated refresh tokens |
| `blacklist:<jti>` | String ("true") | Remaining access token TTL | Blacklist revoked access tokens |

---

## 12. CORS Configuration

- **Allowed origins:** `http://localhost:4200, http://localhost:3000` (configurable)
- **Allowed methods:** GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Allowed headers:** Authorization, Content-Type, Accept
- **Credentials:** true (cookies enabled)
- **Applied to:** all paths (`/**`)

---

## 13. Test Coverage

### Frontend
- `client/src/app/app.spec.ts` — Single test: "should create the app"

### Backend
- `server/src/test/.../CollaborationPlatformApplicationTests.java` — Single test: `contextLoads()`

**No other tests exist in either module.**

---

## 14. Architectural Gaps & Improvements

### Critical (Blocking MVP Completion)

1. **Core domain still incomplete** — Implemented: `User`, `Project`, `ProjectMember`, `ProjectRole`, `ProjectInvitation`. Still missing: `Task`, `TaskStatus`, `Priority`, `Team`, `Notification`, and later collaboration/activity entities.

2. **Project CRUD exists; remaining project work is refinement** — `ProjectController`, `ProjectService`, and repositories now support creation, listing, reading, updating, archiving, membership listing, role changes, removal, and invitation-based membership.

3. **No Task CRUD** — No `TaskController`, `TaskService`, `TaskRepository`. Phase 2 requires full task management with priority and due dates.

4. **No Kanban backend** — No status management, drag-and-drop persistence, or board endpoints. Phase 2 requires Kanban view.

5. **No Dashboard endpoints** — No aggregated queries for personal or project dashboards. Phase 3 requires both.

6. **No Notification system** — No `Notification` entity, no in-app notifications, no WebSocket/STOMP for real-time delivery. Phase 3 requires in-app + email notifications.

7. **Team management not implemented yet** — Project roles and invitation flow exist. Dedicated teams/team membership are still not implemented.

8. **Password reset** — Frontend has a UI-only form. Missing backend endpoint (`POST /api/auth/forgot-password`, `POST /api/auth/reset-password`), email template, token generation.

### Architectural / Technical Debt

9. **WebSocket/STOMP not implemented** — The spec requires real-time sync. Need `WebSocketConfig` on backend with STOMP/SockJS and `@MessageMapping` handlers. Frontend needs STOMP client integration.

10. **Redis Pub/Sub not configured** — Required for scaling WebSocket messages across multiple server instances.

11. **No route guards (frontend)** — No `AuthGuard`, no `CanActivate`. Any user can access any route. Dashboard and project routes would need protection.

12. **No method-level security (backend)** — `@PreAuthorize` not used anywhere. The 4 project roles (Owner, Team Lead, Member, Observer) need fine-grained authorization.

13. **Hardcoded API URL** — `AUTH_API_BASE_URL` is hardcoded in `api.constants.ts`. The `.env` file is not consumed by Angular CLI. Should use Angular environments or a build-time replacement.

14. **Angular client not in Docker Compose** — Only the server is dockerized. The client needs either a service in `docker-compose.yml` or static serving from the backend.

15. **JPA `ddl-auto=create`** — Drops tables on every restart. Should use `update` or `validate` for anything beyond initial development.

16. **Refresh cookie `secure=true`** — Prevents refresh from working over HTTP (local dev). Needs conditional config based on profile.

17. **No automatic token refresh on frontend** — `AuthService.checkSession()` exists but is never called. No HTTP interceptor retries with refresh on 401.

18. **Social login (Google/GitHub)** — Buttons are rendered but have no OAuth flow implementation.

19. **No pagination** — Spec requires paginated endpoints. No `Pageable` usage anywhere.

20. **No rate limiting** — Spec requires rate limiting. Not implemented (could use Spring filter + Redis or Bucket4j).

21. **No file storage** — Spec says "Local (MVP)" for file storage. Not implemented.

22. **No search/filter functionality** — Phase 5 requires global search and Kanban filters. Backend needs search endpoints, frontend needs search UI.

23. **No PostgreSQL indexing** — Spec requires indexing on key columns. No `@Index` annotations or custom index definitions.

24. **Single test file per module** — Critical services (AuthService, JwtService, EmailService) have no unit tests. No integration tests for endpoints.

25. **Frontend has no lazy module for dashboard/projects/tasks** — These will need their own feature modules with routing as they are built.
