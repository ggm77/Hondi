import { receivedReviews } from '../data/mock'
import { CheckIcon, ChevronRightIcon, GenderIcon } from '../components/icons'

interface Props {
  sameGender: boolean
  onToggleGender: () => void
  onOpenBlocked: () => void
  onLogout: () => void
}

export default function Profile({ sameGender, onToggleGender, onOpenBlocked, onLogout }: Props) {
  return (
    <div style={{ animation: 'hondiFade .22s ease' }}>
      <div style={{ position: 'relative', padding: '26px 20px 22px', overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: -40, right: -30, width: 130, height: 130, borderRadius: '50%', background: 'var(--color-accent-200)' }} />
        <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 14 }}>
          <div
            style={{
              width: 62,
              height: 62,
              flex: 'none',
              borderRadius: '50%',
              background: 'var(--color-accent-2)',
              color: 'var(--color-bg)',
              display: 'grid',
              placeItems: 'center',
              fontFamily: 'var(--font-heading)',
              fontSize: 22,
            }}
          >
            지
          </div>
          <div style={{ flex: 1 }}>
            <h4 style={{ margin: '0 0 2px', fontSize: 21 }}>혼디여행자 지민</h4>
            <div className="text-muted" style={{ fontSize: 12 }}>
              2026년 3월 가입 · 제주 3회 방문
            </div>
          </div>
        </div>
      </div>
      <div style={{ padding: '0 20px 32px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div className="card elev-sm" style={{ gap: 10, padding: 17 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ fontSize: 14, fontWeight: 600 }}>내 매너온도</span>
            <span style={{ fontFamily: 'var(--font-heading)', fontSize: 20, color: 'var(--color-accent-2-700)' }}>38.6℃</span>
          </div>
          <div style={{ height: 9, borderRadius: 999, background: 'var(--color-neutral-300)', overflow: 'hidden' }}>
            <div style={{ width: '62%', height: '100%', borderRadius: 999, background: 'var(--color-accent-2)' }} />
          </div>
          <div className="text-muted" style={{ fontSize: 11.5 }}>
            받은 후기 4개 · 노쇼 0회
          </div>
        </div>

        <div style={{ display: 'flex', gap: 9 }}>
          {[
            ['3', '동승 완료'],
            ['2', '진행 중 모집'],
            ['4.9', '평균 별점'],
          ].map(([n, label]) => (
            <div key={label} className="card" style={{ flex: 1, gap: 2, padding: 14, alignItems: 'center' }}>
              <div style={{ fontFamily: 'var(--font-heading)', fontSize: 21 }}>{n}</div>
              <div className="text-muted" style={{ fontSize: 11 }}>
                {label}
              </div>
            </div>
          ))}
        </div>

        <div>
          <h5 style={{ margin: '0 0 9px', fontSize: 15 }}>본인인증</h5>
          <div className="card" style={{ gap: 0, padding: '4px 16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '13px 0' }}>
              <CheckIcon style={{ flex: 'none' }} />
              <div style={{ flex: 1, fontSize: 13.5 }}>휴대폰 본인인증</div>
              <span className="tag tag-accent-2">완료</span>
            </div>
            <div style={{ height: 1, background: 'var(--color-divider)' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '13px 0' }}>
              <GenderIcon style={{ flex: 'none' }} />
              <div style={{ flex: 1, fontSize: 13.5 }}>동성 매칭만 허용</div>
              <button
                onClick={onToggleGender}
                style={{
                  flex: 'none',
                  width: 46,
                  height: 27,
                  borderRadius: 999,
                  border: 'none',
                  cursor: 'pointer',
                  padding: 3,
                  background: sameGender ? 'var(--color-accent-2)' : 'var(--color-neutral-400)',
                }}
                aria-label="동성 매칭 전환"
              >
                <span
                  style={{
                    display: 'block',
                    width: 21,
                    height: 21,
                    borderRadius: '50%',
                    background: 'var(--color-bg)',
                    boxShadow: 'var(--shadow-sm)',
                    transform: sameGender ? 'translateX(19px)' : 'translateX(0)',
                    transition: 'transform .18s',
                  }}
                />
              </button>
            </div>
          </div>
        </div>

        <div>
          <h5 style={{ margin: '0 0 9px', fontSize: 15 }}>받은 후기</h5>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 9 }}>
            {receivedReviews.map((r) => (
              <div key={r.meta} className="card" style={{ gap: 7, padding: 15 }}>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  {r.tags.map((tag, i) => (
                    <span key={tag} className={i === 0 ? 'tag tag-accent-2' : 'tag tag-neutral'}>
                      {tag}
                    </span>
                  ))}
                </div>
                <div style={{ fontSize: 13, lineHeight: 1.5 }}>{r.text}</div>
                <div className="card-meta">{r.meta}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="card" style={{ gap: 0, padding: '4px 16px' }}>
          <button
            onClick={onOpenBlocked}
            style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '13px 0', background: 'none', border: 0, font: 'inherit', fontSize: 13.5, cursor: 'pointer', textAlign: 'left', width: '100%' }}
          >
            <span style={{ flex: 1 }}>차단한 사용자</span>
            <span className="text-muted" style={{ fontSize: 12 }}>
              1명
            </span>
            <ChevronRightIcon />
          </button>
          <div style={{ height: 1, background: 'var(--color-divider)' }} />
          <button
            onClick={onLogout}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '13px 0',
              background: 'none',
              border: 0,
              font: 'inherit',
              fontSize: 13.5,
              cursor: 'pointer',
              textAlign: 'left',
              color: 'var(--color-neutral-700)',
              width: '100%',
            }}
          >
            <span style={{ flex: 1 }}>로그아웃</span>
          </button>
        </div>
      </div>
    </div>
  )
}
