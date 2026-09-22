import { reportReasonLabels } from '../data/mock'

interface Props {
  reason: number | null
  onPickReason: (index: number) => void
  block: boolean
  onToggleBlock: () => void
  onClose: () => void
  onSubmit: () => void
  targetName: string
}

export default function ReportSheet({ reason, onPickReason, block, onToggleBlock, onClose, onSubmit, targetName }: Props) {
  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 8,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
        background: 'color-mix(in srgb, var(--color-neutral-900) 45%, transparent)',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 430,
          padding: '22px 20px 26px',
          borderRadius: '32px 32px 0 0',
          background: 'var(--color-bg)',
          boxShadow: 'var(--shadow-lg)',
          animation: 'hondiSheet .24s ease',
          display: 'flex',
          flexDirection: 'column',
          gap: 15,
        }}
      >
        <div style={{ width: 46, height: 4, borderRadius: 999, background: 'var(--color-neutral-400)', margin: '0 auto' }} />
        <div>
          <h4 style={{ margin: '0 0 3px', fontSize: 19 }}>{targetName}님을 신고할까요?</h4>
          <div className="text-muted" style={{ fontSize: 12.5, lineHeight: 1.5 }}>
            접수되면 해당 모집글은 자동으로 숨겨지고, 운영팀이 24시간 안에 확인해요.
          </div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {reportReasonLabels.map((t, i) => (
            <label className="radio" key={t} style={{ padding: '9px 2px', gap: 11 }}>
              <input type="radio" name="hondi-reason" checked={reason === i} onChange={() => onPickReason(i)} />
              <span className="dot" />
              <span style={{ fontSize: 13.5 }}>{t}</span>
            </label>
          ))}
        </div>
        <label
          className="radio"
          style={{ gap: 11, padding: '12px 15px', borderRadius: 22, background: 'var(--color-surface)' }}
        >
          <input
            type="checkbox"
            checked={block}
            onChange={onToggleBlock}
            style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }}
          />
          <span
            className="dot"
            style={{
              borderRadius: 6,
              background: block ? 'var(--color-accent)' : 'transparent',
              borderColor: block ? 'var(--color-accent)' : 'var(--color-divider)',
              boxShadow: block ? 'inset 0 0 0 3px var(--color-bg)' : 'none',
            }}
          />
          <span style={{ fontSize: 13.5 }}>이 사용자를 차단하고 다시 추천받지 않기</span>
        </label>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary" onClick={onClose} style={{ flex: 1 }}>
            취소
          </button>
          <button className="btn btn-primary" onClick={onSubmit} style={{ flex: 1 }} disabled={reason === null}>
            신고 접수
          </button>
        </div>
      </div>
    </div>
  )
}
