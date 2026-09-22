interface Props {
  onUnblock: () => void
  onClose: () => void
}

export default function BlockedSheet({ onUnblock, onClose }: Props) {
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
          gap: 14,
        }}
      >
        <div style={{ width: 46, height: 4, borderRadius: 999, background: 'var(--color-neutral-400)', margin: '0 auto' }} />
        <h4 style={{ margin: 0, fontSize: 19 }}>차단한 사용자</h4>
        <div className="card" style={{ gap: 10, padding: 15, flexDirection: 'row', alignItems: 'center' }}>
          <div
            style={{
              width: 38,
              height: 38,
              flex: 'none',
              borderRadius: '50%',
              background: 'var(--color-neutral-400)',
              color: 'var(--color-bg)',
              display: 'grid',
              placeItems: 'center',
              fontFamily: 'var(--font-heading)',
            }}
          >
            ㅈ
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 600 }}>지우</div>
            <div className="text-muted" style={{ fontSize: 11.5 }}>
              약속 시간 미준수 · 3월 신고
            </div>
          </div>
          <button className="btn btn-secondary" onClick={onUnblock} style={{ fontSize: 12 }}>
            차단 해제
          </button>
        </div>
        <button className="btn btn-secondary btn-block" onClick={onClose}>
          닫기
        </button>
      </div>
    </div>
  )
}
