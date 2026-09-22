import type { Screen } from '../types'
import { ChatTabIcon, ComposeTabIcon, HomeTabIcon, MapTabIcon, ProfileTabIcon } from './icons'

interface Props {
  screen: Screen
  onNavigate: (screen: Screen) => void
}

const tabs: { key: Screen; label: string; Icon: typeof HomeTabIcon }[] = [
  { key: 'home', label: '홈', Icon: HomeTabIcon },
  { key: 'map', label: '지도', Icon: MapTabIcon },
  { key: 'compose', label: '모집', Icon: ComposeTabIcon },
  { key: 'chat', label: '채팅', Icon: ChatTabIcon },
  { key: 'profile', label: '나', Icon: ProfileTabIcon },
]

export default function BottomTabs({ screen, onNavigate }: Props) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        padding: '9px 8px 14px',
        borderTop: '1px solid var(--color-divider)',
        background: 'var(--color-bg)',
      }}
    >
      {tabs.map(({ key, label, Icon }) => {
        const active = screen === key
        return (
          <button
            key={key}
            onClick={() => onNavigate(key)}
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 4,
              padding: '4px 0',
              background: 'none',
              border: 0,
              cursor: 'pointer',
              font: 'inherit',
              fontSize: 10.5,
              color: active ? 'var(--color-accent)' : 'var(--color-neutral-600)',
            }}
          >
            <Icon />
            {label}
          </button>
        )
      })}
    </div>
  )
}
