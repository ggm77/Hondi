import { quickReplies } from '../data.js'
import { icons, escapeHtml } from '../icons.js'

export function chatScreen(state) {
  return `
    <div style="display:flex;flex-direction:column;min-height:100%;animation:hondiFade .22s ease">
      <div style="display:flex;align-items:center;gap:10px;padding:16px 18px;border-bottom:1px solid var(--color-divider);background:var(--color-bg);position:sticky;top:0;z-index:3">
        <button class="btn btn-icon btn-secondary" data-action="goMatches" aria-label="뒤로">${icons.back()}</button>
        <div class="avatar" style="width:34px;height:34px;background:var(--color-accent);font-size:14px">수</div>
        <div style="flex:1">
          <div style="font-size:14px;font-weight:600">수민</div>
          <div class="text-muted" style="font-size:11px">39.4℃ · 본인인증 · 동승 6회</div>
        </div>
        <button class="btn btn-icon btn-secondary" data-action="openReport" aria-label="더보기">${icons.kebab()}</button>
      </div>

      <div id="chatMessages" style="flex:1;padding:16px 18px 12px;display:flex;flex-direction:column;gap:10px;overflow-y:auto">
        <div style="padding:14px 16px;border-radius:24px;background:var(--color-accent-2-100);font-size:12px;line-height:1.5;color:var(--color-accent-2-800)">
          <b>제주공항 → 애월 한담해변 · 오늘 15:10</b><br>합류가 확정됐어요. 택시 호출과 요금 나누기는 만나서 직접 진행해 주세요.
        </div>
        ${state.msgs
          .map(
            (m) => `
          <div style="display:flex;flex-direction:column;align-items:${m.me ? 'flex-end' : 'flex-start'};gap:3px">
            <div style="max-width:78%;padding:10px 14px;border-radius:${m.me ? '20px 20px 6px 20px' : '20px 20px 20px 6px'};background:${m.me ? 'var(--color-accent)' : 'var(--color-surface)'};color:${m.me ? 'var(--color-bg)' : 'var(--color-text)'};font-size:13.5px;line-height:1.5">${escapeHtml(m.t)}</div>
            <div class="text-muted" style="font-size:10px">${escapeHtml(m.at)}</div>
          </div>`,
          )
          .join('')}
      </div>

      <div style="position:sticky;bottom:0;background:var(--color-bg);padding:10px 18px 16px;border-top:1px solid var(--color-divider)">
        <div style="display:flex;gap:6px;overflow-x:auto;padding-bottom:9px">
          ${quickReplies
            .map(
              (t) => `<button class="btn btn-secondary" data-action="sendQuick" data-arg="${escapeHtml(t)}" style="flex:none;font-size:12px;padding:5px 12px">${escapeHtml(t)}</button>`,
            )
            .join('')}
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <input id="draftInput" class="input" data-bind="draft" data-enter-action="send" value="${escapeHtml(state.draft)}" placeholder="메시지 보내기" style="flex:1;min-height:42px">
          <button class="btn btn-primary btn-icon" data-action="send" style="width:42px;height:42px;flex:none" aria-label="보내기">${icons.send()}</button>
        </div>
        <div style="display:flex;gap:8px;margin-top:9px">
          <button class="btn btn-secondary" data-action="callTaxi" style="flex:1;font-size:13px">카카오 T로 택시 호출</button>
          <button class="btn btn-ghost" data-action="goReview" style="font-size:13px">동승 완료</button>
        </div>
      </div>
    </div>`
}
