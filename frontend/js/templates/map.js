import { mapPosts } from '../data.js'
import { icons, escapeHtml } from '../icons.js'

export function mapScreen() {
  return `
    <div style="animation:hondiFade .22s ease">
      <div style="position:relative;height:352px;background:var(--color-neutral-200)">
        <iframe src="jeju-map.html" title="제주 동승 모집글 지도" style="position:absolute;inset:0;width:100%;height:100%;border:0"></iframe>
        <div style="position:absolute;top:14px;left:16px;right:16px;display:flex;gap:8px">
          <div class="input elev-md" style="display:flex;align-items:center;gap:8px;background:var(--color-bg);font-size:13px;min-height:42px">
            ${icons.search()}
            <span style="color:var(--color-neutral-700)">목적지나 오름 이름으로 검색</span>
          </div>
        </div>
      </div>
      <div style="padding:18px 20px 30px;display:flex;flex-direction:column;gap:12px;margin-top:-26px;position:relative;border-radius:30px 30px 0 0;background:var(--color-bg)">
        <div style="width:46px;height:4px;border-radius:999px;background:var(--color-neutral-400);margin:0 auto 4px"></div>
        <div style="display:flex;align-items:baseline;gap:8px">
          <h4 style="margin:0;font-size:18px">이 화면 안의 모집글</h4>
          <span class="text-muted" style="font-size:12px">4건</span>
        </div>
        ${mapPosts
          .map(
            (p) => `
          <div class="card elev-sm" style="gap:8px;padding:15px;cursor:pointer" data-action="goChat">
            <div style="display:flex;align-items:center;gap:8px">
              <span class="tag tag-accent">${escapeHtml(p.time)}</span>
              <span class="tag tag-neutral">${escapeHtml(p.need)}</span>
              <span style="margin-left:auto;font-size:12px;color:var(--color-neutral-700)">${escapeHtml(p.dist)}</span>
            </div>
            <div style="font-size:14.5px;font-weight:600">${escapeHtml(p.route)}</div>
          </div>`,
          )
          .join('')}
      </div>
    </div>`
}
