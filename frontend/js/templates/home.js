import { icons, escapeHtml } from '../icons.js'
import { toDateInput } from '../format.js'
import { placeSelect, rideCard, loadingBlock, emptyBlock } from './shared.js'

export function homeScreen(state) {
  const { me, search, rides } = state
  return `
    <div class="home-layout" style="padding:22px 20px 30px;animation:hondiFade .22s ease">
      <div class="home-head" style="display:flex;align-items:flex-start;gap:12px">
        <div style="flex:1">
          <div style="font-size:12px;color:var(--color-neutral-700)">안녕하세요${me ? `, ${escapeHtml(me.nickname)}님` : ''}</div>
          <h3 style="margin:4px 0 0;font-size:26px">오늘은 어디 갑서?</h3>
        </div>
        <button class="btn btn-icon btn-secondary" data-action="goProfile" style="flex:none" aria-label="프로필">${icons.user()}</button>
      </div>

      <div class="card elev-sm home-route" style="gap:12px;padding:18px">
        <div class="field">
          <label>출발지</label>
          ${placeSelect('setSearchOrigin', search.origin, true)}
        </div>
        <div class="field">
          <label>목적지</label>
          ${placeSelect('setSearchDest', search.dest, false)}
        </div>
        <div class="field">
          <label>희망 출발 시각</label>
          <div style="display:flex;gap:8px">
            <input class="input" type="date" data-bind="search.date" value="${escapeHtml(search.date)}" min="${toDateInput(new Date())}" style="flex:1">
            <input class="input" type="time" data-bind="search.time" value="${escapeHtml(search.time)}" style="width:140px;flex:none">
          </div>
        </div>
        <button class="btn btn-primary btn-block" data-action="goMatches" style="margin-top:6px">이 동선으로 같이 탈 사람 찾기</button>
      </div>

      <div class="home-divider" style="height:10px;background-image:radial-gradient(circle, var(--color-neutral-400) 2px, transparent 2.4px);background-size:15px 10px;opacity:.65"></div>

      <div class="home-list">
        <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:12px">
          <h4 style="margin:0;font-size:19px">혼디 모집 중</h4>
          ${rides.items ? `<span class="text-muted" style="font-size:12px">${rides.items.length}건${rides.hasNext ? '+' : ''}</span>` : ''}
          <a href="#map" data-action="goMap" style="margin-left:auto;font-size:12px;font-weight:600">지도로 보기</a>
        </div>
        ${ridesList(rides)}
      </div>

      <div class="home-notice" style="display:flex;gap:11px;align-items:flex-start;padding:15px 17px;border-radius:26px;background:var(--color-accent-2-100)">
        ${icons.shield()}
        <div style="font-size:12.5px;line-height:1.5;color:var(--color-accent-2-800)">혼디 가게는 택시를 배차하거나 요금을 정산하지 않아요. 같이 탈 사람만 연결해 드리고, <b>호출과 요금 나누기</b>는 만나서 직접 하시면 돼요.</div>
      </div>
    </div>`
}

function ridesList(rides) {
  if (!rides.items) return loadingBlock()
  if (!rides.items.length) {
    return emptyBlock(
      '지금 모집 중인 동승이 없어요. 먼저 모집글을 올려 보세요.',
      '<button class="btn btn-primary" data-action="goCompose">모집글 올리기</button>',
    )
  }
  return `
    <div class="card-grid">${rides.items.map((r) => rideCard(r)).join('')}</div>
    ${
      rides.hasNext
        ? `<button class="btn btn-secondary btn-block" data-action="loadMoreRides" style="margin-top:12px" ${rides.loadingMore ? 'disabled' : ''}>${rides.loadingMore ? '불러오는 중…' : '더 보기'}</button>`
        : ''
    }`
}
