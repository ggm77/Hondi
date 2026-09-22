import { useEffect, useRef, useState } from 'react'
import type { ChatMessage, Screen, SheetKind } from './types'
import { autoReplies, initialMessages } from './data/mock'
import BottomTabs from './components/BottomTabs'
import Toast from './components/Toast'
import ReportSheet from './components/ReportSheet'
import BlockedSheet from './components/BlockedSheet'
import Onboarding from './screens/Onboarding'
import Home from './screens/Home'
import Compose from './screens/Compose'
import Matches from './screens/Matches'
import MapScreen from './screens/MapScreen'
import Chat from './screens/Chat'
import Review from './screens/Review'
import Profile from './screens/Profile'

function now() {
  const d = new Date()
  return d.getHours() + ':' + String(d.getMinutes()).padStart(2, '0')
}

export default function App() {
  const [screen, setScreen] = useState<Screen>('onboarding')
  const [sameGender, setSameGender] = useState(true)

  const [from, setFrom] = useState('제주공항')
  const [to, setTo] = useState('애월 한담해변 숙소')
  const [day, setDay] = useState<'오늘' | '내일'>('오늘')
  const [time, setTime] = useState('15:10')
  const [seats, setSeats] = useState(1)
  const [memo, setMemo] = useState('')

  const [draft, setDraft] = useState('')
  const [msgs, setMsgs] = useState<ChatMessage[]>(initialMessages)

  const [chips, setChips] = useState<number[]>([0])
  const [reviewText, setReviewText] = useState('')

  const [reportReason, setReportReason] = useState<number | null>(null)
  const [block, setBlock] = useState(true)
  const [sheet, setSheet] = useState<SheetKind>(null)
  const [toast, setToast] = useState<string | null>(null)

  const toastTimer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)
  const replyTimer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  useEffect(
    () => () => {
      clearTimeout(toastTimer.current)
      clearTimeout(replyTimer.current)
    },
    [],
  )

  const go = (next: Screen) => {
    setScreen(next)
    setSheet(null)
    window.scrollTo({ top: 0 })
  }

  const flash = (message: string) => {
    clearTimeout(toastTimer.current)
    setToast(message)
    toastTimer.current = setTimeout(() => setToast(null), 2600)
  }

  const pushMessage = (t: string, me: boolean) => {
    setMsgs((prev) => [...prev, { me, t, at: now() }])
  }

  const sendText = (t: string) => {
    const trimmed = t.trim()
    if (!trimmed) return
    pushMessage(trimmed, true)
    setDraft('')
    clearTimeout(replyTimer.current)
    const reply = autoReplies[Math.floor(Math.random() * autoReplies.length)]
    replyTimer.current = setTimeout(() => pushMessage(reply, false), 1100)
  }

  const routeLabel = `${from} → ${to} · ${day} ${time}`

  const showTabs = screen !== 'onboarding'

  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-bg)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, maxWidth: 430, width: '100%', margin: '0 auto', paddingBottom: showTabs ? 0 : undefined }}>
        {screen === 'onboarding' && <Onboarding onStart={() => go('home')} />}

        {screen === 'home' && (
          <Home
            from={from}
            to={to}
            onOpenProfile={() => go('profile')}
            onCompose={() => go('compose')}
            onOpenMap={() => go('map')}
            onOpenMatches={() => go('matches')}
          />
        )}

        {screen === 'compose' && (
          <Compose
            from={from}
            to={to}
            day={day}
            time={time}
            seats={seats}
            memo={memo}
            sameGender={sameGender}
            onBack={() => go('home')}
            onChangeFrom={setFrom}
            onChangeTo={setTo}
            onChangeDay={setDay}
            onChangeTime={setTime}
            onIncSeats={() => setSeats((s) => Math.min(3, s + 1))}
            onDecSeats={() => setSeats((s) => Math.max(1, s - 1))}
            onChangeMemo={setMemo}
            onToggleGender={() => setSameGender((v) => !v)}
            onSubmit={() => {
              go('matches')
              flash('모집글을 올렸어요. 동선이 겹치는 여행객을 찾고 있어요.')
            }}
          />
        )}

        {screen === 'matches' && (
          <Matches routeLabel={routeLabel} onBack={() => go('home')} onOpenChat={() => go('chat')} onOpenMap={() => go('map')} />
        )}

        {screen === 'map' && <MapScreen onOpenChat={() => go('chat')} />}

        {screen === 'chat' && (
          <Chat
            msgs={msgs}
            draft={draft}
            onChangeDraft={setDraft}
            onSend={(text) => sendText(text ?? draft)}
            onBack={() => go('matches')}
            onOpenReport={() => setSheet('report')}
            onCallTaxi={() => flash('카카오 T 앱으로 이동해요. 호출과 요금 정산은 직접 진행해 주세요.')}
            onGoReview={() => go('review')}
          />
        )}

        {screen === 'review' && (
          <Review
            chips={chips}
            onToggleChip={(i) => setChips((prev) => (prev.includes(i) ? prev.filter((x) => x !== i) : [...prev, i]))}
            reviewText={reviewText}
            onChangeReviewText={setReviewText}
            onBack={() => go('chat')}
            onOpenReport={() => setSheet('report')}
            onSubmit={() => {
              const newTemp = (39.4 + chips.length * 0.1).toFixed(1) + '℃'
              go('home')
              flash(`후기를 남겼어요. 수민님의 매너온도가 ${newTemp}로 올라갔어요.`)
            }}
          />
        )}

        {screen === 'profile' && (
          <Profile
            sameGender={sameGender}
            onToggleGender={() => setSameGender((v) => !v)}
            onOpenBlocked={() => setSheet('blocked')}
            onLogout={() => go('onboarding')}
          />
        )}
      </div>

      {showTabs && <BottomTabs screen={screen} onNavigate={go} />}

      {sheet === 'report' && (
        <ReportSheet
          reason={reportReason}
          onPickReason={setReportReason}
          block={block}
          onToggleBlock={() => setBlock((v) => !v)}
          onClose={() => setSheet(null)}
          onSubmit={() => {
            setSheet(null)
            go('home')
            flash(block ? '신고를 접수하고 수민님을 차단했어요.' : '신고를 접수했어요. 해당 모집글은 숨겨졌어요.')
          }}
          targetName="수민"
        />
      )}

      {sheet === 'blocked' && (
        <BlockedSheet
          onUnblock={() => {
            setSheet(null)
            flash('차단을 해제했어요.')
          }}
          onClose={() => setSheet(null)}
        />
      )}

      <Toast message={toast} />
    </div>
  )
}
