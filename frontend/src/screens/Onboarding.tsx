interface Props {
  onStart: () => void
}

export default function Onboarding({ onStart }: Props) {
  return (
    <div style={{ position: 'relative', minHeight: '100%', display: 'flex', flexDirection: 'column', padding: '40px 26px 0', overflow: 'hidden' }}>
      <div style={{ position: 'absolute', top: 24, right: -34, width: 124, height: 124, borderRadius: '50%', background: 'var(--color-accent-300)' }} />
      <div style={{ position: 'absolute', top: 122, right: 62, width: 34, height: 34, borderRadius: '50%', background: 'var(--color-accent-2-300)' }} />

      <div style={{ position: 'relative' }}>
        <div style={{ fontSize: 11, letterSpacing: '0.16em', textTransform: 'uppercase', color: 'var(--color-accent-700)', fontWeight: 600 }}>
          Jeju · 택시 동승 매칭
        </div>
        <h1 style={{ fontSize: 54, lineHeight: 1.04, margin: '14px 0 10px' }}>
          혼디
          <br />
          가게
        </h1>
        <p style={{ fontSize: 17, lineHeight: 1.5, margin: 0, maxWidth: 250 }}>
          제주도 여행,
          <br />
          택시비도 같이 나눠요
        </p>
        <p style={{ fontSize: 12, color: 'var(--color-neutral-700)', margin: '10px 0 0' }}>
          '혼디'는 제주말로 함께라는 뜻이에요.
        </p>
      </div>

      <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', gap: 14, marginTop: 38 }}>
        {[
          ['1', 'var(--color-accent)', '모집글을 올려요', '출발지 · 목적지 · 희망 시간만 적으면 끝'],
          ['2', 'var(--color-accent-2)', '동선이 겹치는 사람을 찾아요', '경로와 시간대가 비슷한 여행객만 추천'],
          ['3', 'var(--color-neutral-400)', '채팅으로 만날 곳을 정해요', '택시 호출과 요금 정산은 현장에서 직접'],
        ].map(([n, bg, title, desc]) => (
          <div key={n} style={{ display: 'flex', alignItems: 'flex-start', gap: 13 }}>
            <div
              style={{
                width: 30,
                height: 30,
                flex: 'none',
                borderRadius: '50%',
                background: bg,
                color: 'var(--color-bg)',
                display: 'grid',
                placeItems: 'center',
                fontSize: 13,
                fontFamily: 'var(--font-heading)',
              }}
            >
              {n}
            </div>
            <div style={{ paddingTop: 3 }}>
              <div style={{ fontSize: 15, fontWeight: 600 }}>{title}</div>
              <div style={{ fontSize: 13, color: 'var(--color-neutral-700)' }}>{desc}</div>
            </div>
          </div>
        ))}
      </div>

      <div style={{ position: 'relative', marginTop: 'auto', paddingBottom: 26, paddingTop: 40, display: 'flex', flexDirection: 'column', gap: 8, zIndex: 2 }}>
        <button className="btn btn-primary btn-block" onClick={onStart} style={{ fontSize: 15, padding: 13 }}>
          휴대폰 번호로 시작하기
        </button>
        <button className="btn btn-ghost btn-block" onClick={onStart} style={{ fontSize: 14 }}>
          먼저 둘러보기
        </button>
      </div>

      <div style={{ position: 'absolute', left: -60, right: -60, bottom: 0, height: 250, pointerEvents: 'none' }}>
        <div style={{ position: 'absolute', bottom: 0, left: '8%', width: 300, height: 190, borderRadius: '50% 50% 0 0 / 100% 100% 0 0', background: 'var(--color-accent-2-200)' }} />
        <div style={{ position: 'absolute', bottom: 0, right: '2%', width: 260, height: 132, borderRadius: '50% 50% 0 0 / 100% 100% 0 0', background: 'var(--color-accent-2-300)' }} />
        <div style={{ position: 'absolute', bottom: 0, left: '-4%', width: 210, height: 92, borderRadius: '50% 50% 0 0 / 100% 100% 0 0', background: 'var(--color-accent-200)' }} />
      </div>
    </div>
  )
}
