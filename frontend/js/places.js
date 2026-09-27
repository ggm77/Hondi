// 출발지·목적지로 고를 수 있는 제주 주요 장소 (서비스 지역: 위도 33.0~34.1, 경도 126.0~127.1)
export const PLACES = [
  { name: '제주국제공항', lat: 33.5104, lon: 126.4914 },
  { name: '제주시외버스터미널', lat: 33.4997, lon: 126.5155 },
  { name: '제주항 연안여객터미널', lat: 33.5195, lon: 126.543 },
  { name: '동문재래시장', lat: 33.5118, lon: 126.5283 },
  { name: '함덕해수욕장', lat: 33.5433, lon: 126.6697 },
  { name: '김녕해수욕장', lat: 33.5577, lon: 126.7593 },
  { name: '월정리해변', lat: 33.5563, lon: 126.7958 },
  { name: '비자림', lat: 33.4906, lon: 126.811 },
  { name: '성산항 (우도행)', lat: 33.4735, lon: 126.9336 },
  { name: '성산일출봉', lat: 33.4581, lon: 126.9425 },
  { name: '섭지코지', lat: 33.424, lon: 126.931 },
  { name: '사려니숲길', lat: 33.4126, lon: 126.6553 },
  { name: '한라산 성판악 탐방로', lat: 33.3849, lon: 126.62 },
  { name: '한라산 영실 탐방로', lat: 33.3625, lon: 126.4972 },
  { name: '서귀포 매일올레시장', lat: 33.25, lon: 126.5634 },
  { name: '천지연폭포', lat: 33.2447, lon: 126.5594 },
  { name: '서귀포 버스터미널', lat: 33.2491, lon: 126.5094 },
  { name: '중문관광단지', lat: 33.2505, lon: 126.412 },
  { name: '산방산', lat: 33.2381, lon: 126.3131 },
  { name: '송악산', lat: 33.2, lon: 126.2917 },
  { name: '모슬포항', lat: 33.2143, lon: 126.2513 },
  { name: '오설록 티뮤지엄', lat: 33.3059, lon: 126.2894 },
  { name: '협재해수욕장', lat: 33.394, lon: 126.2397 },
  { name: '한림공원', lat: 33.3893, lon: 126.2393 },
  { name: '애월 한담해변', lat: 33.4626, lon: 126.3108 },
]

export function isInServiceArea(lat, lon) {
  return lat >= 33.0 && lat <= 34.1 && lon >= 126.0 && lon <= 127.1
}

// 두 지점 사이 대략의 거리 (km, 제주 범위에서는 평면 근사로 충분)
function distanceKm(lat1, lon1, lat2, lon2) {
  const dy = (lat2 - lat1) * 111
  const dx = (lon2 - lon1) * 111 * Math.cos((lat1 * Math.PI) / 180)
  return Math.hypot(dx, dy)
}

export function nearestPlace(lat, lon) {
  const dist = (p) => distanceKm(lat, lon, p.lat, p.lon)
  const place = PLACES.reduce((best, p) => (dist(p) < dist(best) ? p : best))
  return { place, km: dist(place) }
}

const NEAR_KM = 0.7

// 지도에서 고른 지점의 이름. 주요 장소 가까이면 "OO 부근", 아니면 OpenStreetMap 역지오코딩 주소
// 바다처럼 읍·면·동 주소가 없는 곳은 null
export async function describePoint(lat, lon) {
  const { place, km } = nearestPlace(lat, lon)
  if (km <= NEAR_KM) return `${place.name} 부근`

  const url = new URL('https://nominatim.openstreetmap.org/reverse')
  url.search = new URLSearchParams({ format: 'jsonv2', 'accept-language': 'ko', zoom: 18, lat, lon })
  let address
  try {
    const res = await fetch(url)
    address = res.ok ? (await res.json()).address : undefined
  } catch {
    address = undefined
  }
  // 주소를 못 받으면 가장 가까운 장소와 거리로 대신한다
  if (address === undefined) return `${place.name}에서 ${km.toFixed(1)}km`

  const area = address.town ?? address.city_district ?? address.suburb
  const detail = address.village ?? address.road
  if (!area && !detail) return null
  return [address.city, area, detail].filter(Boolean).join(' ')
}
