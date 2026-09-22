import { matches } from '../data.js'
import { escapeHtml } from '../icons.js'
import { screenHeader } from './shared.js'

export function matchesScreen(state) {
  const routeLabel = `${state.from} → ${state.to} · ${state.day} ${state.time}`
  return `
    <div style="animation:hondiFade .22s ease">
      ${screenHeader('추천 동승 상대', 'goHome', true)}
      <div style="padding:6px 20px 32px;display:flex;flex-direction:column;gap:14px">
        <div style="font-size:13px;color:var(--color-neutral-700);line-height:1.5">
          <b style="color:var(--color-text)">${escapeHtml(routeLabel)}</b><br>동선과 시간대가 겹치는 여행객 3명을 찾았어요.
        </div>
        ${matches
          .map(
            (m) => `
          <div class="card elev-sm" style="gap:12px;padding:17px">
            <div style="display:flex;align-items:center;gap:11px">
              <div class="avatar" style="width:42px;height:42px;background:${m.avatarBg};font-size:16px">${m.initial}</div>
              <div style="flex:1">
                <div style="font-size:15px;font-weight:600">${escapeHtml(m.name)}</div>
                <div class="text-muted" style="font-size:12px">${escapeHtml(m.meta)}</div>
              </div>
              <div style="text-align:right">
                <div style="font-family:var(--font-heading);font-size:17px;color:var(--color-accent-2-700)">${escapeHtml(m.temp)}</div>
                <div class="text-muted" style="font-size:10px">매너온도</div>
              </div>
            </div>
            <div style="display:flex;gap:6px;flex-wrap:wrap">
              <span class="tag tag-accent">${escapeHtml(m.overlap)}</span>
              <span class="tag tag-neutral">${escapeHtml(m.gap)}</span>
              <span class="tag tag-accent-2">본인인증</span>
            </div>
            <div style="font-size:13px;line-height:1.5;color:var(--color-neutral-800);padding:11px 14px;border-radius:20px;background:var(--color-neutral-100)">${escapeHtml(m.note)}</div>
            <div style="display:flex;gap:8px">
              <button class="btn btn-primary" data-action="goChat" style="flex:1">합류 요청 보내기</button>
              <button class="btn btn-secondary" data-action="goMap">경로 보기</button>
            </div>
          </div>`,
          )
          .join('')}
      </div>
    </div>`
}
