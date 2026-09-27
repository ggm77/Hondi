import { KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI } from './config.js'
import { api, tokens } from './api.js'

const STATE_KEY = 'hondi.kakaoState'

function randomState() {
  const bytes = crypto.getRandomValues(new Uint8Array(16))
  return Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('')
}

// 카카오 인가 화면으로 이동한다. 돌아오면 ?code= 또는 ?error= 가 붙는다
export function startKakaoLogin() {
  if (!KAKAO_CLIENT_ID) throw new Error('js/config.js에 KAKAO_CLIENT_ID를 먼저 설정해 주세요.')

  const state = randomState()
  sessionStorage.setItem(STATE_KEY, state)

  const url = new URL('https://kauth.kakao.com/oauth/authorize')
  url.search = new URLSearchParams({
    client_id: KAKAO_CLIENT_ID,
    redirect_uri: KAKAO_REDIRECT_URI,
    response_type: 'code',
    state,
  })
  location.assign(url)
}

// 카카오 인가 후 돌아온 경우 쿼리를 읽고 주소창에서 지운다. 콜백이 아니면 null
export function takeKakaoCallback() {
  const params = new URLSearchParams(location.search)
  if (!params.has('code') && !params.has('error')) return null

  history.replaceState(null, '', location.pathname + location.hash)
  return {
    code: params.get('code'),
    error: params.get('error'),
    state: params.get('state'),
  }
}

async function requestKakaoToken(code) {
  let res
  try {
    res = await fetch('https://kauth.kakao.com/oauth/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: KAKAO_CLIENT_ID,
        redirect_uri: KAKAO_REDIRECT_URI,
        code,
      }),
    })
  } catch {
    res = null
  }
  const data = res ? await res.json().catch(() => null) : null
  if (!res?.ok || !data?.access_token) throw new Error('카카오 토큰을 받지 못했어요. 다시 시도해 주세요.')
  return data.access_token
}

// 인가 코드 → 카카오 액세스 토큰 → 혼디 토큰 순서로 교환하고 저장한다
export async function completeKakaoLogin({ code, error, state }) {
  const expectedState = sessionStorage.getItem(STATE_KEY)
  sessionStorage.removeItem(STATE_KEY)

  if (error) throw new Error(error === 'access_denied' ? '카카오 로그인을 취소했어요.' : '카카오 로그인에 실패했어요.')
  if (!expectedState || state !== expectedState) throw new Error('로그인 요청이 올바르지 않아요. 다시 시도해 주세요.')

  const kakaoAccessToken = await requestKakaoToken(code)
  const res = await api.loginWithKakao(kakaoAccessToken)
  tokens.save(res)
}
