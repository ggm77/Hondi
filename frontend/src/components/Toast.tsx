interface Props {
  message: string | null
}

export default function Toast({ message }: Props) {
  if (!message) return null
  return (
    <div
      style={{
        position: 'fixed',
        left: 20,
        right: 20,
        bottom: 22,
        zIndex: 9,
        maxWidth: 390,
        margin: '0 auto',
        padding: '13px 17px',
        borderRadius: 22,
        background: 'var(--color-neutral-900)',
        color: 'var(--color-neutral-100)',
        fontSize: 13,
        lineHeight: 1.45,
        boxShadow: 'var(--shadow-lg)',
        animation: 'hondiToast .2s ease',
      }}
    >
      {message}
    </div>
  )
}
