const pad = (n) => String(n).padStart(2, '0')

function startOfDay(d) {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate())
}

function dayDiff(d, base) {
  return Math.round((startOfDay(d) - startOfDay(base)) / 86400000)
}

// <input type="date"> / <input type="time"> 값
export function toDateInput(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function toTimeInput(d) {
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 날짜·시간 입력값(로컬 시간)을 Date로. 비어 있거나 잘못되면 null
export function fromInputs(date, time) {
  if (!date || !time) return null
  const [y, m, d] = date.split('-').map(Number)
  const [h, min] = time.split(':').map(Number)
  const result = new Date(y, m - 1, d, h, min)
  return Number.isNaN(result.getTime()) ? null : result
}

// 지금부터 30분 뒤를 10분 단위로 올림
export function defaultDeparture() {
  const d = new Date(Date.now() + 30 * 60000)
  d.setMinutes(Math.ceil(d.getMinutes() / 10) * 10, 0, 0)
  return d
}

function dayLabel(d) {
  const diff = dayDiff(d, new Date())
  if (diff === 0) return '오늘'
  if (diff === 1) return '내일'
  if (diff === -1) return '어제'
  return `${d.getMonth() + 1}월 ${d.getDate()}일`
}

// "오늘 15:10", "1월 15일 09:00"
export function formatDeparture(iso) {
  const d = new Date(iso)
  return `${dayLabel(d)} ${toTimeInput(d)}`
}

// 채팅 메시지 시각: 오늘이면 "15:10", 아니면 "1/15 15:10"
export function formatMessageTime(iso) {
  const d = new Date(iso)
  if (dayDiff(d, new Date()) === 0) return toTimeInput(d)
  return `${d.getMonth() + 1}/${d.getDate()} ${toTimeInput(d)}`
}

export function formatJoinedAt(iso) {
  const d = new Date(iso)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 가입`
}

export function isDeparted(ride) {
  return new Date(ride.departureAt) <= new Date()
}

export function isRecruiting(ride) {
  return !isDeparted(ride) && ride.currentCount < ride.capacity
}

// 모집 상태 태그 문구
export function rideStatusLabel(ride) {
  if (isDeparted(ride)) return '출발함'
  const left = ride.capacity - ride.currentCount
  return left > 0 ? `${left}자리 남음` : '모집 마감'
}
