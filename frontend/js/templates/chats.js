import { escapeHtml } from '../icons.js'
import { formatDeparture, formatMessageTime } from '../format.js'
import { loadingBlock, emptyBlock } from './shared.js'

export function chatsScreen(state) {
  const { rooms } = state
  return `
    <div style="animation:hondiFade .22s ease">
      <div style="padding:18px 20px 10px">
        <h4 style="margin:0;font-size:19px">채팅</h4>
      </div>
      <div style="padding:6px 20px 32px;display:flex;flex-direction:column;gap:10px">
        ${
          !rooms
            ? loadingBlock()
            : rooms.length
              ? `<div class="card-grid">${rooms.map(roomCard).join('')}</div>`
              : emptyBlock(
                  '참여 중인 채팅방이 없어요. 동승에 합류하거나 모집글을 올리면 채팅방이 생겨요.',
                  '<button class="btn btn-primary" data-action="goHome">모집글 둘러보기</button>',
                )
        }
      </div>
    </div>`
}

function roomCard(room) {
  return `
    <div class="card elev-sm" style="gap:7px;padding:15px 16px;cursor:pointer" data-action="goChat" data-arg="${room.rideId}">
      <div style="display:flex;align-items:center;gap:8px">
        <div style="flex:1;min-width:0;font-size:14.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${escapeHtml(room.originName)} → ${escapeHtml(room.destName)}</div>
        ${room.lastMessageAt ? `<span class="text-muted" style="flex:none;font-size:11px">${escapeHtml(formatMessageTime(room.lastMessageAt))}</span>` : ''}
      </div>
      <div style="display:flex;align-items:center;gap:8px">
        <div class="text-muted" style="flex:1;min-width:0;font-size:12.5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${room.lastMessage ? escapeHtml(room.lastMessage) : '아직 메시지가 없어요'}</div>
        ${room.unreadCount > 0 ? `<span class="badge">${room.unreadCount > 99 ? '99+' : room.unreadCount}</span>` : ''}
      </div>
      <div class="card-meta">${escapeHtml(formatDeparture(room.departureAt))} 출발</div>
    </div>`
}
