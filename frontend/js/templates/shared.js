import { icons, escapeHtml } from '../icons.js'
import { reportReasonLabels } from '../data.js'

export function screenHeader(title, backAction, tight) {
  return `
    <div style="display:flex;align-items:center;gap:10px;padding:${tight ? '18px 20px 6px' : '18px 20px 10px'}">
      <button class="btn btn-icon btn-secondary" data-action="${backAction}" aria-label="뒤로">${icons.back()}</button>
      <h4 style="margin:0;font-size:19px">${escapeHtml(title)}</h4>
    </div>`
}

const TABS = [
  { key: 'home', label: '홈', action: 'goHome', icon: 'tabHome' },
  { key: 'map', label: '지도', action: 'goMap', icon: 'tabMap' },
  { key: 'compose', label: '모집', action: 'goCompose', icon: 'tabCompose' },
  { key: 'chat', label: '채팅', action: 'goChat', icon: 'tabChat' },
  { key: 'profile', label: '나', action: 'goProfile', icon: 'tabProfile' },
]

export function bottomTabs(screen) {
  return `
    <div class="tabbar">
      <div class="tabbar-brand">혼디 가게</div>
      ${TABS.map(
        (t) => `
        <button class="tabbar-btn${screen === t.key ? ' active' : ''}" data-action="${t.action}">
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

export function reportSheet(state) {
  const { reportReason, block, reportTarget } = state
  return `
    <div class="sheet-backdrop">
      <div class="sheet">
        <div class="sheet-grip"></div>
        <div>
          <h4 style="margin:0 0 3px;font-size:19px">${escapeHtml(reportTarget)}님을 신고할까요?</h4>
          <div class="text-muted" style="font-size:12.5px;line-height:1.5">접수되면 해당 모집글은 자동으로 숨겨지고, 운영팀이 24시간 안에 확인해요.</div>
        </div>
        <div style="display:flex;flex-direction:column;gap:3px">
          ${reportReasonLabels
            .map(
              (t, i) => `
            <label class="radio" style="padding:9px 2px;gap:11px">
              <input type="radio" name="hondi-reason" data-action="pickReason" data-arg="${i}" ${reportReason === i ? 'checked' : ''}>
              <span class="dot"></span>
              <span style="font-size:13.5px">${escapeHtml(t)}</span>
            </label>`,
            )
            .join('')}
        </div>
        <label class="radio" style="gap:11px;padding:12px 15px;border-radius:22px;background:var(--color-surface)">
          <input type="checkbox" data-action="toggleBlock" style="position:absolute;opacity:0;width:0;height:0" ${block ? 'checked' : ''}>
          <span class="dot" style="border-radius:6px;background:${block ? 'var(--color-accent)' : 'transparent'};border-color:${block ? 'var(--color-accent)' : 'var(--color-divider)'};box-shadow:${block ? 'inset 0 0 0 3px var(--color-bg)' : 'none'}"></span>
          <span style="font-size:13.5px">이 사용자를 차단하고 다시 추천받지 않기</span>
        </label>
        <div style="display:flex;gap:8px">
          <button class="btn btn-secondary" data-action="closeSheet" style="flex:1">취소</button>
          <button class="btn btn-primary" data-action="submitReport" style="flex:1" ${reportReason === null ? 'disabled' : ''}>신고 접수</button>
        </div>
      </div>
    </div>`
}

export function blockedSheet() {
  return `
    <div class="sheet-backdrop">
      <div class="sheet">
        <div class="sheet-grip"></div>
        <h4 style="margin:0;font-size:19px">차단한 사용자</h4>
        <div class="card" style="gap:10px;padding:15px;flex-direction:row;align-items:center">
          <div class="avatar" style="width:38px;height:38px;background:var(--color-neutral-400)">ㅈ</div>
          <div style="flex:1">
            <div style="font-size:14px;font-weight:600">지우</div>
            <div class="text-muted" style="font-size:11.5px">약속 시간 미준수 · 3월 신고</div>
          </div>
          <button class="btn btn-secondary" data-action="unblock" style="font-size:12px">차단 해제</button>
        </div>
        <button class="btn btn-secondary btn-block" data-action="closeSheet">닫기</button>
      </div>
    </div>`
}
