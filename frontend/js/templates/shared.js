import { icons, escapeHtml } from '../icons.js'
import { PLACES } from '../places.js'
import { formatDeparture, rideStatusLabel } from '../format.js'

export function screenHeader(title, backAction, tight, backArg) {
  return `
    <div style="display:flex;align-items:center;gap:10px;padding:${tight ? '18px 20px 6px' : '18px 20px 10px'}">
      <button class="btn btn-icon btn-secondary" data-action="${backAction}" ${backArg !== undefined ? `data-arg="${escapeHtml(backArg)}"` : ''} aria-label="뒤로">${icons.back()}</button>
      <h4 style="margin:0;font-size:19px">${escapeHtml(title)}</h4>
    </div>`
}

const TABS = [
  { keys: ['home', 'matches', 'ride'], label: '홈', action: 'goHome', icon: 'tabHome' },
  { keys: ['map'], label: '지도', action: 'goMap', icon: 'tabMap' },
  { keys: ['compose', 'edit'], label: '모집', action: 'goCompose', icon: 'tabCompose' },
  { keys: ['chats', 'chat'], label: '채팅', action: 'goChats', icon: 'tabChat' },
  { keys: ['profile'], label: '나', action: 'goProfile', icon: 'tabProfile' },
]

export function bottomTabs(screen) {
  return `
    <div class="tabbar">
      <div class="tabbar-brand">혼디 가게</div>
      ${TABS.map(
        (t) => `
        <button class="tabbar-btn${t.keys.includes(screen) ? ' active' : ''}" data-action="${t.action}">
          ${icons[t.icon]()}
          ${t.label}
        </button>`,
      ).join('')}
    </div>`
}

export function toast(message) {
  if (!message) return ''
  return `<div class="toast">${escapeHtml(message)}</div>`
}

export function confirmSheet({ title, desc, confirmLabel }) {
  return `
    <div class="sheet-backdrop">
      <div class="sheet">
        <div class="sheet-grip"></div>
        <div>
          <h4 style="margin:0 0 3px;font-size:19px">${escapeHtml(title)}</h4>
          <div class="text-muted" style="font-size:12.5px;line-height:1.5">${escapeHtml(desc)}</div>
        </div>
        <div style="display:flex;gap:8px">
          <button class="btn btn-secondary" data-action="closeSheet" style="flex:1">닫기</button>
          <button class="btn btn-primary" data-action="confirmSheet" style="flex:1">${escapeHtml(confirmLabel)}</button>
        </div>
      </div>
    </div>`
}

const AVATAR_COLORS = ['var(--color-accent)', 'var(--color-accent-2)', 'var(--color-neutral-500)']

export function avatar(user, size = 38) {
  const style = `width:${size}px;height:${size}px;font-size:${Math.round(size * 0.4)}px;background:${AVATAR_COLORS[user.id % AVATAR_COLORS.length]}`
  if (user.profileImage) {
    return `<div class="avatar" style="${style}"><img src="${escapeHtml(user.profileImage)}" alt="" referrerpolicy="no-referrer"></div>`
  }
  return `<div class="avatar" style="${style}">${escapeHtml([...user.nickname][0] ?? '?')}</div>`
}

export function loadingBlock(text = '불러오는 중…') {
  return `<div class="text-muted" style="padding:28px 0;text-align:center;font-size:13px">${escapeHtml(text)}</div>`
}

export function emptyBlock(text, button = '') {
  return `
    <div class="card" style="gap:12px;padding:24px 18px;align-items:center;text-align:center">
      <div class="text-muted" style="font-size:13px;line-height:1.5">${escapeHtml(text)}</div>
      ${button}
    </div>`
}

// 출발지·목적지 선택. allowCurrent면 "현재 위치" 항목을 맨 위에 둔다
export function placeSelect(action, selected, allowCurrent) {
  const index = PLACES.findIndex((p) => p.name === selected?.name && p.lat === selected.lat && p.lon === selected.lon)
  const isCurrent = Boolean(selected) && index === -1
  return `
    <select class="input" data-action="${action}">
      ${allowCurrent ? `<option value="current" ${isCurrent ? 'selected' : ''}>${isCurrent ? escapeHtml(selected.name) : '현재 위치 사용하기'}</option>` : ''}
      ${PLACES.map((p, i) => `<option value="${i}" ${i === index ? 'selected' : ''}>${escapeHtml(p.name)}</option>`).join('')}
    </select>`
}

export function routeLine(originName, destName, bold = true) {
  const weight = bold ? 'font-weight:600' : ''
  return `
    <div style="display:flex;flex-direction:column">
      <div style="display:flex;align-items:center;gap:10px">
        <div style="flex:none;width:9px;height:9px;border-radius:50%;border:2.75px solid var(--color-accent)"></div>
        <div style="flex:1;font-size:14px;${weight}">${escapeHtml(originName)}</div>
      </div>
      <div style="margin:3px 0 3px 3px;width:2px;height:14px;background-image:radial-gradient(circle, var(--color-neutral-400) 1px, transparent 1.2px);background-size:2px 5px"></div>
      <div style="display:flex;align-items:center;gap:10px">
        ${icons.pin()}
        <div style="flex:1;font-size:14px;${weight}">${escapeHtml(destName)}</div>
      </div>
    </div>`
}

export function myStatusTag(myStatus) {
  if (myStatus === 'HOST') return '<span class="tag tag-accent-2">내 모집글</span>'
  if (myStatus === 'JOINED') return '<span class="tag tag-accent-2">참여 중</span>'
  return ''
}

// 모집글 목록 카드 (홈, 지도, 내 동승 공용)
export function rideCard(ride, extra = '') {
  return `
    <div class="card elev-sm" style="gap:9px;padding:16px;cursor:pointer" data-action="goRide" data-arg="${ride.id}">
      <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap">
        <span class="tag tag-accent">${escapeHtml(formatDeparture(ride.departureAt))}</span>
        <span class="tag tag-neutral">${escapeHtml(rideStatusLabel(ride))}</span>
        ${myStatusTag(ride.myStatus)}
        <span style="margin-left:auto;font-size:12px;font-weight:600;color:var(--color-accent-2-700)">${ride.currentCount}/${ride.capacity}명</span>
      </div>
      <div style="font-size:15px;font-weight:600;line-height:1.35">${escapeHtml(ride.originName)} → ${escapeHtml(ride.destName)}</div>
      ${extra}
      <div class="card-meta">${avatar(ride.host, 18)}${escapeHtml(ride.host.nickname)}</div>
    </div>`
}
