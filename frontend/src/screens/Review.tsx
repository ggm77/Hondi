import ScreenHeader from '../components/ScreenHeader'
import { reviewChipLabels } from '../data/mock'

interface Props {
  chips: number[]
  onToggleChip: (index: number) => void
  reviewText: string
  onChangeReviewText: (v: string) => void
  onBack: () => void
  onOpenReport: () => void
  onSubmit: () => void
}

export default function Review({ chips, onToggleChip, reviewText, onChangeReviewText, onBack, onOpenReport, onSubmit }: Props) {
  const newTemp = (39.4 + chips.length * 0.1).toFixed(1) + '℃'
  const tempWidth = Math.min(96, 66 + chips.length * 2) + '%'

  return (
    <div style={{ animation: 'hondiFade .22s ease' }}>
      <ScreenHeader title="동승은 어땠어요?" onBack={onBack} tight />
      <div style={{ padding: '8px 20px 32px', display: 'flex', flexDirection: 'column', gap: 18 }}>
        <div className="card elev-sm" style={{ gap: 14, padding: 18, alignItems: 'center', textAlign: 'center' }}>
          <div
            style={{
              width: 54,
              height: 54,
              borderRadius: '50%',
              background: 'var(--color-accent)',
              color: 'var(--color-bg)',
              display: 'grid',
              placeItems: 'center',
              fontFamily: 'var(--font-heading)',
              fontSize: 20,
            }}
          >
            수
          </div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 600 }}>수민님과 함께 탔어요</div>
            <div className="text-muted" style={{ fontSize: 12 }}>
              제주공항 → 애월 한담해변 · 오늘 15:10
            </div>
          </div>
          <div style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginBottom: 6 }}>
              <span className="text-muted">수민님의 매너온도</span>
              <span style={{ fontWeight: 600, color: 'var(--color-accent-2-700)' }}>39.4℃ → {newTemp}</span>
            </div>
            <div style={{ height: 9, borderRadius: 999, background: 'var(--color-neutral-300)', overflow: 'hidden' }}>
              <div style={{ width: tempWidth, height: '100%', borderRadius: 999, background: 'var(--color-accent-2)', transition: 'width .3s' }} />
            </div>
          </div>
        </div>
        <div>
          <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>좋았던 점을 골라주세요</div>
          <div className="text-muted" style={{ fontSize: 12, marginBottom: 11 }}>
            고른 항목만 상대에게 보여요. 매너온도는 후기가 쌓일수록 올라갑니다.
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 7 }}>
            {reviewChipLabels.map((t, i) => {
              const on = chips.includes(i)
              return (
                <button
                  key={t}
                  onClick={() => onToggleChip(i)}
                  style={{
                    cursor: 'pointer',
                    font: 'inherit',
                    fontSize: 12.5,
                    padding: '8px 14px',
                    borderRadius: 999,
                    background: on ? 'var(--color-accent-2-200)' : 'transparent',
                    color: on ? 'var(--color-accent-2-800)' : 'var(--color-text)',
                    border: `1px solid ${on ? 'var(--color-accent-2-400)' : 'var(--color-divider)'}`,
                  }}
                >
                  {t}
                </button>
              )
            })}
          </div>
        </div>
        <div className="field">
          <label>한 줄 후기 (선택)</label>
          <textarea
            className="input"
            value={reviewText}
            onChange={(e) => onChangeReviewText(e.target.value)}
            placeholder="공항에서 바로 만나서 편하게 이동했어요."
            style={{ borderRadius: 24, minHeight: 76 }}
          />
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary" onClick={onOpenReport} style={{ flex: 'none', fontSize: 13 }}>
            문제가 있었어요
          </button>
          <button className="btn btn-primary" onClick={onSubmit} style={{ flex: 1 }}>
            후기 남기기
          </button>
        </div>
      </div>
    </div>
  )
}
