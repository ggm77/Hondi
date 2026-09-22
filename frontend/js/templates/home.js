import { posts } from '../data.js'
import { icons, escapeHtml } from '../icons.js'

export function homeScreen(state) {
  return `
    <div style="padding:22px 20px 30px;display:flex;flex-direction:column;gap:18px;animation:hondiFade .22s ease">
      <div style="display:flex;align-items:flex-start;gap:12px">
        <div style="flex:1">
          <div style="font-size:12px;color:var(--color-neutral-700)">안녕하세요, 지민님</div>
          <h3 style="margin:4px 0 0;font-size:26px">오늘은 어디 갑서?</h3>
        </div>
        <button class="btn btn-icon btn-secondary" data-action="goProfile" style="flex:none" aria-label="프로필">${icons.user()}</button>
      </div>

      <div class="card elev-sm" style="gap:12px;padding:18px">
        <div style="display:flex;align-items:center;gap:10px">
          <div style="width:9px;height:9px;border-radius:50%;border:2.75px solid var(--color-accent)"></div>
          <div style="flex:1;font-size:14px;font-weight:600">${escapeHtml(state.from)}</div>
        </div>
        <div style="margin-left:3px;width:2px;height:14px;background-image:radial-gradient(circle, var(--color-neutral-400) 1px, transparent 1.2px);background-size:2px 5px"></div>
        <div style="display:flex;align-items:center;gap:10px">
          ${icons.pin()}
          <div style="flex:1;font-size:14px;font-weight:600">${escapeHtml(state.to)}</div>
        </div>
        <button class="btn btn-primary btn-block" data-action="goCompose" style="margin-top:6px">이 동선으로 같이 탈 사람 찾기</button>
      </div>

      <div style="height:10px;background-image:radial-gradient(circle, var(--color-neutral-400) 2px, transparent 2.4px);background-size:15px 10px;opacity:.65"></div>

      <div>
        <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:12px">
          <h4 style="margin:0;font-size:19px">혼디 모집 중</h4>
          <span class="text-muted" style="font-size:12px">제주 전역 12건</span>
          <a href="#" data-action="goMap" style="margin-left:auto;font-size:12px;font-weight:600">지도로 보기</a>
        </div>
        <div style="display:flex;flex-direction:column;gap:10px">
          ${posts
            .map(
              (p) => `
            <div class="card elev-sm" style="gap:9px;padding:16px;cursor:pointer" data-action="goMatches">
              <div style="display:flex;align-items:center;gap:8px">
                <span class="tag tag-accent">${escapeHtml(p.time)}</span>
                <span class="tag tag-neutral">${escapeHtml(p.need)}</span>
                <span style="margin-left:auto;font-size:12px;font-weight:600;color:var(--color-accent-2-700)">${escapeHtml(p.temp)}</span>
              </div>
              <div style="font-size:15px;font-weight:600;line-height:1.35">${escapeHtml(p.route)}</div>
              <div style="font-size:12px;color:var(--color-neutral-700)">${escapeHtml(p.fare)}</div>
              <div class="card-meta">${escapeHtml(p.host)}</div>
            </div>`,
            )
            .join('')}
        </div>
      </div>

      <div style="display:flex;gap:11px;align-items:flex-start;padding:15px 17px;border-radius:26px;background:var(--color-accent-2-100)">
        ${icons.shield()}
        <div style="font-size:12.5px;line-height:1.5;color:var(--color-accent-2-800)">본인인증한 여행객만 매칭돼요. 기본값은 <b>동성 매칭</b>이고, 언제든 신고·차단할 수 있어요.</div>
      </div>
    </div>`
}
