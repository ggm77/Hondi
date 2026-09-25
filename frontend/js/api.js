import { API_BASE_URL } from './config.js'

const ACCESS_KEY = 'hondi.accessToken'
const REFRESH_KEY = 'hondi.refreshToken'

export class ApiError extends Error {
  constructor(status, code, message) {
    super(message)
    this.status = status
    this.code = code
  }
}

export const tokens = {
  get access() {
    return localStorage.getItem(ACCESS_KEY)
  },
  get refresh() {
    return localStorage.getItem(REFRESH_KEY)
  },
  save({ accessToken, refreshToken }) {
    localStorage.setItem(ACCESS_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}

// 재발급까지 실패하면 호출된다 (로그인 화면으로 보내기 등)
let onUnauthorized = () => {}

export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn
}

function fallbackMessage(status) {
  if (status === 401) return '다시 로그인해 주세요.'
  if (status === 403) return '접근 권한이 없어요.'
  if (status >= 500) return '서버에서 문제가 생겼어요. 잠시 후 다시 시도해 주세요.'
  return '요청을 처리하지 못했어요.'
}

function parseJson(text) {
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

async function send(method, path, { body, query, token } = {}) {
  const url = new URL(API_BASE_URL + path)
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null) url.searchParams.set(key, value)
  }

  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) })
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.')
  }

  if (res.status === 204) return null
  const data = parseJson(await res.text())
  if (!res.ok) {
    // 401/403 필터 응답이나 Spring 기본 오류는 code가 없을 수 있다
    throw new ApiError(res.status, data?.code ?? null, data?.code ? data.message : fallbackMessage(res.status))
  }
  return data
}

// 동시에 여러 요청이 401을 받아도 재발급은 한 번만 한다
let refreshing = null

function refreshTokens() {
  if (!tokens.refresh) return Promise.resolve(false)
  if (!refreshing) {
    refreshing = send('POST', '/api/v1/auth/token/refresh', { body: { refreshToken: tokens.refresh } })
      .then((res) => {
        tokens.save(res)
        return true
      })
      .catch(() => false)
      .finally(() => {
        refreshing = null
      })
  }
  return refreshing
}

function expireSession() {
  tokens.clear()
  onUnauthorized()
  return new ApiError(401, 'UNAUTHORIZED', '로그인이 만료됐어요. 다시 로그인해 주세요.')
}

async function request(method, path, options) {
  if (!tokens.access) throw expireSession()
  try {
    return await send(method, path, { ...options, token: tokens.access })
  } catch (e) {
    if (e.status !== 401) throw e
  }

  if (!(await refreshTokens())) throw expireSession()
  try {
    return await send(method, path, { ...options, token: tokens.access })
  } catch (e) {
    if (e.status === 401) throw expireSession()
    throw e
  }
}

export const api = {
  loginWithKakao: (kakaoAccessToken) =>
    send('POST', '/api/v1/auth/oauth2/kakao', { body: { accessToken: kakaoAccessToken } }),

  getMe: () => request('GET', '/api/v1/user/me'),
  updateUser: (id, body) => request('PATCH', `/api/v1/user/${id}`, { body }),

  createRide: (body) => request('POST', '/api/v1/ride', { body }),
  getRide: (id) => request('GET', `/api/v1/ride/${id}`),
  updateRide: (id, body) => request('PATCH', `/api/v1/ride/${id}`, { body }),
  deleteRide: (id) => request('DELETE', `/api/v1/ride/${id}`),

  getRides: (page, size = 20) => request('GET', '/api/v1/rides', { query: { page, size } }),
  matchRides: (query) => request('GET', '/api/v1/rides/match', { query }),
  getMyRides: () => request('GET', '/api/v1/rides/me'),

  joinRide: (id) => request('POST', `/api/v1/ride/${id}/participant`),
  leaveRide: (id) => request('DELETE', `/api/v1/ride/${id}/participant/me`),

  getChatRooms: () => request('GET', '/api/v1/chats/rooms'),
  getMessages: (rideId, query) => request('GET', `/api/v1/chats/${rideId}/messages`, { query }),
  sendMessage: (rideId, content) => request('POST', `/api/v1/chats/${rideId}/messages`, { body: { content } }),
  markRead: (rideId, lastMessageId) => request('POST', `/api/v1/chats/${rideId}/read`, { query: { lastMessageId } }),
}
