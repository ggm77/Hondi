import { icons, escapeHtml } from '../icons.js'
import { formatDeparture, formatMessageTime } from '../format.js'
import { loadingBlock } from './shared.js'

const quickReplies = ['어디서 만날까요?', '몇 시에 도착하세요?', '요금은 인원수대로 나눠요']

export function chatScreen(state) {
  const { chat } = state
  const ride = chat.ride
  return `
    <div style="display:flex;flex-direction:column;min-height:100%;animation:hondiFade .22s ease">
      <div style="display:flex;align-items:center;gap:10px;padding:16px 18px;border-bottom:1px solid var(--color-divider);background:var(--color-bg);position:sticky;top:0;z-index:3">
        <button class="btn btn-icon btn-secondary" data-action="goChats" aria-label="뒤로">${icons.back()}</button>
        <div style="flex:1;min-width:0">
          <div style="font-size:14px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${ride ? `${escapeHtml(ride.originName)} → ${escapeHtml(ride.destName)}` : '채팅'}</div>
          <div class="text-muted" style="font-size:11px">${ride ? `${escapeHtml(formatDeparture(ride.departureAt))} · ${ride.currentCount}/${ride.capacity}명` : ''}</div>
        </div>
        <button class="btn btn-icon btn-secondary" data-action="goRide" data-arg="${chat.rideId}" aria-label="모집글 보기">${icons.kebab()}</button>
      </div>

      <div id="chatMessages" style="flex:1;padding:16px 18px 12px;display:flex;flex-direction:column;gap:10px">
        ${chatMessages(state)}
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
          <input id="draftInput" class="input" data-bind="draft" data-enter-action="send" maxlength="1000" value="${escapeHtml(state.draft)}" placeholder="메시지 보내기" style="flex:1;min-height:42px">
          <button class="btn btn-primary btn-icon" data-action="send" style="width:42px;height:42px;flex:none" aria-label="보내기">${icons.send()}</button>
        </div>
        <div style="display:flex;gap:8px;margin-top:9px">
          <button class="btn btn-secondary" data-action="callTaxi" style="flex:1;font-size:13px">카카오 T로 택시 호출</button>
        </div>
      </div>
    </div>`
}

// 폴링으로 새 메시지가 오면 이 부분만 다시 그린다 (입력창 포커스·한글 조합 유지)
export function chatMessages({ chat, me }) {
  if (!chat.loaded) return loadingBlock()
  const ride = chat.ride

  return `
    ${
      chat.hasOlder
        ? `<button class="btn btn-ghost" data-action="loadOlderMessages" style="align-self:center;font-size:12px" ${chat.loadingOlder ? 'disabled' : ''}>${chat.loadingOlder ? '불러오는 중…' : '이전 메시지 보기'}</button>`
        : `<div style="padding:14px 16px;border-radius:24px;background:var(--color-accent-2-100);font-size:12px;line-height:1.5;color:var(--color-accent-2-800)">
            <b>${escapeHtml(ride.originName)} → ${escapeHtml(ride.destName)} · ${escapeHtml(formatDeparture(ride.departureAt))}</b><br>
            ${ride.memo ? `${escapeHtml(ride.memo)}<br>` : ''}택시 호출과 요금 나누기는 만나서 직접 진행해 주세요.
          </div>`
    }
    ${chat.messages.map((m, i) => bubble(m, chat.messages[i - 1], me)).join('')}`
}

function bubble(m, prev, me) {
  const mine = me && m.sender.id === me.id
  const showName = !mine && (!prev || prev.sender.id !== m.sender.id)
  return `
    <div style="display:flex;flex-direction:column;align-items:${mine ? 'flex-end' : 'flex-start'};gap:3px">
      ${showName ? `<div class="text-muted" style="font-size:11px;padding-left:4px">${escapeHtml(m.sender.nickname)}</div>` : ''}
      <div style="max-width:78%;padding:10px 14px;border-radius:${mine ? '20px 20px 6px 20px' : '20px 20px 20px 6px'};background:${mine ? 'var(--color-accent)' : 'var(--color-surface)'};color:${mine ? 'var(--color-bg)' : 'var(--color-text)'};font-size:13.5px;line-height:1.5;white-space:pre-wrap;overflow-wrap:anywhere">${escapeHtml(m.content)}</div>
      <div class="text-muted" style="font-size:10px">${escapeHtml(formatMessageTime(m.createdAt))}</div>
    </div>`
}
