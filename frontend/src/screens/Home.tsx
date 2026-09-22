import { posts } from '../data/mock'
import { ShieldIcon, UserIcon } from '../components/icons'

interface Props {
  from: string
  to: string
  onOpenProfile: () => void
  onCompose: () => void
  onOpenMap: () => void
  onOpenMatches: () => void
}

export default function Home({ from, to, onOpenProfile, onCompose, onOpenMap, onOpenMatches }: Props) {
  return (
    <div style={{ padding: '22px 20px 30px', display: 'flex', flexDirection: 'column', gap: 18, animation: 'hondiFade .22s ease' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 12, color: 'var(--color-neutral-700)' }}>안녕하세요, 지민님</div>
          <h3 style={{ margin: '4px 0 0', fontSize: 26 }}>오늘은 어디 갑서?</h3>
        </div>
        <button className="btn btn-icon btn-secondary" onClick={onOpenProfile} style={{ flex: 'none' }} aria-label="프로필">
          <UserIcon />
        </button>
      </div>

      <div className="card elev-sm" style={{ gap: 12, padding: 18 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 9, height: 9, borderRadius: '50%', border: '2.75px solid var(--color-accent)' }} />
          <div style={{ flex: 1, fontSize: 14, fontWeight: 600 }}>{from}</div>
        </div>
        <div
          style={{
            marginLeft: 3,
            width: 2,
            height: 14,
            backgroundImage: 'radial-gradient(circle, var(--color-neutral-400) 1px, transparent 1.2px)',
            backgroundSize: '2px 5px',
          }}
        />
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent-2)" strokeWidth={2.75} strokeLinecap="round" style={{ marginLeft: -1 }}>
            <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0z" />
          </svg>
          <div style={{ flex: 1, fontSize: 14, fontWeight: 600 }}>{to}</div>
        </div>
        <button className="btn btn-primary btn-block" onClick={onCompose} style={{ marginTop: 6 }}>
          이 동선으로 같이 탈 사람 찾기
        </button>
      </div>

      <div
        style={{
          height: 10,
          backgroundImage: 'radial-gradient(circle, var(--color-neutral-400) 2px, transparent 2.4px)',
          backgroundSize: '15px 10px',
          opacity: 0.65,
        }}
      />

      <div>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 12 }}>
          <h4 style={{ margin: 0, fontSize: 19 }}>혼디 모집 중</h4>
          <span className="text-muted" style={{ fontSize: 12 }}>
            제주 전역 12건
          </span>
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault()
              onOpenMap()
            }}
            style={{ marginLeft: 'auto', fontSize: 12, fontWeight: 600 }}
          >
            지도로 보기
          </a>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {posts.map((p) => (
            <div key={p.route} className="card elev-sm" style={{ gap: 9, padding: 16, cursor: 'pointer' }} onClick={onOpenMatches}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span className="tag tag-accent">{p.time}</span>
                <span className="tag tag-neutral">{p.need}</span>
                <span style={{ marginLeft: 'auto', fontSize: 12, fontWeight: 600, color: 'var(--color-accent-2-700)' }}>{p.temp}</span>
              </div>
              <div style={{ fontSize: 15, fontWeight: 600, lineHeight: 1.35 }}>{p.route}</div>
              <div style={{ fontSize: 12, color: 'var(--color-neutral-700)' }}>{p.fare}</div>
              <div className="card-meta">{p.host}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: 'flex', gap: 11, alignItems: 'flex-start', padding: '15px 17px', borderRadius: 26, background: 'var(--color-accent-2-100)' }}>
        <ShieldIcon style={{ flex: 'none', marginTop: 1 }} />
        <div style={{ fontSize: 12.5, lineHeight: 1.5, color: 'var(--color-accent-2-800)' }}>
          본인인증한 여행객만 매칭돼요. 기본값은 <b>동성 매칭</b>이고, 언제든 신고·차단할 수 있어요.
        </div>
      </div>
    </div>
  )
}
