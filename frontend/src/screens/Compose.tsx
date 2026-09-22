import ScreenHeader from '../components/ScreenHeader'
import { InfoIcon } from '../components/icons'

interface Props {
  from: string
  to: string
  day: '오늘' | '내일'
  time: string
  seats: number
  memo: string
  sameGender: boolean
  onBack: () => void
  onChangeFrom: (v: string) => void
  onChangeTo: (v: string) => void
  onChangeDay: (v: '오늘' | '내일') => void
  onChangeTime: (v: string) => void
  onIncSeats: () => void
  onDecSeats: () => void
  onChangeMemo: (v: string) => void
  onToggleGender: () => void
  onSubmit: () => void
}

export default function Compose({
  from,
  to,
  day,
  time,
  seats,
  memo,
  sameGender,
  onBack,
  onChangeFrom,
  onChangeTo,
  onChangeDay,
  onChangeTime,
  onIncSeats,
  onDecSeats,
  onChangeMemo,
  onToggleGender,
  onSubmit,
}: Props) {
  return (
    <div style={{ animation: 'hondiFade .22s ease' }}>
      <ScreenHeader title="동승 모집글 올리기" onBack={onBack} />
      <div style={{ padding: '8px 20px 32px', display: 'flex', flexDirection: 'column', gap: 17 }}>
        <div className="field">
          <label>출발지</label>
          <input className="input" value={from} onChange={(e) => onChangeFrom(e.target.value)} />
        </div>
        <div className="field">
          <label>목적지</label>
          <input className="input" value={to} onChange={(e) => onChangeTo(e.target.value)} />
        </div>
        <div className="field">
          <label>언제 출발해요?</label>
          <div style={{ display: 'flex', gap: 9, alignItems: 'center' }}>
            <div className="seg">
              <label className="seg-opt">
                <input type="radio" name="hondi-day" checked={day === '오늘'} onChange={() => onChangeDay('오늘')} />
                오늘
              </label>
              <label className="seg-opt">
                <input type="radio" name="hondi-day" checked={day === '내일'} onChange={() => onChangeDay('내일')} />
                내일
              </label>
            </div>
            <input className="input" value={time} onChange={(e) => onChangeTime(e.target.value)} style={{ width: 96, textAlign: 'center' }} />
          </div>
        </div>
        <div className="field">
          <label>더 모집할 인원</label>
          <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
            <button className="btn btn-secondary btn-icon" onClick={onDecSeats} style={{ fontSize: 18 }} aria-label="인원 줄이기">
              −
            </button>
            <div style={{ fontFamily: 'var(--font-heading)', fontSize: 20, minWidth: 46, textAlign: 'center' }}>{seats}명</div>
            <button className="btn btn-secondary btn-icon" onClick={onIncSeats} style={{ fontSize: 18 }} aria-label="인원 늘리기">
              +
            </button>
            <div className="text-muted" style={{ fontSize: 12, lineHeight: 1.4 }}>
              나 포함 {seats + 1}명이
              <br />한 대에 탑니다
            </div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '15px 17px', borderRadius: 26, background: 'var(--color-surface)' }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 600 }}>동성끼리만 매칭</div>
            <div className="text-muted" style={{ fontSize: 12 }}>
              이성 간 동승 요청은 받지 않아요
            </div>
          </div>
          <button
            onClick={onToggleGender}
            style={{
              flex: 'none',
              width: 52,
              height: 30,
              borderRadius: 999,
              border: 'none',
              cursor: 'pointer',
              padding: 3,
              background: sameGender ? 'var(--color-accent-2)' : 'var(--color-neutral-400)',
              transition: 'background .18s',
            }}
            aria-label="동성 매칭 전환"
          >
            <span
              style={{
                display: 'block',
                width: 24,
                height: 24,
                borderRadius: '50%',
                background: 'var(--color-bg)',
                boxShadow: 'var(--shadow-sm)',
                transform: sameGender ? 'translateX(22px)' : 'translateX(0)',
                transition: 'transform .18s',
              }}
            />
          </button>
        </div>
        <div className="field">
          <label>한마디 (선택)</label>
          <textarea
            className="input"
            value={memo}
            onChange={(e) => onChangeMemo(e.target.value)}
            placeholder="캐리어 하나 있어요. 공항 1번 게이트에서 만나면 좋겠어요."
            style={{ borderRadius: 24, minHeight: 76 }}
          />
        </div>
        <div style={{ display: 'flex', gap: 11, alignItems: 'flex-start', padding: '15px 17px', borderRadius: 26, background: 'var(--color-accent-100)' }}>
          <InfoIcon style={{ flex: 'none', marginTop: 1 }} />
          <div style={{ fontSize: 12.5, lineHeight: 1.5, color: 'var(--color-accent-800)' }}>
            혼디 가게는 택시를 배차하거나 요금을 정산하지 않아요. 같이 탈 사람만 연결해 드리고, 호출과 요금 나누기는 현장에서 두 분이 직접 하시면 돼요.
          </div>
        </div>
        <button className="btn btn-primary btn-block" onClick={onSubmit} style={{ fontSize: 15, padding: 13 }}>
          혼디 모집글 올리기
        </button>
      </div>
    </div>
  )
}
