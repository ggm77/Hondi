import { mapPosts } from '../data/mock'
import { SearchIcon } from '../components/icons'

interface Props {
  onOpenChat: () => void
}

export default function MapScreen({ onOpenChat }: Props) {
  return (
    <div style={{ animation: 'hondiFade .22s ease' }}>
      <div style={{ position: 'relative', height: 352, background: 'var(--color-neutral-200)' }}>
        <iframe
          src="/jeju-map.html"
          title="제주 동승 모집글 지도"
          style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', border: 0 }}
        />
        <div style={{ position: 'absolute', top: 14, left: 16, right: 16, display: 'flex', gap: 8 }}>
          <div className="input elev-md" style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'var(--color-bg)', fontSize: 13, minHeight: 42 }}>
            <SearchIcon style={{ flex: 'none' }} />
            <span style={{ color: 'var(--color-neutral-700)' }}>목적지나 오름 이름으로 검색</span>
          </div>
        </div>
      </div>
      <div style={{ padding: '18px 20px 30px', display: 'flex', flexDirection: 'column', gap: 12, marginTop: -26, position: 'relative', borderRadius: '30px 30px 0 0', background: 'var(--color-bg)' }}>
        <div style={{ width: 46, height: 4, borderRadius: 999, background: 'var(--color-neutral-400)', margin: '0 auto 4px' }} />
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
          <h4 style={{ margin: 0, fontSize: 18 }}>이 화면 안의 모집글</h4>
          <span className="text-muted" style={{ fontSize: 12 }}>
            4건
          </span>
        </div>
        {mapPosts.map((p) => (
          <div key={p.route} className="card elev-sm" style={{ gap: 8, padding: 15, cursor: 'pointer' }} onClick={onOpenChat}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span className="tag tag-accent">{p.time}</span>
              <span className="tag tag-neutral">{p.need}</span>
              <span style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--color-neutral-700)' }}>{p.dist}</span>
            </div>
            <div style={{ fontSize: 14.5, fontWeight: 600 }}>{p.route}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
