// 혼디 API 서버 주소
export const API_BASE_URL = 'https://hondi.seohamin.com'

// 카카오 디벨로퍼스 > 앱 > 플랫폼 키 > JavaScript 키 (인가·토큰 요청의 client_id)
export const KAKAO_CLIENT_ID = '241dbd5a81459a9e0e283c797f086bd9'

// 카카오 디벨로퍼스 > 카카오 로그인 > Redirect URI에 같은 값을 등록해야 한다.
export const KAKAO_REDIRECT_URI = location.origin + location.pathname

// 채팅 새 메시지 폴링 간격 (ms)
export const CHAT_POLL_INTERVAL = 3000
