---
name: Project Management Design System
colors:
  surface: '#fcf8fa'
  surface-dim: '#dcd9db'
  surface-bright: '#fcf8fa'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f5'
  surface-container: '#f0edef'
  surface-container-high: '#eae7e9'
  surface-container-highest: '#e4e2e4'
  on-surface: '#1b1b1d'
  on-surface-variant: '#45464d'
  inverse-surface: '#303032'
  inverse-on-surface: '#f3f0f2'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#0051d5'
  on-secondary: '#ffffff'
  secondary-container: '#316bf3'
  on-secondary-container: '#fefcff'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#271901'
  on-tertiary-container: '#98805d'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#fcdeb5'
  tertiary-fixed-dim: '#dec29a'
  on-tertiary-fixed: '#271901'
  on-tertiary-fixed-variant: '#574425'
  background: '#fcf8fa'
  on-background: '#1b1b1d'
  surface-variant: '#e4e2e4'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-base:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px
  container-max: 1440px
  gutter: 24px
---

## Brand & Style

This design system is engineered for high-velocity project management and executive oversight. The brand personality is **utilitarian, authoritative, and precise**, stripping away decorative excess to prioritize data density and cognitive clarity. 

The aesthetic follows a **Modern Corporate** movement—a refined blend of Swiss-inspired minimalism and contemporary SaaS functionalism. It utilizes a structured hierarchy to manage complex information environments without overwhelming the user. The emotional response should be one of "controlled efficiency," where the interface feels like a high-performance tool rather than a social platform. 

Key visual principles include:
- **Functional Density:** Maximizing screen real estate for data visibility.
- **Visual Silence:** Using generous white space between logical groups to reduce noise.
- **Calculated Contrast:** reserving high-impact colors exclusively for primary actions and critical status changes.

## Colors

The color palette is anchored by **Deep Slate (#0F172A)** for primary branding and navigation, and **Professional Blue (#2563EB)** for interaction cues and primary CTA buttons. 

- **Surfaces:** We use a "Layered Light" approach. The base application background is a neutral **Light Gray (#F8FAFC)**, while all functional containers and cards use **Crisp White (#FFFFFF)** to create immediate separation through value contrast rather than heavy shadows.
- **Hierarchy:** Deep Slate is used for high-level navigation and primary headers to provide a sense of stability.
- **Accents:** Secondary Blue is utilized for active states, text links, and primary progress indicators.
- **Status Indicators:** A semantic palette is employed for badges (pills). These should use a 10% opacity background of the base color with 100% opacity text for a professional, "washed" look that doesn't compete with primary actions.

## Typography

The system relies exclusively on **Inter**, a typeface designed for user interfaces. The typographic scale is optimized for readability in data-rich environments.

- **Headlines:** Use tighter letter-spacing and heavier weights to anchor pages.
- **Body Text:** Standardized at 14px and 16px to ensure long-form legibility in task descriptions and comments.
- **Labels:** Small, uppercase labels with increased letter-spacing are used for table headers and metadata titles to differentiate them from actionable data.
- **Data Tables:** For numerical data, use `font-variant-numeric: tabular-nums` to ensure columns align perfectly for easy scanning.

## Layout & Spacing

This design system uses a **4px baseline grid** to ensure mathematical harmony across all components.

- **Layout Model:** A **12-column fixed-fluid hybrid grid**. On desktop, content is contained within a 1440px max-width wrapper. 
- **Sidebars:** Navigation is handled via a fixed 240px left-hand rail in Deep Slate, maximizing vertical space for project lists.
- **Responsive Behavior:**
    - **Desktop (>1024px):** 12 columns, 24px gutters, 32px margins.
    - **Tablet (768px - 1023px):** 8 columns, 16px gutters, 24px margins.
    - **Mobile (<767px):** 4 columns, 16px gutters, 16px margins. 
- **Angular Integration:** Utilize Flex Layout or CSS Grid components. Spacing should be applied via utility classes (e.g., `p-md`, `m-lg`) to maintain consistency.

## Elevation & Depth

To maintain a "modern and professional" feel, this design system avoids heavy shadows, favoring **Low-Contrast Outlines** and **Tonal Layers**.

- **Level 0 (Background):** Neutral Light Gray (#F8FAFC).
- **Level 1 (Cards/Surface):** Crisp White (#FFFFFF) with a 1px solid border (#E2E8F0). No shadow.
- **Level 2 (Popovers/Modals):** Crisp White with a subtle, diffused shadow (`0 10px 15px -3px rgba(0, 0, 0, 0.1)`).
- **Active States:** Subtle border color changes (e.g., Blue #2563EB) signal focus rather than "lifting" the element off the page. This keeps the UI flat and efficient.

## Shapes

The shape language is primarily **Soft (Level 1)** to maintain a professional, crisp edge while avoiding the harshness of 90-degree corners.

- **Components:** Buttons, Input fields, and Cards use a standard **4px (0.25rem)** radius.
- **Badges/Chips:** These are the exception, utilizing a **Pill (Full Round)** shape to clearly distinguish status indicators from actionable buttons.
- **Selection States:** Highlighting in lists or navigation uses the same 4px radius to create a cohesive internal "box" language.

## Components

### Buttons
- **Primary:** Professional Blue (#2563EB) background, white text. Flat, high-contrast.
- **Secondary:** Deep Slate (#0F172A) outline, slate text. 1px border.
- **Ghost:** No background or border. Slate text. Used for less frequent actions.

### Cards
- **Flat Card:** White background, 1px border (#E2E8F0), 4px border-radius. No box-shadow. This is the primary container for all project data and dashboard widgets.

### Status Indicators (Pill Badges)
- **Design:** Fully rounded (pill), 12px font size, semi-bold.
- **Coloring:** Use the semantic palette with 10% background opacity. Example: "In Progress" uses a blue tint background with blue text.

### Inputs & Tables
- **Inputs:** 1px border (#CBD5E1), 4px radius. Focus state: 1px Blue border with a soft blue 2px outer glow (ring).
- **Tables:** No outer border. Row borders are 1px light gray (#F1F5F9). Header cells use Light Gray (#F8FAFC) background and uppercase Label typography.

### Angular Specifics
- Implementation should utilize **Angular Material** as a base, with heavy CSS variable overrides to match this design system's specific spacing and color tokens. 
- Components should be modular, with "dumb" presentational components for cards and badges to ensure reusability across different project modules.