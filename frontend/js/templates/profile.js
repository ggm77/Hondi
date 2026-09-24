import { receivedReviews } from '../data.js'
import { icons, escapeHtml } from '../icons.js'

export function profileScreen(state) {
  return `
    <div style="animation:hondiFade .22s ease">
      <div style="position:relative;padding:26px 20px 22px;overflow:hidden">
        <div style="position:absolute;top:-40px;right:-30px;width:130px;height:130px;border-radius:50%;background:var(--color-accent-200)"></div>
        <div style="position:relative;display:flex;align-items:center;gap:14px">
          <div class="avatar" style="width:62px;height:62px;background:var(--color-accent-2);font-size:22px">지</div>
          <div style="flex:1">
            <h4 style="margin:0 0 2px;font-size:21px">혼디여행자 지민</h4>
            <div class="text-muted" style="font-size:12px">2026년 3월 가입 · 제주 3회 방문</div>
          </div>
        </div>
      </div>
      <div style="padding:0 20px 32px;display:flex;flex-direction:column;gap:16px">
        <div class="card elev-sm" style="gap:10px;padding:17px">
          <div style="display:flex;justify-content:space-between;align-items:baseline">
            <span style="font-size:14px;font-weight:600">내 매너온도</span>
            <span style="font-family:var(--font-heading);font-size:20px;color:var(--color-accent-2-700)">38.6℃</span>
          </div>
          <div style="height:9px;border-radius:999px;background:var(--color-neutral-300);overflow:hidden">
            <div style="width:62%;height:100%;border-radius:999px;background:var(--color-accent-2)"></div>
          </div>
          <div class="text-muted" style="font-size:11.5px">받은 후기 4개 · 노쇼 0회</div>
        </div>

        <div style="display:flex;gap:9px">
          ${[
            ['3', '동승 완료'],
            ['2', '진행 중 모집'],
            ['4.9', '평균 별점'],
          ]
            .map(
              ([n, label]) => `
            <div class="card" style="flex:1;gap:2px;padding:14px;align-items:center">
              <div style="font-family:var(--font-heading);font-size:21px">${n}</div>
              <div class="text-muted" style="font-size:11px">${label}</div>
            </div>`,
            )
            .join('')}
        </div>

        <div>
          <h5 style="margin:0 0 9px;font-size:15px">본인인증</h5>
          <div class="card" style="gap:0;padding:4px 16px">
            <div style="display:flex;align-items:center;gap:10px;padding:13px 0">
              ${icons.check()}
              <div style="flex:1;font-size:13.5px">휴대폰 본인인증</div>
              <span class="tag tag-accent-2">완료</span>
            </div>
            <div style="height:1px;background:var(--color-divider)"></div>
            <div style="display:flex;align-items:center;gap:10px;padding:13px 0">
              ${icons.gender()}
              <div style="flex:1;font-size:13.5px">동성 매칭만 허용</div>
              <button data-action="toggleGender" aria-label="동성 매칭 전환" style="flex:none;width:46px;height:27px;border-radius:999px;border:none;cursor:pointer;padding:3px;background:${state.sameGender ? 'var(--color-accent-2)' : 'var(--color-neutral-400)'}">
                <span style="display:block;width:21px;height:21px;border-radius:50%;background:var(--color-bg);box-shadow:var(--shadow-sm);transform:${state.sameGender ? 'translateX(19px)' : 'translateX(0)'};transition:transform .18s"></span>
              </button>
            </div>
          </div>
        </div>

        <div>
          <h5 style="margin:0 0 9px;font-size:15px">받은 후기</h5>
          <div class="card-grid" style="gap:9px">
            ${receivedReviews
              .map(
                (r) => `
              <div class="card" style="gap:7px;padding:15px">
                <div style="display:flex;gap:6px;flex-wrap:wrap">
                  ${r.tags.map((tag, i) => `<span class="tag ${i === 0 ? 'tag-accent-2' : 'tag-neutral'}">${escapeHtml(tag)}</span>`).join('')}
                </div>
                <div style="font-size:13px;line-height:1.5">${escapeHtml(r.text)}</div>
                <div class="card-meta">${escapeHtml(r.meta)}</div>
              </div>`,
              )
              .join('')}
          </div>
        </div>

        <div class="card" style="gap:0;padding:4px 16px">
          <button data-action="openBlocked" style="display:flex;align-items:center;gap:10px;padding:13px 0;background:none;border:0;font:inherit;font-size:13.5px;cursor:pointer;text-align:left;width:100%">
            <span style="flex:1">차단한 사용자</span>
            <span class="text-muted" style="font-size:12px">1명</span>
            ${icons.chevronRight()}
          </button>
          <div style="height:1px;background:var(--color-divider)"></div>
          <button data-action="goOnboarding" style="display:flex;align-items:center;gap:10px;padding:13px 0;background:none;border:0;font:inherit;font-size:13.5px;cursor:pointer;text-align:left;color:var(--color-neutral-700);width:100%">
            <span style="flex:1">로그아웃</span>
          </button>
        </div>
      </div>
    </div>`
}
