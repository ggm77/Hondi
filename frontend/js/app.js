import { initialMessages, autoReplies } from './data.js'
import { bottomTabs, toast, reportSheet, blockedSheet } from './templates/shared.js'
import { onboardingScreen } from './templates/onboarding.js'
import { homeScreen } from './templates/home.js'
import { composeScreen } from './templates/compose.js'
import { matchesScreen } from './templates/matches.js'
import { mapScreen } from './templates/map.js'
import { chatScreen } from './templates/chat.js'
import { reviewScreen } from './templates/review.js'
import { profileScreen } from './templates/profile.js'

const appEl = document.getElementById('app')
const overlayEl = document.getElementById('overlay')

const state = {
  screen: 'onboarding',
  sameGender: true,

  from: '제주공항',
  to: '애월 한담해변 숙소',
  day: '오늘',
  time: '15:10',
  seats: 1,
  memo: '',

  draft: '',
  msgs: initialMessages.map((m) => ({ ...m })),

  chips: [0],
  reviewText: '',

  reportReason: null,
  reportTarget: '수민',
  block: true,
  sheet: null,
  toast: null,
}

let toastTimer
let replyTimer

const SCREENS = {
  onboarding: onboardingScreen,
  home: homeScreen,
  compose: composeScreen,
  matches: matchesScreen,
  map: mapScreen,
  chat: chatScreen,
  review: reviewScreen,
  profile: profileScreen,
}

function now() {
  const d = new Date()
  return d.getHours() + ':' + String(d.getMinutes()).padStart(2, '0')
}

function render() {
  renderApp()
  renderOverlay()
}

function renderApp() {
  const showTabs = state.screen !== 'onboarding'
  appEl.innerHTML = `
    <div class="app-main" data-screen="${state.screen}">${SCREENS[state.screen](state)}</div>
    ${showTabs ? bottomTabs(state.screen) : ''}
  `
  if (state.screen === 'chat') {
    const messages = document.getElementById('chatMessages')
    if (messages) messages.scrollTop = messages.scrollHeight
  }
}

function renderOverlay() {
  overlayEl.innerHTML = `
    ${state.sheet === 'report' ? reportSheet(state) : ''}
    ${state.sheet === 'blocked' ? blockedSheet() : ''}
    ${toast(state.toast)}
  `
}

function update(patch) {
  Object.assign(state, patch)
  render()
}

function updateOverlay(patch) {
  Object.assign(state, patch)
  renderOverlay()
}

function go(screen) {
  state.screen = screen
  state.sheet = null
  if (location.hash.slice(1) !== screen) location.hash = screen
  window.scrollTo({ top: 0 })
  render()
}

function screenFromHash() {
  const screen = location.hash.slice(1)
  return SCREENS[screen] ? screen : 'onboarding'
}

function flash(message) {
  clearTimeout(toastTimer)
  updateOverlay({ toast: message })
  toastTimer = setTimeout(() => updateOverlay({ toast: null }), 2600)
}

function withMessage(t, me) {
  return [...state.msgs, { me, t, at: now() }]
}

function toggleItem(list, item) {
  return list.includes(item) ? list.filter((x) => x !== item) : [...list, item]
}

function sendText(t) {
  const trimmed = (t ?? '').trim()
  if (!trimmed) return
  update({ msgs: withMessage(trimmed, true), draft: '' })
  const draftInput = document.getElementById('draftInput')
  if (draftInput) draftInput.focus()

  clearTimeout(replyTimer)
  const reply = autoReplies[Math.floor(Math.random() * autoReplies.length)]
  replyTimer = setTimeout(() => update({ msgs: withMessage(reply, false) }), 1100)
}

const actions = {
  goOnboarding: () => go('onboarding'),
  goHome: () => go('home'),
  goCompose: () => go('compose'),
  goMatches: () => go('matches'),
  goMap: () => go('map'),
  goChat: () => go('chat'),
  goReview: () => go('review'),
  goProfile: () => go('profile'),

  setDay: (day) => update({ day }),
  incSeats: () => update({ seats: Math.min(3, state.seats + 1) }),
  decSeats: () => update({ seats: Math.max(1, state.seats - 1) }),
  toggleGender: () => update({ sameGender: !state.sameGender }),
  submitPost: () => {
    go('matches')
    flash('모집글을 올렸어요. 동선이 겹치는 여행객을 찾고 있어요.')
  },

  send: () => sendText(state.draft),
  sendQuick: (text) => sendText(text),
  callTaxi: () => flash('카카오 T 앱으로 이동해요. 호출과 요금 정산은 직접 진행해 주세요.'),

  toggleChip: (arg) => update({ chips: toggleItem(state.chips, Number(arg)) }),
  submitReview: () => {
    const newTemp = (39.4 + state.chips.length * 0.1).toFixed(1) + '℃'
    go('home')
    flash(`후기를 남겼어요. 수민님의 매너온도가 ${newTemp}로 올라갔어요.`)
  },

  openReport: () => updateOverlay({ sheet: 'report' }),
  openBlocked: () => updateOverlay({ sheet: 'blocked' }),
  closeSheet: () => updateOverlay({ sheet: null }),
  pickReason: (arg) => updateOverlay({ reportReason: Number(arg) }),
  toggleBlock: () => updateOverlay({ block: !state.block }),
  submitReport: () => {
    if (state.reportReason === null) return
    const blocked = state.block
    state.sheet = null
    go('home')
    flash(blocked ? '신고를 접수하고 수민님을 차단했어요.' : '신고를 접수했어요. 해당 모집글은 숨겨졌어요.')
  },
  unblock: () => {
    updateOverlay({ sheet: null })
    flash('차단을 해제했어요.')
  },
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
  if (!el || el.tagName === 'INPUT') return
  e.preventDefault()
  dispatch(el.dataset.action, el.dataset.arg)
})

document.addEventListener('change', (e) => {
  const el = e.target.closest('[data-action]')
  if (!el || el.tagName !== 'INPUT') return
  dispatch(el.dataset.action, el.dataset.arg)
})

document.addEventListener('input', (e) => {
  const bind = e.target.dataset ? e.target.dataset.bind : null
  if (!bind) return
  state[bind] = e.target.value
})

document.addEventListener('keydown', (e) => {
  const action = e.target.dataset ? e.target.dataset.enterAction : null
  if (action && e.key === 'Enter') {
    e.preventDefault()
    dispatch(action)
  }
})

window.addEventListener('hashchange', () => {
  const screen = screenFromHash()
  if (screen !== state.screen) go(screen)
})

state.screen = screenFromHash()
render()
