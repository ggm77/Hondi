import ScreenHeader from '../components/ScreenHeader'
import { matches } from '../data/mock'

interface Props {
  routeLabel: string
  onBack: () => void
  onOpenChat: () => void
  onOpenMap: () => void
}

export default function Matches({ routeLabel, onBack, onOpenChat, onOpenMap }: Props) {
  return (
    <div style={{ animation: 'hondiFade .22s ease' }}>
      <ScreenHeader title="추천 동승 상대" onBack={onBack} tight />
      <div style={{ padding: '6px 20px 32px', display: 'flex', flexDirection: 'column', gap: 14 }}>
        <div style={{ fontSize: 13, color: 'var(--color-neutral-700)', lineHeight: 1.5 }}>
          <b style={{ color: 'var(--color-text)' }}>{routeLabel}</b>
          <br />
          동선과 시간대가 겹치는 여행객 3명을 찾았어요.
        </div>
        {matches.map((m) => (
          <div key={m.name} className="card elev-sm" style={{ gap: 12, padding: 17 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 11 }}>
              <div
                style={{
                  width: 42,
                  height: 42,
                  flex: 'none',
                  borderRadius: '50%',
                  background: m.avatarBg,
                  color: 'var(--color-bg)',
                  display: 'grid',
                  placeItems: 'center',
                  fontFamily: 'var(--font-heading)',
                  fontSize: 16,
                }}
              >
                {m.initial}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 15, fontWeight: 600 }}>{m.name}</div>
                <div className="text-muted" style={{ fontSize: 12 }}>
                  {m.meta}
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontFamily: 'var(--font-heading)', fontSize: 17, color: 'var(--color-accent-2-700)' }}>{m.temp}</div>
                <div className="text-muted" style={{ fontSize: 10 }}>
                  매너온도
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
              <span className="tag tag-accent">{m.overlap}</span>
              <span className="tag tag-neutral">{m.gap}</span>
              <span className="tag tag-accent-2">본인인증</span>
            </div>
            <div style={{ fontSize: 13, lineHeight: 1.5, color: 'var(--color-neutral-800)', padding: '11px 14px', borderRadius: 20, background: 'var(--color-neutral-100)' }}>
              {m.note}
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-primary" onClick={onOpenChat} style={{ flex: 1 }}>
                합류 요청 보내기
              </button>
              <button className="btn btn-secondary" onClick={onOpenMap}>
                경로 보기
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
