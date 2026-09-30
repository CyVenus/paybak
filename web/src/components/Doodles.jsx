// Hand-drawn ink bits used across the site.

export function Squiggle({ className = '' }) {
  return (
    <svg className={`squiggle ${className}`} viewBox="0 0 200 20" preserveAspectRatio="none" aria-hidden="true">
      <path d="M2 14 C 30 4, 50 18, 80 10 S 130 4, 160 12 S 190 8, 198 9" />
    </svg>
  )
}

export function Sparks({ className = '' }) {
  return (
    <svg className={`sparks ${className}`} viewBox="0 0 60 60" aria-hidden="true">
      <path d="M30 4 L30 18" />
      <path d="M48 12 L40 22" />
      <path d="M56 32 L44 32" />
      <path d="M12 12 L20 22" />
    </svg>
  )
}

export function Arrow({ className = '' }) {
  return (
    <svg className={`doodle-arrow ${className}`} viewBox="0 0 120 80" aria-hidden="true">
      <path d="M6 10 C 40 6, 80 20, 96 60" />
      <path d="M84 52 L97 63 L104 46" />
    </svg>
  )
}

export function Check({ className = '' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M5 12.5 L10 17 L19 7" />
    </svg>
  )
}

const HAIR = [
  'M9 16 C 9 6, 31 6, 31 16 C 27 11, 14 11, 9 16 Z',
  'M8 20 C 6 4, 34 4, 32 20 C 32 12, 26 10, 20 10 C 14 10, 8 12, 8 20 Z M8 20 L6 32 L10 30 Z M32 20 L34 32 L30 30 Z',
  'M10 15 C 12 7, 28 7, 30 15 L 30 12 C 26 8, 14 8, 10 12 Z',
  'M8 18 C 8 6, 32 6, 32 18 C 30 13, 24 12, 20 14 C 16 12, 10 13, 8 18 Z',
  'M11 13 Q 20 3 29 13 Q 20 9 11 13 Z',
]

// Tiny sketch-style face. `v` picks a hairstyle.
export function Avatar({ v = 0, size = 32, label }) {
  return (
    <span className="avatar" style={{ width: size, height: size }} title={label}>
      <svg viewBox="0 0 40 40" aria-hidden="true">
        <circle cx="20" cy="21" r="11" fill="var(--paper)" stroke="var(--ink)" strokeWidth="1.6" />
        <path d={HAIR[v % HAIR.length]} fill="var(--ink)" />
        <circle cx="16" cy="21" r="1.2" fill="var(--ink)" />
        <circle cx="24" cy="21" r="1.2" fill="var(--ink)" />
        <path d="M16.5 26 Q 20 28.5 23.5 26" fill="none" stroke="var(--ink)" strokeWidth="1.4" strokeLinecap="round" />
      </svg>
    </span>
  )
}

export function DownloadIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 4v11" />
      <path d="M7 10l5 5 5-5" />
      <path d="M5 20h14" />
    </svg>
  )
}

export function AndroidIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
      <path d="M6 9h12v8a1.5 1.5 0 0 1-1.5 1.5H16V21a1 1 0 0 1-2 0v-2.5h-4V21a1 1 0 0 1-2 0v-2.5h-.5A1.5 1.5 0 0 1 6 17V9Zm-2.5 0a1 1 0 0 1 1 1v5a1 1 0 0 1-2 0v-5a1 1 0 0 1 1-1Zm17 0a1 1 0 0 1 1 1v5a1 1 0 0 1-2 0v-5a1 1 0 0 1 1-1ZM6.2 8a5.9 5.9 0 0 1 3.1-4.6l-1-1.6a.4.4 0 0 1 .7-.4l1 1.6a6.3 6.3 0 0 1 4 0l1-1.6a.4.4 0 0 1 .7.4l-1 1.6A5.9 5.9 0 0 1 17.8 8H6.2Zm3.3-2.4a.7.7 0 1 0 0 1.4.7.7 0 0 0 0-1.4Zm5 0a.7.7 0 1 0 0 1.4.7.7 0 0 0 0-1.4Z" />
    </svg>
  )
}
