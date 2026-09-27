import { icons, escapeHtml } from '../icons.js'
import { formatDeparture, rideStatusLabel, isRecruiting, isDeparted } from '../format.js'
import { screenHeader, avatar, routeLine, myStatusTag, loadingBlock } from './shared.js'

export function rideScreen(state) {
  const { ride, busy } = state
  const header = screenHeader('모집글', 'goBackFromRide')
  if (!ride) return header + loadingBlock()

  return `
    <div style="animation:hondiFade .22s ease">
      ${header}
      <div style="padding:8px 20px 32px;display:flex;flex-direction:column;gap:16px">
        <div class="card elev-sm" style="gap:14px;padding:18px">
          <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap">
            <span class="tag tag-accent">${escapeHtml(formatDeparture(ride.departureAt))}</span>
            <span class="tag tag-neutral">${escapeHtml(rideStatusLabel(ride))}</span>
            ${myStatusTag(ride.myStatus)}
          </div>
          ${routeLine(ride.originName, ride.destName)}
          ${
            ride.memo
              ? `<div style="font-size:13px;line-height:1.5;color:var(--color-neutral-800);padding:11px 14px;border-radius:20px;background:var(--color-neutral-100);white-space:pre-wrap">${escapeHtml(ride.memo)}</div>`
              : ''
          }
        </div>

        <div>
          <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:9px">
            <h5 style="margin:0;font-size:15px">함께 타는 사람</h5>
            <span class="text-muted" style="font-size:12px">${ride.currentCount}/${ride.capacity}명</span>
          </div>
          <div class="card" style="gap:0;padding:4px 16px">
            ${memberRow(ride.host, '방장')}
            ${ride.members.map((m) => `<div style="height:1px;background:var(--color-divider)"></div>${memberRow(m)}`).join('')}
          </div>
        </div>

        ${actions(ride, busy)}

        <div style="display:flex;gap:11px;align-items:flex-start;padding:15px 17px;border-radius:26px;background:var(--color-accent-100)">
          ${icons.info()}
          <div style="font-size:12.5px;line-height:1.5;color:var(--color-accent-800)">합류하면 채팅방에서 만날 곳을 정할 수 있어요. 택시 호출과 요금 나누기는 만나서 직접 진행해 주세요.</div>
        </div>
      </div>
    </div>`
}

function memberRow(user, badge) {
  return `
    <div style="display:flex;align-items:center;gap:10px;padding:11px 0">
      ${avatar(user, 32)}
      <div style="flex:1;font-size:13.5px;font-weight:600">${escapeHtml(user.nickname)}</div>
      ${badge ? `<span class="tag tag-accent">${badge}</span>` : ''}
    </div>`
}

function actions(ride, busy) {
  const disabled = busy ? 'disabled' : ''
  if (ride.myStatus === 'HOST') {
    return `
      <div style="display:flex;flex-direction:column;gap:8px">
        <button class="btn btn-primary" data-action="goChat" data-arg="${ride.id}">채팅방 열기</button>
        <div style="display:flex;gap:8px">
          <button class="btn btn-secondary" data-action="goEdit" data-arg="${ride.id}" style="flex:1">수정하기</button>
          <button class="btn btn-secondary" data-action="askDeleteRide" style="flex:1" ${disabled}>삭제하기</button>
        </div>
      </div>`
  }
  if (ride.myStatus === 'JOINED') {
    return `
      <div style="display:flex;gap:8px">
        <button class="btn btn-primary" data-action="goChat" data-arg="${ride.id}" style="flex:1">채팅방 열기</button>
        <button class="btn btn-secondary" data-action="askLeaveRide" ${disabled}>참여 취소</button>
      </div>`
  }
  if (!isRecruiting(ride)) {
    return `<button class="btn btn-primary" disabled>${isDeparted(ride) ? '이미 출발한 동승이에요' : '모집이 마감됐어요'}</button>`
  }
  return `<button class="btn btn-primary" data-action="joinRide" data-arg="${ride.id}" style="font-size:15px;padding:13px" ${disabled}>이 동승에 합류하기</button>`
}
