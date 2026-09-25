import { icons, escapeHtml } from '../icons.js'
import { toDateInput } from '../format.js'
import { screenHeader, placeSelect, routeLine, loadingBlock } from './shared.js'

export function composeScreen(state) {
  const { form } = state
  const editing = form.mode === 'edit'
  const header = editing ? screenHeader('모집글 수정', 'goRide', false, form.rideId) : screenHeader('동승 모집글 올리기', 'goHome')
  if (editing && !form.loaded) return header + loadingBlock()

  return `
    <div style="animation:hondiFade .22s ease">
      ${header}
      <div style="padding:8px 20px 32px;display:flex;flex-direction:column;gap:17px">
        ${
          editing
            ? `<div class="card" style="padding:16px 18px;gap:8px">
                ${routeLine(form.origin.name, form.dest.name)}
                <div class="text-muted" style="font-size:11.5px">출발지와 목적지는 바꿀 수 없어요.</div>
              </div>`
            : `<div class="field">
                <label>출발지</label>
                ${placeSelect('setFormOrigin', form.origin, true)}
              </div>
              <div class="field">
                <label>목적지</label>
                ${placeSelect('setFormDest', form.dest, false)}
              </div>`
        }
        <div class="field">
          <label>언제 출발해요?</label>
          <div style="display:flex;gap:8px">
            <input class="input" type="date" data-bind="form.date" value="${escapeHtml(form.date)}" min="${toDateInput(new Date())}" style="flex:1">
            <input class="input" type="time" data-bind="form.time" value="${escapeHtml(form.time)}" style="width:140px;flex:none">
          </div>
        </div>
        <div class="field">
          <label>최대 인원 (나 포함)</label>
          <div style="display:flex;align-items:center;gap:14px">
            <button class="btn btn-secondary btn-icon" data-action="decCapacity" style="font-size:18px" aria-label="인원 줄이기" ${form.capacity <= form.minCapacity ? 'disabled' : ''}>−</button>
            <div style="font-family:var(--font-heading);font-size:20px;min-width:46px;text-align:center">${form.capacity}명</div>
            <button class="btn btn-secondary btn-icon" data-action="incCapacity" style="font-size:18px" aria-label="인원 늘리기" ${form.capacity >= 6 ? 'disabled' : ''}>+</button>
            <div class="text-muted" style="font-size:12px;line-height:1.4">나 포함 ${form.capacity}명이<br>한 대에 탑니다</div>
          </div>
        </div>
        <div class="field">
          <label>한마디 (선택)</label>
          <textarea class="input" data-bind="form.memo" maxlength="500" placeholder="캐리어 하나 있어요. 공항 3번 게이트 앞에서 만나면 좋겠어요." style="border-radius:24px;min-height:76px">${escapeHtml(form.memo)}</textarea>
        </div>
        <div style="display:flex;gap:11px;align-items:flex-start;padding:15px 17px;border-radius:26px;background:var(--color-accent-100)">
          ${icons.info()}
          <div style="font-size:12.5px;line-height:1.5;color:var(--color-accent-800)">혼디 가게는 택시를 배차하거나 요금을 정산하지 않아요. 같이 탈 사람만 연결해 드리고, 호출과 요금 나누기는 현장에서 직접 하시면 돼요.</div>
        </div>
        <button class="btn btn-primary btn-block" data-action="submitForm" style="font-size:15px;padding:13px" ${state.busy ? 'disabled' : ''}>
          ${editing ? '수정 내용 저장' : '혼디 모집글 올리기'}
        </button>
      </div>
    </div>`
}
