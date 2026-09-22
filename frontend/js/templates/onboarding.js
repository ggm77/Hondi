export function onboardingScreen() {
  return `
    <div style="position:relative;min-height:100%;display:flex;flex-direction:column;padding:40px 26px 0;overflow:hidden">
      <div style="position:absolute;top:24px;right:-34px;width:124px;height:124px;border-radius:50%;background:var(--color-accent-300)"></div>
      <div style="position:absolute;top:122px;right:62px;width:34px;height:34px;border-radius:50%;background:var(--color-accent-2-300)"></div>

      <div style="position:relative">
        <div style="font-size:11px;letter-spacing:0.16em;text-transform:uppercase;color:var(--color-accent-700);font-weight:600">Jeju · 택시 동승 매칭</div>
        <h1 style="font-size:54px;line-height:1.04;margin:14px 0 10px">혼디<br>가게</h1>
        <p style="font-size:17px;line-height:1.5;margin:0;max-width:250px">제주도 여행,<br>택시비도 같이 나눠요</p>
        <p style="font-size:12px;color:var(--color-neutral-700);margin:10px 0 0">'혼디'는 제주말로 함께라는 뜻이에요.</p>
      </div>

      <div style="position:relative;display:flex;flex-direction:column;gap:14px;margin-top:38px">
        ${[
          ['1', 'var(--color-accent)', '모집글을 올려요', '출발지 · 목적지 · 희망 시간만 적으면 끝'],
          ['2', 'var(--color-accent-2)', '동선이 겹치는 사람을 찾아요', '경로와 시간대가 비슷한 여행객만 추천'],
          ['3', 'var(--color-neutral-400)', '채팅으로 만날 곳을 정해요', '택시 호출과 요금 정산은 현장에서 직접'],
        ]
          .map(
            ([n, bg, title, desc]) => `
          <div style="display:flex;align-items:flex-start;gap:13px">
            <div class="avatar" style="width:30px;height:30px;background:${bg};font-size:13px">${n}</div>
            <div style="padding-top:3px">
              <div style="font-size:15px;font-weight:600">${title}</div>
              <div style="font-size:13px;color:var(--color-neutral-700)">${desc}</div>
            </div>
          </div>`,
          )
          .join('')}
      </div>

      <div style="position:relative;margin-top:auto;padding-bottom:26px;padding-top:40px;display:flex;flex-direction:column;gap:8px;z-index:2">
        <button class="btn btn-primary btn-block" data-action="goHome" style="font-size:15px;padding:13px">휴대폰 번호로 시작하기</button>
        <button class="btn btn-ghost btn-block" data-action="goHome" style="font-size:14px">먼저 둘러보기</button>
      </div>

      <div style="position:absolute;left:-60px;right:-60px;bottom:0;height:250px;pointer-events:none">
        <div style="position:absolute;bottom:0;left:8%;width:300px;height:190px;border-radius:50% 50% 0 0 / 100% 100% 0 0;background:var(--color-accent-2-200)"></div>
        <div style="position:absolute;bottom:0;right:2%;width:260px;height:132px;border-radius:50% 50% 0 0 / 100% 100% 0 0;background:var(--color-accent-2-300)"></div>
        <div style="position:absolute;bottom:0;left:-4%;width:210px;height:92px;border-radius:50% 50% 0 0 / 100% 100% 0 0;background:var(--color-accent-200)"></div>
      </div>
    </div>`
}
