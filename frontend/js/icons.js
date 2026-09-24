const strokeBase = 'fill="none" stroke-width="2.75" stroke-linecap="round" stroke-linejoin="round"'

export const icons = {
  back: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="m15 18-6-6 6-6" />
    </svg>`,
  kebab: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="2.75" stroke-linecap="round">
      <circle cx="12" cy="5" r="1" />
      <circle cx="12" cy="12" r="1" />
      <circle cx="12" cy="19" r="1" />
    </svg>`,
  send: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="M12 19V5" />
      <path d="m5 12 7-7 7 7" />
    </svg>`,
  search: (size = 15) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-neutral-700)" ${strokeBase}>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </svg>`,
  pin: (size = 13) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-accent-2)" ${strokeBase} style="margin-left:-1px">
      <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0z" />
    </svg>`,
  shield: (size = 18) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-accent-2-700)" ${strokeBase} style="flex:none;margin-top:1px">
      <path d="M20 13c0 5-3.5 7.5-7.7 8.9a1 1 0 0 1-.6 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.2-2.7a1 1 0 0 1 1.5 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1z" />
      <path d="m9 12 2 2 4-4" />
    </svg>`,
  info: (size = 18) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-accent-700)" ${strokeBase} style="flex:none;margin-top:1px">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8h.01M11 12h1v5h1" />
    </svg>`,
  user: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 8-3 8h18s-3-1-3-8" />
      <path d="M10.3 21a2 2 0 0 0 3.4 0" />
    </svg>`,
  check: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-accent-2-700)" ${strokeBase} style="flex:none">
      <path d="M20 6 9 17l-5-5" />
    </svg>`,
  gender: (size = 17) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-neutral-500)" ${strokeBase} style="flex:none">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v8" />
    </svg>`,
  chevronRight: (size = 15) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="var(--color-neutral-500)" ${strokeBase}>
      <path d="m9 18 6-6-6-6" />
    </svg>`,
  tabHome: (size = 21) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <path d="M9 22V12h6v10" />
    </svg>`,
  tabMap: (size = 21) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0z" />
      <circle cx="12" cy="10" r="3" />
    </svg>`,
  tabCompose: (size = 21) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <circle cx="12" cy="12" r="9" />
      <path d="M8 12h8M12 8v8" />
    </svg>`,
  tabChat: (size = 21) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22z" />
    </svg>`,
  tabProfile: (size = 21) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" stroke="currentColor" ${strokeBase}>
      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>`,
}

export function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}
