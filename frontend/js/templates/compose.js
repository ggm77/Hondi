import { icons, escapeHtml } from '../icons.js'
import { screenHeader } from './shared.js'

export function composeScreen(state) {
  return `
    <div style="animation:hondiFade .22s ease">
      ${screenHeader('동승 모집글 올리기', 'goHome')}
      <div style="padding:8px 20px 32px;display:flex;flex-direction:column;gap:17px">
        <div class="field">
          <label>출발지</label>
          <input class="input" data-bind="from" value="${escapeHtml(state.from)}">
        </div>
        <div class="field">
          <label>목적지</label>
          <input class="input" data-bind="to" value="${escapeHtml(state.to)}">
        </div>
        <div class="field">
          <label>언제 출발해요?</label>
          <div style="display:flex;gap:9px;align-items:center">
            <div class="seg">
              <label class="seg-opt">
                <input type="radio" name="hondi-day" data-action="setDay" data-arg="오늘" ${state.day === '오늘' ? 'checked' : ''}>오늘
              </label>
              <label class="seg-opt">
                <input type="radio" name="hondi-day" data-action="setDay" data-arg="내일" ${state.day === '내일' ? 'checked' : ''}>내일
              </label>
            </div>
            <input class="input" data-bind="time" value="${escapeHtml(state.time)}" style="width:96px;text-align:center">
          </div>
        </div>
        <div class="field">
          <label>더 모집할 인원</label>
          <div style="display:flex;align-items:center;gap:14px">
            <button class="btn btn-secondary btn-icon" data-action="decSeats" style="font-size:18px" aria-label="인원 줄이기">−</button>
            <div style="font-family:var(--font-heading);font-size:20px;min-width:46px;text-align:center">${state.seats}명</div>
            <button class="btn btn-secondary btn-icon" data-action="incSeats" style="font-size:18px" aria-label="인원 늘리기">+</button>
            <div class="text-muted" style="font-size:12px;line-height:1.4">나 포함 ${state.seats + 1}명이<br>한 대에 탑니다</div>
          </div>
        </div>
        <div style="display:flex;align-items:center;gap:13px;padding:15px 17px;border-radius:26px;background:var(--color-surface)">
          <div style="flex:1">
            <div style="font-size:14px;font-weight:600">동성끼리만 매칭</div>
            <div class="text-muted" style="font-size:12px">이성 간 동승 요청은 받지 않아요</div>
          </div>
          <button data-action="toggleGender" aria-label="동성 매칭 전환" style="flex:none;width:52px;height:30px;border-radius:999px;border:none;cursor:pointer;padding:3px;background:${state.sameGender ? 'var(--color-accent-2)' : 'var(--color-neutral-400)'};transition:background .18s">
            <span style="display:block;width:24px;height:24px;border-radius:50%;background:var(--color-bg);box-shadow:var(--shadow-sm);transform:${state.sameGender ? 'translateX(22px)' : 'translateX(0)'};transition:transform .18s"></span>
          </button>
        </div>
        <div class="field">
          <label>한마디 (선택)</label>
          <textarea class="input" data-bind="memo" placeholder="캐리어 하나 있어요. 공항 1번 게이트에서 만나면 좋겠어요." style="border-radius:24px;min-height:76px">${escapeHtml(state.memo)}</textarea>
        </div>
        <div style="display:flex;gap:11px;align-items:flex-start;padding:15px 17px;border-radius:26px;background:var(--color-accent-100)">
          ${icons.info()}
          <div style="font-size:12.5px;line-height:1.5;color:var(--color-accent-800)">혼디 가게는 택시를 배차하거나 요금을 정산하지 않아요. 같이 탈 사람만 연결해 드리고, 호출과 요금 나누기는 현장에서 두 분이 직접 하시면 돼요.</div>
        </div>
        <button class="btn btn-primary btn-block" data-action="submitPost" style="font-size:15px;padding:13px">혼디 모집글 올리기</button>
      </div>
    </div>`
}
