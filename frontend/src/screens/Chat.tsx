import type { KeyboardEvent } from 'react'
import type { ChatMessage } from '../types'
import { quickReplies } from '../data/mock'
import { BackIcon, KebabIcon, SendIcon } from '../components/icons'

interface Props {
  msgs: ChatMessage[]
  draft: string
  onChangeDraft: (v: string) => void
  onSend: (text?: string) => void
  onBack: () => void
  onOpenReport: () => void
  onCallTaxi: () => void
  onGoReview: () => void
}

export default function Chat({ msgs, draft, onChangeDraft, onSend, onBack, onOpenReport, onCallTaxi, onGoReview }: Props) {
  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      onSend()
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100%', animation: 'hondiFade .22s ease' }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          padding: '16px 18px',
          borderBottom: '1px solid var(--color-divider)',
          background: 'var(--color-bg)',
          position: 'sticky',
          top: 0,
          zIndex: 3,
        }}
      >
        <button className="btn btn-icon btn-secondary" onClick={onBack} aria-label="뒤로">
          <BackIcon />
        </button>
        <div
          style={{
            width: 34,
            height: 34,
            borderRadius: '50%',
            background: 'var(--color-accent)',
            color: 'var(--color-bg)',
            display: 'grid',
            placeItems: 'center',
            fontFamily: 'var(--font-heading)',
            fontSize: 14,
          }}
        >
          수
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 14, fontWeight: 600 }}>수민</div>
          <div className="text-muted" style={{ fontSize: 11 }}>
            39.4℃ · 본인인증 · 동승 6회
          </div>
        </div>
        <button className="btn btn-icon btn-secondary" onClick={onOpenReport} aria-label="더보기">
          <KebabIcon />
        </button>
      </div>

      <div style={{ flex: 1, padding: '16px 18px 12px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ padding: '14px 16px', borderRadius: 24, background: 'var(--color-accent-2-100)', fontSize: 12, lineHeight: 1.5, color: 'var(--color-accent-2-800)' }}>
          <b>제주공항 → 애월 한담해변 · 오늘 15:10</b>
          <br />
          합류가 확정됐어요. 택시 호출과 요금 나누기는 만나서 직접 진행해 주세요.
        </div>
        {msgs.map((m, i) => (
          <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: m.me ? 'flex-end' : 'flex-start', gap: 3 }}>
            <div
              style={{
                maxWidth: '78%',
                padding: '10px 14px',
                borderRadius: m.me ? '20px 20px 6px 20px' : '20px 20px 20px 6px',
                background: m.me ? 'var(--color-accent)' : 'var(--color-surface)',
                color: m.me ? 'var(--color-bg)' : 'var(--color-text)',
                fontSize: 13.5,
                lineHeight: 1.5,
              }}
            >
              {m.t}
            </div>
            <div className="text-muted" style={{ fontSize: 10 }}>
              {m.at}
            </div>
          </div>
        ))}
      </div>

      <div style={{ position: 'sticky', bottom: 0, background: 'var(--color-bg)', padding: '10px 18px 16px', borderTop: '1px solid var(--color-divider)' }}>
        <div style={{ display: 'flex', gap: 6, overflowX: 'auto', paddingBottom: 9 }}>
          {quickReplies.map((t) => (
            <button key={t} className="btn btn-secondary" onClick={() => onSend(t)} style={{ flex: 'none', fontSize: 12, padding: '5px 12px' }}>
              {t}
            </button>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            className="input"
            value={draft}
            onChange={(e) => onChangeDraft(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder="메시지 보내기"
            style={{ flex: 1, minHeight: 42 }}
          />
          <button className="btn btn-primary btn-icon" onClick={() => onSend()} style={{ width: 42, height: 42, flex: 'none' }} aria-label="보내기">
            <SendIcon />
          </button>
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 9 }}>
          <button className="btn btn-secondary" onClick={onCallTaxi} style={{ flex: 1, fontSize: 13 }}>
            카카오 T로 택시 호출
          </button>
          <button className="btn btn-ghost" onClick={onGoReview} style={{ fontSize: 13 }}>
            동승 완료
          </button>
        </div>
      </div>
    </div>
  )
}
