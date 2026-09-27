import { icons, escapeHtml } from '../icons.js'

// 지도에서 출발지·목적지 고르기. 지도 iframe은 열 때 한 번만 그리고, 하단 바만 다시 그린다
export function placePicker({ title, pinLabel, point }) {
  const query = new URLSearchParams({ mode: 'pick', label: pinLabel })
  if (point) {
    query.set('lat', point.lat)
    query.set('lon', point.lon)
  }
  return `
    <div class="picker">
      <div style="display:flex;align-items:center;gap:10px;padding:14px 18px;border-bottom:1px solid var(--color-divider)">
        <button class="btn btn-icon btn-secondary" data-action="closePicker" aria-label="닫기">${icons.back()}</button>
        <h4 style="margin:0;font-size:17px">${escapeHtml(title)}</h4>
      </div>
      <div style="flex:1;position:relative;background:var(--color-neutral-200)">
        <iframe id="pickerFrame" src="jeju-map.html?${query}" title="${escapeHtml(title)}" style="position:absolute;inset:0;width:100%;height:100%;border:0"></iframe>
      </div>
      <div id="pickerBar" style="padding:14px 18px 20px;border-top:1px solid var(--color-divider)">${pickerBar(point)}</div>    </div>`
}

export function pickerBar(point, hint = '지도를 눌러 위치를 찍어 주세요.') {
  return `
    <div style="display:flex;align-items:center;gap:12px">
      <div style="flex:1;min-width:0;font-size:13.5px;line-height:1.4">
        ${point ? `<b>${escapeHtml(point.name)}</b>` : `<span class="text-muted">${escapeHtml(hint)}</span>`}
      </div>
      <button class="btn btn-primary" data-action="confirmPicker" style="flex:none" ${point ? '' : 'disabled'}>이 위치로 정하기</button>
    </div>`
}
