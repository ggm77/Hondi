import { api, tokens, setUnauthorizedHandler } from './api.js'
import { startKakaoLogin, takeKakaoCallback, completeKakaoLogin } from './auth.js'
import { CHAT_POLL_INTERVAL } from './config.js'
import { PLACES, isInServiceArea } from './places.js'
import { toDateInput, toTimeInput, fromInputs, defaultDeparture, formatDeparture } from './format.js'
import { bottomTabs, toast, confirmSheet } from './templates/shared.js'
import { onboardingScreen } from './templates/onboarding.js'
import { homeScreen } from './templates/home.js'
import { composeScreen } from './templates/compose.js'
import { matchesScreen } from './templates/matches.js'
import { mapScreen, mapPanel } from './templates/map.js'
import { rideScreen } from './templates/ride.js'
import { chatsScreen } from './templates/chats.js'
import { chatScreen, chatMessages } from './templates/chat.js'
import { profileScreen } from './templates/profile.js'

const appEl = document.getElementById('app')
const overlayEl = document.getElementById('overlay')

const initialDeparture = defaultDeparture()

const state = {
  screen: 'onboarding',
  param: null, // 해시의 ID (#ride/100, #chat/100, #edit/100)
  from: null, // 직전 화면 { screen, param }
  authPending: false,
  me: null,
  busy: false, // 요청 중 버튼 중복 클릭 방지

  // 홈 동선 검색 (추천 API 조건)
  search: {
    origin: PLACES.find((p) => p.name === '제주국제공항'),
    dest: PLACES.find((p) => p.name === '성산일출봉'),
    date: toDateInput(initialDeparture),
    time: toTimeInput(initialDeparture),
  },
  form: null, // 모집글 작성·수정

  rides: emptyRides(),
  matches: { items: null, hasNext: false },
  ride: null,
  rooms: null,
  chat: null,
  draft: '',
  myRides: null,
  nicknameDraft: '',

  sheet: null,
  toast: null,
}

function emptyRides() {
  return { items: null, page: 0, hasNext: false, loadingMore: false }
}

const SCREENS = {
  onboarding: onboardingScreen,
  home: homeScreen,
  compose: composeScreen,
  edit: composeScreen,
  matches: matchesScreen,
  map: mapScreen,
  ride: rideScreen,
  chats: chatsScreen,
  chat: chatScreen,
  profile: profileScreen,
}

const NEEDS_PARAM = new Set(['ride', 'chat', 'edit'])

let toastTimer
let navSeq = 0 // 화면을 옮기면 늦게 도착한 이전 화면의 응답을 버린다
let chatTimer = null
let chatPolling = false
let chatScrollMode = null // 다음 채팅 렌더 후 스크롤: 'bottom' | 'keep'

// ── 렌더링 ────────────────────────────────────────────

function render() {
  renderApp()
  renderOverlay()
}

function renderApp() {
  // 지도 화면에 머무는 동안에는 목록만 바꾸고 iframe을 다시 불러오지 않는다
  const panel = state.screen === 'map' && document.getElementById('mapPanel')
  if (panel) {
    panel.innerHTML = mapPanel(state)
    const frame = document.getElementById('mapFrame')
    if (frame) sendRidesToMap(frame)
    return
  }

  const focus = captureFocus()
  const showTabs = state.screen !== 'onboarding'
  withChatScroll(() => {
    appEl.innerHTML = `
      <div class="app-main" data-screen="${state.screen}">${SCREENS[state.screen](state)}</div>
      ${showTabs ? bottomTabs(state.screen) : ''}
    `
  })
  restoreFocus(focus)
}

function renderOverlay() {
  overlayEl.innerHTML = `
    ${state.sheet ? confirmSheet(state.sheet) : ''}
    ${toast(state.toast)}
  `
}

// 새 메시지는 메시지 영역만 다시 그려서 입력 중인 글자(한글 조합 포함)를 건드리지 않는다
function renderChatMessages() {
  const el = document.getElementById('chatMessages')
  if (state.screen !== 'chat' || !el) return
  withChatScroll(() => {
    el.innerHTML = chatMessages(state)
  })
}

function withChatScroll(draw) {
  if (state.screen !== 'chat') {
    draw()
    return
  }
  const doc = document.documentElement
  const nearBottom = window.innerHeight + window.scrollY >= doc.scrollHeight - 120
  const fromBottom = doc.scrollHeight - window.scrollY
  draw()
  const mode = chatScrollMode
  chatScrollMode = null
  if (mode === 'keep') window.scrollTo(0, doc.scrollHeight - fromBottom)
  else if (mode === 'bottom' || nearBottom) window.scrollTo(0, doc.scrollHeight)
}

function captureFocus() {
  const el = document.activeElement
  if (!el || !el.id || !appEl.contains(el)) return null
  let selection = null
  try {
    selection = [el.selectionStart, el.selectionEnd]
  } catch {
    // date/time 입력은 selection이 없다
  }
  return { id: el.id, selection }
}

function restoreFocus(focus) {
  if (!focus) return
  const el = document.getElementById(focus.id)
  if (!el) return
  el.focus({ preventScroll: true })
  if (focus.selection && focus.selection[0] !== null) {
    try {
      el.setSelectionRange(...focus.selection)
    } catch {
      // selection을 지원하지 않는 입력
    }
  }
}

function update(patch) {
  Object.assign(state, patch)
  render()
}

function updateOverlay(patch) {
  Object.assign(state, patch)
  renderOverlay()
}

function flash(message) {
  clearTimeout(toastTimer)
  updateOverlay({ toast: message })
  toastTimer = setTimeout(() => updateOverlay({ toast: null }), 2600)
}

// 401은 로그인 화면으로 보내면서 이미 안내했으니 따로 띄우지 않는다
function report(e) {
  if (e.status !== 401) flash(e.message)
}

// ── 화면 이동 ─────────────────────────────────────────

function go(screen, param = null) {
  if (screen !== 'onboarding' && !tokens.access) {
    screen = 'onboarding'
    param = null
  }
  if (screen === 'onboarding' && tokens.access && !state.authPending) screen = 'home'

  stopChat()
  navSeq++
  state.from = state.screen === 'onboarding' ? null : { screen: state.screen, param: state.param }
  Object.assign(state, { screen, param, sheet: null, busy: false })

  const hash = param === null ? screen : `${screen}/${param}`
  if (location.hash.slice(1) !== hash) location.hash = hash
  window.scrollTo({ top: 0 })

  // 로더의 동기 부분(state.form, state.chat 초기화 등)이 첫 렌더보다 먼저 실행돼야 한다
  const loader = LOADERS[screen]
  if (loader) loader(param, navSeq)
  if (tokens.access && !state.me) loadMe()
  render()
}

function parseHash() {
  const [screen, rawParam] = location.hash.slice(1).split('/')
  const param = /^\d+$/.test(rawParam ?? '') ? Number(rawParam) : null
  if (!SCREENS[screen] || (NEEDS_PARAM.has(screen) && param === null)) {
    return [tokens.access ? 'home' : 'onboarding', null]
  }
  return [screen, NEEDS_PARAM.has(screen) ? param : null]
}

const isFresh = (seq) => seq === navSeq

// ── 데이터 불러오기 ───────────────────────────────────

let mePromise = null

function loadMe() {
  if (!mePromise) {
    mePromise = api
      .getMe()
      .then((me) => {
        state.me = me
        if (!state.nicknameDraft) state.nicknameDraft = me.nickname
        render()
      })
      .catch(report)
      .finally(() => {
        mePromise = null
      })
  }
  return mePromise
}

async function loadRides(_, seq) {
  try {
    const res = await api.getRides(0)
    if (!isFresh(seq)) return
    state.rides = { items: res.rides, page: 0, hasNext: res.hasNext, loadingMore: false }
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    if (!state.rides.items) state.rides.items = []
    render()
    report(e)
  }
}

async function loadMoreRides() {
  const rides = state.rides
  if (!rides.items || rides.loadingMore || !rides.hasNext) return
  rides.loadingMore = true
  render()
  try {
    const res = await api.getRides(rides.page + 1)
    // 페이지 사이에 글이 추가·마감되면 중복이 생길 수 있다
    const seen = new Set(rides.items.map((r) => r.id))
    rides.items = [...rides.items, ...res.rides.filter((r) => !seen.has(r.id))]
    rides.page += 1
    rides.hasNext = res.hasNext
  } catch (e) {
    report(e)
  } finally {
    rides.loadingMore = false
    if (state.rides === rides) render()
  }
}

async function loadMatches(_, seq) {
  state.matches = { items: null, hasNext: false }
  const { origin, dest, date, time } = state.search
  const departure = fromInputs(date, time)
  try {
    const res = await api.matchRides({
      originLat: origin.lat,
      originLon: origin.lon,
      destLat: dest.lat,
      destLon: dest.lon,
      departureAt: departure ? departure.toISOString() : undefined,
      size: 20,
    })
    if (!isFresh(seq)) return
    state.matches = { items: res.rides, hasNext: res.hasNext }
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    state.matches = { items: [], hasNext: false }
    render()
    report(e)
  }
}

async function loadRide(id, seq) {
  if (state.ride?.id !== id) state.ride = null
  try {
    const ride = await api.getRide(id)
    if (!isFresh(seq)) return
    state.ride = ride
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    report(e)
    if (e.code === 'RIDE_NOT_EXIST') go('home')
  }
}

function initCompose() {
  if (state.form?.mode !== 'create') resetFormFromSearch()
}

function resetFormFromSearch() {
  const { origin, dest, date, time } = state.search
  state.form = { mode: 'create', loaded: true, origin, dest, date, time, capacity: 4, minCapacity: 2, memo: '' }
}

async function loadEdit(id, seq) {
  state.form = { mode: 'edit', rideId: id, loaded: false }
  try {
    const ride = await api.getRide(id)
    if (!isFresh(seq)) return
    if (ride.myStatus !== 'HOST') {
      flash('모집글 작성자만 수정할 수 있어요.')
      go('ride', id)
      return
    }
    const departure = new Date(ride.departureAt)
    const date = toDateInput(departure)
    const time = toTimeInput(departure)
    state.form = {
      mode: 'edit',
      rideId: id,
      loaded: true,
      origin: { name: ride.originName, lat: ride.originLat, lon: ride.originLon },
      dest: { name: ride.destName, lat: ride.destLat, lon: ride.destLon },
      date,
      time,
      initialDate: date,
      initialTime: time,
      capacity: ride.capacity,
      initialCapacity: ride.capacity,
      minCapacity: Math.max(2, ride.currentCount),
      memo: ride.memo ?? '',
    }
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    report(e)
    if (e.code === 'RIDE_NOT_EXIST') go('home')
    else if (e.status !== 401) go('ride', id)
  }
}

async function loadRooms(_, seq) {
  try {
    const res = await api.getChatRooms()
    if (!isFresh(seq)) return
    state.rooms = res.rooms
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    if (!state.rooms) state.rooms = []
    render()
    report(e)
  }
}

async function loadProfile(_, seq) {
  state.nicknameDraft = state.me?.nickname ?? ''
  try {
    const [me, mine] = await Promise.all([api.getMe(), api.getMyRides()])
    if (!isFresh(seq)) return
    state.me = me
    state.nicknameDraft = me.nickname
    state.myRides = mine.rides
    render()
  } catch (e) {
    if (!isFresh(seq)) return
    if (!state.myRides) state.myRides = []
    render()
    report(e)
  }
}

const LOADERS = {
  home: loadRides,
  map: loadRides,
  matches: loadMatches,
  ride: loadRide,
  compose: initCompose,
  edit: loadEdit,
  chats: loadRooms,
  chat: openChat,
  profile: loadProfile,
}

// ── 채팅 (HTTP 폴링) ──────────────────────────────────

function mergeMessages(chat, messages) {
  const byId = new Map(chat.messages.map((m) => [m.id, m]))
  for (const m of messages) byId.set(m.id, m)
  chat.messages = [...byId.values()].sort((a, b) => a.id - b.id)
}

async function openChat(rideId, seq) {
  const chat = { rideId, ride: null, messages: [], afterId: 0, readId: 0, hasOlder: false, loadingOlder: false, loaded: false, sending: false }
  state.chat = chat
  state.draft = ''
  try {
    const [ride, page] = await Promise.all([api.getRide(rideId), api.getMessages(rideId, { size: 50 })])
    if (!isFresh(seq)) return
    chat.ride = ride
    chat.messages = page.messages
    chat.afterId = page.messages.at(-1)?.id ?? 0
    chat.hasOlder = page.hasNext
    chat.loaded = true
    chatScrollMode = 'bottom'
    render()
    markRead()
    chatTimer = setInterval(pollChat, CHAT_POLL_INTERVAL)
  } catch (e) {
    if (!isFresh(seq)) return
    report(e)
    if (e.status !== 401) go('chats')
  }
}

function stopChat() {
  clearInterval(chatTimer)
  chatTimer = null
}

function isActiveChat(chat) {
  return state.screen === 'chat' && state.chat === chat
}

async function pollChat() {
  const chat = state.chat
  if (!chat?.loaded || chatPolling || !isActiveChat(chat)) return
  chatPolling = true
  try {
    let hasNext = true
    while (hasNext) {
      const page = await api.getMessages(chat.rideId, { afterId: chat.afterId, size: 100 })
      if (!isActiveChat(chat)) return
      if (page.messages.length) {
        chat.afterId = page.messages.at(-1).id
        mergeMessages(chat, page.messages)
        renderChatMessages()
      }
      hasNext = page.hasNext
    }
    markRead()
  } catch (e) {
    if (!isActiveChat(chat)) return
    // 방이 삭제됐거나 참여가 끝난 경우에만 나간다. 일시적인 오류는 다음 폴링에서 다시 시도
    if (e.status === 403 || e.code === 'RIDE_NOT_EXIST') {
      report(e)
      go('chats')
    } else if (e.status === 401) {
      stopChat()
    }
  } finally {
    chatPolling = false
  }
}

function markRead() {
  const chat = state.chat
  if (!chat || !isActiveChat(chat) || document.hidden) return
  const last = chat.messages.at(-1)
  if (!last || last.id <= chat.readId) return
  const previous = chat.readId
  chat.readId = last.id
  api.markRead(chat.rideId, last.id).catch(() => {
    chat.readId = previous
  })
}

async function sendText(text, fromDraft) {
  const chat = state.chat
  const content = (text ?? '').trim()
  if (!chat?.loaded || !content || chat.sending) return
  chat.sending = true
  try {
    const message = await api.sendMessage(chat.rideId, content)
    if (!isActiveChat(chat)) return
    // afterId는 폴링으로만 올린다. 여기서 올리면 그 사이 다른 사람이 보낸 메시지를 건너뛸 수 있다
    mergeMessages(chat, [message])
    if (fromDraft && state.draft === text) {
      state.draft = ''
      const input = document.getElementById('draftInput')
      if (input) input.value = ''
    }
    chatScrollMode = 'bottom'
    renderChatMessages()
    pollChat()
  } catch (e) {
    report(e)
  } finally {
    chat.sending = false
  }
}

async function loadOlderMessages() {
  const chat = state.chat
  if (!chat?.loaded || chat.loadingOlder || !chat.hasOlder || !chat.messages.length) return
  chat.loadingOlder = true
  renderChatMessages()
  try {
    const page = await api.getMessages(chat.rideId, { beforeId: chat.messages[0].id, size: 50 })
    if (!isActiveChat(chat)) return
    mergeMessages(chat, page.messages)
    chat.hasOlder = page.hasNext
  } catch (e) {
    report(e)
  } finally {
    chat.loadingOlder = false
    chatScrollMode = 'keep'
    renderChatMessages()
  }
}

// ── 지도 iframe 연동 ─────────────────────────────────

function sendRidesToMap(frame) {
  const rides = (state.rides.items ?? []).map((r) => ({
    id: r.id,
    originLat: r.originLat,
    originLon: r.originLon,
    destLat: r.destLat,
    destLon: r.destLon,
    label: `${r.destName} ${formatDeparture(r.departureAt)}`,
  }))
  frame.contentWindow.postMessage({ type: 'hondi-map-rides', rides }, location.origin)
}

window.addEventListener('message', (e) => {
  const frame = document.getElementById('mapFrame')
  if (e.origin !== location.origin || !frame || e.source !== frame.contentWindow) return
  if (e.data?.type === 'hondi-map-ready') sendRidesToMap(frame)
  if (e.data?.type === 'hondi-map-open') go('ride', Number(e.data.id))
})

// ── 장소 선택 ─────────────────────────────────────────

function samePlace(a, b) {
  return a.lat === b.lat && a.lon === b.lon
}

function nearestPlace(lat, lon) {
  const dist = (p) => (p.lat - lat) ** 2 + ((p.lon - lon) * Math.cos((lat * Math.PI) / 180)) ** 2
  return PLACES.reduce((best, p) => (dist(p) < dist(best) ? p : best))
}

function pickPlace(target, key, value) {
  if (value === 'current') {
    useCurrentLocation(target)
    return
  }
  target[key] = PLACES[Number(value)]
}

// 모집글 출발지 이름으로도 쓰이므로 가장 가까운 장소 이름을 붙인다
function useCurrentLocation(target) {
  if (!navigator.geolocation) {
    flash('이 브라우저에서는 현재 위치를 쓸 수 없어요.')
    render()
    return
  }
  flash('현재 위치를 확인하고 있어요…')
  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      const lat = Number(coords.latitude.toFixed(6))
      const lon = Number(coords.longitude.toFixed(6))
      if (!isInServiceArea(lat, lon)) {
        flash('현재 위치가 서비스 지역(제주도) 밖이에요.')
      } else {
        target.origin = { name: `${nearestPlace(lat, lon).name} 부근`, lat, lon }
        flash('현재 위치를 출발지로 정했어요.')
      }
      render()
    },
    () => {
      flash('현재 위치를 가져오지 못했어요. 위치 권한을 확인해 주세요.')
      render()
    },
    { enableHighAccuracy: true, timeout: 10000 },
  )
}

// ── 모집글 작성·수정 ──────────────────────────────────

async function submitForm() {
  const form = state.form
  if (!form?.loaded || state.busy) return

  const creating = form.mode === 'create'
  const departure = fromInputs(form.date, form.time)
  const departureChanged = creating || form.date !== form.initialDate || form.time !== form.initialTime
  if (!departure) return flash('출발 날짜와 시간을 입력해 주세요.')
  if (departureChanged && departure <= new Date()) return flash('출발 시각은 지금 이후로 정해 주세요.')
  if (creating && samePlace(form.origin, form.dest)) return flash('출발지와 목적지가 같아요.')
  const memo = form.memo.trim()

  update({ busy: true })
  try {
    if (creating) {
      const ride = await api.createRide({
        originName: form.origin.name,
        originLat: form.origin.lat,
        originLon: form.origin.lon,
        destName: form.dest.name,
        destLat: form.dest.lat,
        destLon: form.dest.lon,
        departureAt: departure.toISOString(),
        capacity: form.capacity,
        memo: memo || null,
      })
      state.form = null
      go('ride', ride.id)
      flash('모집글을 올렸어요. 동선이 비슷한 여행객에게 추천돼요.')
    } else {
      // 출발 시각이 지난 글도 메모·인원은 고칠 수 있도록 바뀐 값만 보낸다
      const body = { memo }
      if (departureChanged) body.departureAt = departure.toISOString()
      if (form.capacity !== form.initialCapacity) body.capacity = form.capacity
      await api.updateRide(form.rideId, body)
      state.form = null
      go('ride', form.rideId)
      flash('모집글을 수정했어요.')
    }
  } catch (e) {
    update({ busy: false })
    report(e)
  }
}

// ── 참여·취소·삭제 ────────────────────────────────────

async function joinRide(id) {
  if (state.busy) return
  const seq = navSeq
  update({ busy: true })
  try {
    await api.joinRide(id)
    go('chat', id)
    flash('합류했어요! 채팅방에서 만날 곳을 정해 보세요.')
  } catch (e) {
    if (!isFresh(seq)) return
    update({ busy: false })
    report(e)
    if (state.screen === 'ride') loadRide(id, seq)
  }
}

async function leaveRide() {
  const id = state.ride?.id
  if (!id || state.busy) return
  const seq = navSeq
  update({ busy: true })
  try {
    await api.leaveRide(id)
    flash('참여를 취소했어요.')
  } catch (e) {
    report(e)
  }
  if (!isFresh(seq)) return
  update({ busy: false })
  loadRide(id, seq)
}

async function deleteRide() {
  const id = state.ride?.id
  if (!id || state.busy) return
  update({ busy: true })
  try {
    await api.deleteRide(id)
    state.ride = null
    go('home')
    flash('모집글을 삭제했어요.')
  } catch (e) {
    update({ busy: false })
    report(e)
  }
}

// ── 프로필 ────────────────────────────────────────────

async function saveNickname() {
  const nickname = state.nicknameDraft.trim()
  if (!state.me || state.busy) return
  if (nickname.length < 2 || nickname.length > 20) return flash('닉네임은 2~20자로 입력해 주세요.')
  if (nickname === state.me.nickname) return flash('지금 쓰는 닉네임과 같아요.')
  update({ busy: true })
  try {
    const me = await api.updateUser(state.me.id, { nickname })
    update({ me, nicknameDraft: me.nickname, busy: false })
    flash('닉네임을 바꿨어요.')
  } catch (e) {
    update({ busy: false })
    report(e)
  }
}

function resetSession() {
  tokens.clear()
  Object.assign(state, {
    me: null,
    form: null,
    rides: emptyRides(),
    matches: { items: null, hasNext: false },
    ride: null,
    rooms: null,
    chat: null,
    draft: '',
    myRides: null,
    nicknameDraft: '',
  })
}

setUnauthorizedHandler(() => {
  if (state.screen === 'onboarding') return
  resetSession()
  go('onboarding')
  flash('로그인이 만료됐어요. 다시 로그인해 주세요.')
})

// ── 액션 ──────────────────────────────────────────────

function openSheet(sheet) {
  updateOverlay({ sheet })
}

const actions = {
  loginKakao: () => {
    try {
      startKakaoLogin()
    } catch (e) {
      flash(e.message)
    }
  },
  logout: () => {
    resetSession()
    go('onboarding')
    flash('로그아웃했어요.')
  },

  goHome: () => go('home'),
  goCompose: () => go('compose'),
  goMap: () => go('map'),
  goChats: () => go('chats'),
  goProfile: () => go('profile'),
  goRide: (id) => go('ride', Number(id)),
  goChat: (id) => go('chat', Number(id)),
  goEdit: (id) => go('edit', Number(id)),
  goBackFromRide: () => {
    const from = state.from
    if (from && !['ride', 'edit', 'compose'].includes(from.screen)) go(from.screen, from.param)
    else go('home')
  },
  goMatches: () => {
    if (samePlace(state.search.origin, state.search.dest)) return flash('출발지와 목적지가 같아요.')
    go('matches')
  },
  composeFromSearch: () => {
    resetFormFromSearch()
    go('compose')
  },

  setSearchOrigin: (value) => pickPlace(state.search, 'origin', value),
  setSearchDest: (value) => pickPlace(state.search, 'dest', value),
  setFormOrigin: (value) => pickPlace(state.form, 'origin', value),
  setFormDest: (value) => pickPlace(state.form, 'dest', value),
  incCapacity: () => {
    state.form.capacity = Math.min(6, state.form.capacity + 1)
    render()
  },
  decCapacity: () => {
    state.form.capacity = Math.max(state.form.minCapacity, state.form.capacity - 1)
    render()
  },
  submitForm,

  loadMoreRides,
  joinRide: (id) => joinRide(Number(id)),
  askLeaveRide: () =>
    openSheet({
      title: '참여를 취소할까요?',
      desc: '취소하면 이 동승의 채팅방을 더 이상 볼 수 없어요. 모집 중이면 다시 합류할 수 있어요.',
      confirmLabel: '참여 취소',
      onConfirm: leaveRide,
    }),
  askDeleteRide: () =>
    openSheet({
      title: '모집글을 삭제할까요?',
      desc: '참여 기록과 채팅 내용이 함께 삭제되고 되돌릴 수 없어요.',
      confirmLabel: '삭제하기',
      onConfirm: deleteRide,
    }),
  closeSheet: () => updateOverlay({ sheet: null }),
  confirmSheet: () => {
    const onConfirm = state.sheet?.onConfirm
    updateOverlay({ sheet: null })
    if (onConfirm) onConfirm()
  },

  send: () => sendText(state.draft, true),
  sendQuick: (text) => sendText(text, false),
  loadOlderMessages,
  callTaxi: () => flash('카카오 T 앱으로 이동해요. 호출과 요금 정산은 직접 진행해 주세요.'),

  saveNickname,
}

function dispatch(name, arg) {
  const fn = actions[name]
  if (fn) fn(arg)
}

document.addEventListener('click', (e) => {
  if (e.target.classList && e.target.classList.contains('sheet-backdrop')) {
    actions.closeSheet()
    return
  }
  const el = e.target.closest('[data-action]')
  if (!el || el.tagName === 'INPUT' || el.tagName === 'SELECT') return
  e.preventDefault()
  dispatch(el.dataset.action, el.dataset.arg)
})

document.addEventListener('change', (e) => {
  const el = e.target.closest('[data-action]')
  if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'SELECT')) return
  dispatch(el.dataset.action, el.dataset.arg ?? el.value)
})

// data-bind="search.date"처럼 점으로 중첩 상태를 가리킨다
document.addEventListener('input', (e) => {
  const bind = e.target.dataset ? e.target.dataset.bind : null
  if (!bind) return
  const keys = bind.split('.')
  const last = keys.pop()
  const target = keys.reduce((obj, key) => obj?.[key], state)
  if (target) target[last] = e.target.value
})

document.addEventListener('keydown', (e) => {
  const action = e.target.dataset ? e.target.dataset.enterAction : null
  // 한글 조합 중 Enter는 조합 확정용이라 무시한다 (마지막 글자 중복 전송 방지)
  if (!action || e.key !== 'Enter' || e.isComposing || e.keyCode === 229) return
  e.preventDefault()
  dispatch(action)
})

document.addEventListener('visibilitychange', () => {
  if (document.hidden) return
  markRead()
  pollChat()
})

window.addEventListener('hashchange', () => {
  const [screen, param] = parseHash()
  if (screen !== state.screen || param !== state.param) go(screen, param)
})

// ── 시작 ──────────────────────────────────────────────

async function start() {
  const callback = takeKakaoCallback()
  if (!callback) {
    go(...parseHash())
    return
  }

  state.authPending = true
  go('onboarding')
  try {
    await completeKakaoLogin(callback)
    state.authPending = false
    go('home')
    flash('로그인했어요. 반가워요!')
  } catch (e) {
    state.authPending = false
    render()
    flash(e.message)
  }
}

start()
