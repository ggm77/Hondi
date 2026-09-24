import { reviewChipLabels } from '../data.js'
import { escapeHtml } from '../icons.js'
import { screenHeader } from './shared.js'

export function reviewScreen(state) {
  const newTemp = (39.4 + state.chips.length * 0.1).toFixed(1) + '℃'
  const tempWidth = Math.min(96, 66 + state.chips.length * 2) + '%'

  return `
    <div style="animation:hondiFade .22s ease">
      ${screenHeader('동승은 어땠어요?', 'goChat', true)}
      <div style="padding:8px 20px 32px;display:flex;flex-direction:column;gap:18px">
        <div class="card elev-sm" style="gap:14px;padding:18px;align-items:center;text-align:center">
          <div class="avatar" style="width:54px;height:54px;background:var(--color-accent);font-size:20px">수</div>
          <div>
            <div style="font-size:16px;font-weight:600">수민님과 함께 탔어요</div>
            <div class="text-muted" style="font-size:12px">제주공항 → 애월 한담해변 · 오늘 15:10</div>
          </div>
          <div style="width:100%">
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:6px">
              <span class="text-muted">수민님의 매너온도</span>
              <span style="font-weight:600;color:var(--color-accent-2-700)">39.4℃ → ${newTemp}</span>
            </div>
            <div style="height:9px;border-radius:999px;background:var(--color-neutral-300);overflow:hidden">
              <div style="width:${tempWidth};height:100%;border-radius:999px;background:var(--color-accent-2);transition:width .3s"></div>
            </div>
          </div>
        </div>
        <div>
          <div style="font-size:14px;font-weight:600;margin-bottom:4px">좋았던 점을 골라주세요</div>
          <div class="text-muted" style="font-size:12px;margin-bottom:11px">고른 항목만 상대에게 보여요. 매너온도는 후기가 쌓일수록 올라갑니다.</div>
          <div style="display:flex;flex-wrap:wrap;gap:7px">
            ${reviewChipLabels
              .map((t, i) => {
                const on = state.chips.includes(i)
                return `<button data-action="toggleChip" data-arg="${i}" style="cursor:pointer;font:inherit;font-size:12.5px;padding:8px 14px;border-radius:999px;background:${on ? 'var(--color-accent-2-200)' : 'transparent'};color:${on ? 'var(--color-accent-2-800)' : 'var(--color-text)'};border:1px solid ${on ? 'var(--color-accent-2-400)' : 'var(--color-divider)'}">${escapeHtml(t)}</button>`
              })
              .join('')}
          </div>
        </div>
        <div class="field">
          <label>한 줄 후기 (선택)</label>
          <textarea class="input" data-bind="reviewText" placeholder="공항에서 바로 만나서 편하게 이동했어요." style="border-radius:24px;min-height:76px">${escapeHtml(state.reviewText)}</textarea>
        </div>
        <div style="display:flex;gap:8px">
          <button class="btn btn-secondary" data-action="openReport" style="flex:none;font-size:13px">문제가 있었어요</button>
          <button class="btn btn-primary" data-action="submitReview" style="flex:1">후기 남기기</button>
        </div>
      </div>
    </div>`
}
