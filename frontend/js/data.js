export const posts = [
  {
    time: '오늘 14:00',
    need: '1명 더',
    temp: '39.4℃',
    route: '제주공항 → 성산일출봉',
    fare: '예상 택시요금 48,000원 · 2명이면 1인 24,000원',
    host: '수민 · 본인인증 · 여성',
  },
  {
    time: '오늘 15:10',
    need: '1명 더',
    temp: '38.1℃',
    route: '제주공항 → 애월 한담해변',
    fare: '예상 택시요금 26,000원 · 2명이면 1인 13,000원',
    host: '은지 · 본인인증 · 여성',
  },
  {
    time: '오늘 16:30',
    need: '2명 더',
    temp: '37.6℃',
    route: '서귀포 버스터미널 → 사려니숲길',
    fare: '예상 택시요금 33,000원 · 3명이면 1인 11,000원',
    host: '가은 · 본인인증 · 여성',
  },
]

export const mapPosts = [
  { time: '오늘 15:10', need: '1명 더', dist: '내 위치에서 1.2km', route: '제주공항 → 애월 한담해변' },
  { time: '오늘 14:00', need: '1명 더', dist: '내 위치에서 1.2km', route: '제주공항 → 성산일출봉' },
  { time: '오늘 16:30', need: '2명 더', dist: '22km', route: '서귀포 버스터미널 → 사려니숲길' },
]

export const matches = [
  {
    initial: '수',
    name: '수민',
    meta: '2030 · 여성 · 동승 6회',
    temp: '39.4℃',
    overlap: '경로 82% 겹침',
    gap: '출발 10분 차이',
    note: '캐리어 하나 있어요. 공항 1번 게이트에서 만나면 좋아요.',
    avatarBg: 'var(--color-accent)',
  },
  {
    initial: '은',
    name: '은지',
    meta: '2030 · 여성 · 동승 3회',
    temp: '38.1℃',
    overlap: '경로 74% 겹침',
    gap: '출발 25분 차이',
    note: '애월 지나 한림까지 가요. 중간에 내려주셔도 괜찮아요.',
    avatarBg: 'var(--color-accent-2)',
  },
  {
    initial: '가',
    name: '가은',
    meta: '2030 · 여성 · 동승 1회',
    temp: '37.6℃',
    overlap: '경로 61% 겹침',
    gap: '출발 40분 차이',
    note: '첫 제주 여행이에요. 시간은 조금 맞출 수 있어요.',
    avatarBg: 'var(--color-neutral-500)',
  },
]

export const reviewChipLabels = [
  '시간 약속을 잘 지켜요',
  '대화가 편했어요',
  '정산이 깔끔했어요',
  '합류 지점이 명확했어요',
  '조용히 이동했어요',
  '또 만나고 싶어요',
]

export const reportReasonLabels = [
  '약속 시간에 나타나지 않았어요',
  '불쾌한 말이나 행동을 했어요',
  '요금을 나누지 않았어요',
  '모집글 내용과 달랐어요',
  '기타',
]

export const receivedReviews = [
  {
    tags: ['시간 약속을 잘 지켜요', '정산이 깔끔해요'],
    text: '공항에서 바로 만나서 애월까지 편하게 갔어요. 다음에 또 뵈면 좋겠습니다.',
    meta: '수민 · 2일 전',
  },
  {
    tags: ['대화가 편했어요'],
    text: '성산 가는 길에 여행 정보도 많이 얻었어요.',
    meta: '은지 · 지난주',
  },
]

export const quickReplies = ['1번 게이트 앞에서 만나요', '몇 시에 도착하세요?', '요금은 반씩 나눠요']

export const initialMessages = [
  { me: false, t: '안녕하세요! 애월 한담 쪽 가시는 거 맞죠?', at: '14:32' },
  { me: true, t: '네 맞아요. 3시 10분쯤 공항 1번 게이트 앞에서 뵐까요?', at: '14:33' },
  { me: false, t: '좋아요. 저는 24인치 캐리어 하나 있어요.', at: '14:34' },
]

export const autoReplies = ['네, 1번 게이트 앞에서 봬요!', '확인했어요. 곧 도착합니다.', '좋아요, 그때 뵐게요.']
