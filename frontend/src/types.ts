export type Screen =
  | 'onboarding'
  | 'home'
  | 'compose'
  | 'matches'
  | 'map'
  | 'chat'
  | 'review'
  | 'profile'

export type SheetKind = 'report' | 'blocked' | null

export interface ChatMessage {
  me: boolean
  t: string
  at: string
}

export interface Post {
  time: string
  need: string
  temp: string
  route: string
  fare: string
  host: string
}

export interface MapPost {
  time: string
  need: string
  dist: string
  route: string
}

export interface Match {
  initial: string
  name: string
  meta: string
  temp: string
  overlap: string
  gap: string
  note: string
  avatarBg: string
}
