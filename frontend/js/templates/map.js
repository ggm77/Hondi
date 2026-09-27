import { rideCard, loadingBlock, emptyBlock } from './shared.js'

export function mapScreen(state) {
  return `
    <div class="map-layout" style="animation:hondiFade .22s ease">
      <div class="map-view" style="position:relative;background:var(--color-neutral-200)">
        <iframe id="mapFrame" src="jeju-map.html" title="제주 동승 모집글 지도" style="position:absolute;inset:0;width:100%;height:100%;border:0"></iframe>
      </div>
      <div id="mapPanel" class="map-panel" style="padding:18px 20px 30px;display:flex;flex-direction:column;gap:12px;position:relative;background:var(--color-bg)">
        ${mapPanel(state)}
      </div>
    </div>`
}

// 목록이 바뀔 때는 이 부분만 다시 그리고 지도 iframe은 그대로 둔다
export function mapPanel({ rides }) {
  return `
    <div class="sheet-grip map-grip"></div>
    <div style="display:flex;align-items:baseline;gap:8px">
      <h4 style="margin:0;font-size:18px">모집 중인 동승</h4>
      ${rides.items ? `<span class="text-muted" style="font-size:12px">${rides.items.length}건${rides.hasNext ? '+' : ''}</span>` : ''}
    </div>
    ${
      !rides.items
        ? loadingBlock()
        : rides.items.length
          ? rides.items.map((r) => rideCard(r)).join('')
          : emptyBlock('지금 모집 중인 동승이 없어요.', '<button class="btn btn-primary" data-action="goCompose">모집글 올리기</button>')
    }
    ${
      rides.hasNext
        ? `<button class="btn btn-secondary btn-block" data-action="loadMoreRides" ${rides.loadingMore ? 'disabled' : ''}>${rides.loadingMore ? '불러오는 중…' : '더 보기'}</button>`
        : ''
    }`
}
