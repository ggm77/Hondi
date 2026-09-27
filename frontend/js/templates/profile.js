import { escapeHtml } from '../icons.js'
import { formatJoinedAt, isDeparted } from '../format.js'
import { avatar, rideCard, loadingBlock, emptyBlock } from './shared.js'

export function profileScreen(state) {
  const { me, myRides, busy } = state
  if (!me) return loadingBlock()

  return `
    <div style="animation:hondiFade .22s ease">
      <div style="position:relative;padding:26px 20px 22px;overflow:hidden">
        <div style="position:absolute;top:-40px;right:-30px;width:130px;height:130px;border-radius:50%;background:var(--color-accent-200)"></div>
        <div style="position:relative;display:flex;align-items:center;gap:14px">
          ${avatar(me, 62)}
          <div style="flex:1">
            <h4 style="margin:0 0 2px;font-size:21px">${escapeHtml(me.nickname)}</h4>
            <div class="text-muted" style="font-size:12px">${escapeHtml(formatJoinedAt(me.createdAt))} · 카카오 로그인</div>
          </div>
        </div>
      </div>
      <div style="padding:0 20px 32px;display:flex;flex-direction:column;gap:16px">
        ${stats(myRides)}

        <div class="field">
          <label>닉네임</label>
          <div style="display:flex;gap:8px">
            <input id="nicknameInput" class="input" data-bind="nicknameDraft" data-enter-action="saveNickname" value="${escapeHtml(state.nicknameDraft)}" minlength="2" maxlength="20" style="flex:1">
            <button class="btn btn-secondary" data-action="saveNickname" style="flex:none" ${busy ? 'disabled' : ''}>저장</button>
          </div>
        </div>

        <div>
          <h5 style="margin:0 0 9px;font-size:15px">내 동승</h5>
          ${
            !myRides
              ? loadingBlock()
              : myRides.length
                ? `<div class="card-grid" style="gap:9px">${myRides.map((r) => rideCard(r)).join('')}</div>`
                : emptyBlock('아직 올리거나 참여한 동승이 없어요.', '<button class="btn btn-primary" data-action="goHome">모집글 둘러보기</button>')
          }
        </div>

        <div class="card" style="gap:0;padding:4px 16px">
          <button data-action="logout" style="display:flex;align-items:center;gap:10px;padding:13px 0;background:none;border:0;font:inherit;font-size:13.5px;cursor:pointer;text-align:left;color:var(--color-neutral-700);width:100%">
            <span style="flex:1">로그아웃</span>
          </button>
        </div>
      </div>
    </div>`
}

function stats(myRides) {
  const rides = myRides ?? []
  const upcoming = rides.filter((r) => !isDeparted(r))
  const items = [
    [myRides ? upcoming.filter((r) => r.myStatus === 'HOST').length : '-', '모집 중인 내 글'],
    [myRides ? upcoming.filter((r) => r.myStatus === 'JOINED').length : '-', '참여 예정'],
    [myRides ? rides.length - upcoming.length : '-', '지난 동승'],
  ]
  return `
    <div style="display:flex;gap:9px">
      ${items
        .map(
          ([n, label]) => `
        <div class="card" style="flex:1;gap:2px;padding:14px;align-items:center">
          <div style="font-family:var(--font-heading);font-size:21px">${n}</div>
          <div class="text-muted" style="font-size:11px">${label}</div>
        </div>`,
        )
        .join('')}
    </div>`
}
