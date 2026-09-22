import { BackIcon } from './icons'

interface Props {
  title: string
  onBack: () => void
  tight?: boolean
}

export default function ScreenHeader({ title, onBack, tight }: Props) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: tight ? '18px 20px 6px' : '18px 20px 10px' }}>
      <button className="btn btn-icon btn-secondary" onClick={onBack} aria-label="뒤로">
        <BackIcon />
      </button>
      <h4 style={{ margin: 0, fontSize: 19 }}>{title}</h4>
    </div>
  )
}
