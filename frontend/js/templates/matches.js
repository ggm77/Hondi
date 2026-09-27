import { escapeHtml } from '../icons.js'
import { fromInputs, formatDeparture } from '../format.js'
import { screenHeader, avatar, loadingBlock, emptyBlock } from './shared.js'

export function matchesScreen(state) {
  const { search, matches } = state
  const departure = fromInputs(search.date, search.time)
  const routeLabel = `${search.origin.name} → ${search.dest.name}${departure ? ` · ${formatDeparture(departure)}` : ''}`

  return `
    <div style="animation:hondiFade .22s ease">
      ${screenHeader('추천 동승', 'goHome', true)}
      <div style="padding:6px 20px 32px;display:flex;flex-direction:column;gap:14px">
        <div style="font-size:13px;color:var(--color-neutral-700);line-height:1.5">
          <b style="color:var(--color-text)">${escapeHtml(routeLabel)}</b><br>${summary(matches)}
        </div>
        ${matchList(state)}
      </div>
    </div>`
}

function summary(matches) {
  if (!matches.items) return '동선과 시간대가 겹치는 모집글을 찾고 있어요.'
  if (!matches.items.length) return '출발지 3km · 목적지 5km · 전후 1시간 안에 맞는 모집글이 없어요.'
  return `출발지 3km · 목적지 5km · 전후 1시간 안의 모집글 ${matches.items.length}건${matches.hasNext ? '+' : ''}을 찾았어요.`
}

function matchList({ matches, busy }) {
  if (!matches.items) return loadingBlock()
  if (!matches.items.length) {
    return emptyBlock(
      '이 동선으로 직접 모집글을 올리면 비슷한 동선의 여행객에게 추천돼요.',
      '<button class="btn btn-primary" data-action="composeFromSearch">이 동선으로 모집글 올리기</button>',
    )
  }
  return `
    <div class="card-grid" style="gap:14px">
      ${matches.items.map((r) => matchCard(r, busy)).join('')}
    </div>
    <button class="btn btn-ghost" data-action="composeFromSearch" style="align-self:center;font-size:13px">맞는 글이 없나요? 직접 모집글 올리기</button>`
}

function matchCard(ride, busy) {
  const left = ride.capacity - ride.currentCount
  return `
    <div class="card elev-sm" style="gap:12px;padding:17px">
      <div style="display:flex;align-items:center;gap:11px">
        ${avatar(ride.host, 42)}
        <div style="flex:1;min-width:0">
          <div style="font-size:15px;font-weight:600">${escapeHtml(ride.host.nickname)}</div>
          <div class="text-muted" style="font-size:12px">${escapeHtml(ride.originName)} → ${escapeHtml(ride.destName)}</div>
        </div>
        <div style="text-align:right;flex:none">
          <div style="font-family:var(--font-heading);font-size:17px;color:var(--color-accent-2-700)">${left}자리</div>
          <div class="text-muted" style="font-size:10px">${ride.currentCount}/${ride.capacity}명</div>
        </div>
      </div>
      <div style="display:flex;gap:6px;flex-wrap:wrap">
        <span class="tag tag-accent">${escapeHtml(formatDeparture(ride.departureAt))}</span>
        <span class="tag tag-neutral">출발 ${ride.timeDiffMinutes}분 차이</span>
        <span class="tag tag-accent-2">출발지 ${ride.originDistanceKm}km</span>
        <span class="tag tag-accent-2">목적지 ${ride.destDistanceKm}km</span>
      </div>
      <div style="display:flex;gap:8px">
        <button class="btn btn-primary" data-action="joinRide" data-arg="${ride.id}" style="flex:1" ${busy ? 'disabled' : ''}>합류하기</button>
        <button class="btn btn-secondary" data-action="goRide" data-arg="${ride.id}">자세히</button>
      </div>
    </div>`
}
