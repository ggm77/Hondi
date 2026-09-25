# 혼디 가게 API 명세서

- 작성일: 2026-09-25
- 구현 기준: `8e55928` — 모집글과 채팅의 동시성 및 참여 상태 오류 수정
- 범위: 현재 구현된 인증, 사용자, 모집글, 참여, 채팅, 상태 확인 API 20개
- 서버 주소: 실행 환경에서 정한 API 서버 주소를 사용한다. 아래 경로는 서버 주소 뒤에 붙인다.

## 1. 공통 규칙

### 요청과 응답

| 항목 | 규칙 |
| --- | --- |
| 서비스 API 접두사 | `/api/v1` |
| 상태 확인 경로 | `/ping`, `/ready` — `/api/v1`을 붙이지 않는다. |
| 요청 본문 | 본문이 있는 API는 `Content-Type: application/json` 사용 |
| 성공 응답 | 응답 객체를 바로 반환한다. `data`, `success` 같은 공통 래퍼는 없다. |
| 생성 성공 | 모집글 생성, 참여, 메시지 전송도 `200 OK`를 반환한다. |
| 본문 없는 성공 | `204 No Content` — JSON 파싱을 시도하지 않는다. |
| ID | JSON 정수, 서버 타입은 `Long` |
| 좌표 | JSON 숫자, 위도 `Lat` / 경도 `Lon` |
| 일시 | ISO 8601 문자열. UTC 예: `2030-01-15T06:00:00Z` |
| 빈 목록 | `[]` |
| 선택 값 | 응답 필드의 `null` 가능 여부는 각 표에 표시 |

예시의 날짜·ID·토큰은 설명용 값이다. 모집글 등록/수정 시 `departureAt`은 요청 시점보다 미래로 바꿔야 한다. `2030-01-15T06:00:00Z`는 한국 시간으로 같은 날 15시다. 시간대 없는 `2030-01-15T15:00:00` 대신 `Z` 또는 UTC 오프셋이 있는 값을 전송한다.

### 인증

인증이 필요한 API에는 **혼디 서버에서 발급한 액세스 토큰**을 넣는다.

```http
Authorization: Bearer <hondi-access-token>
```

- 카카오 액세스 토큰은 카카오 로그인 API의 요청 본문에만 사용한다.
- 로그인, 토큰 재발급, `/ping`, `/ready`는 인증 없이 호출할 수 있다.
- 그 외 아래 API는 `USER` 또는 `ADMIN` 권한이 필요하다.
- 다른 사용자의 프로필 수정, 다른 방장의 모집글 수정/삭제는 `ADMIN`이어도 허용하지 않는다.
- 액세스 토큰이 없거나 잘못되었거나 만료된 경우 인증이 필요한 API는 `401`을 반환한다. 리프레시 토큰을 인증 헤더에 넣어도 `401`이다.
- CORS 허용 Origin은 서버의 `cors.allowed-origins` 설정을 따른다.

### 경로 변수

`{id}`와 `{rideId}`는 필수 정수 경로 변수다. 사용자 API의 `{id}`는 사용자 ID, 모집글 API의 `{id}`와 채팅 API의 `{rideId}`는 모집글 ID다.

### 사용자 상태

| 필드 | 값 | 의미 |
| --- | --- | --- |
| `role` | `USER` | 일반 사용자 |
| `role` | `ADMIN` | 관리자 |
| `myStatus` | `HOST` | 내가 모집글 작성자 |
| `myStatus` | `JOINED` | 내가 참여 중 |
| `myStatus` | `NONE` | 작성자도 참여자도 아님 |

`capacity`와 `currentCount`는 **방장을 포함한 전체 인원**이다. `members` 배열은 **방장을 제외한 참여자**다. 예를 들어 방장과 참여자 한 명이면 `currentCount = 2`, `members.length = 1`이다.

## 2. API 목록

인증 열의 `필수`는 공통 Bearer 인증을 뜻하며, 추가 소유권 조건은 각 API에 설명한다.

| 메서드 | 경로 | 기능 | 인증 | 성공 |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/oauth2/kakao` | 카카오 로그인 및 자동 가입 | 없음 | 200 |
| POST | `/api/v1/auth/token/refresh` | 토큰 재발급 | 없음 | 200 |
| GET | `/api/v1/user/me` | 내 프로필 조회 | 필수 | 200 |
| GET | `/api/v1/user/{id}` | 사용자 프로필 조회 | 필수 | 200 |
| PATCH | `/api/v1/user/{id}` | 내 프로필 수정 | 필수 | 200 |
| POST | `/api/v1/ride` | 모집글 생성 | 필수 | 200 |
| GET | `/api/v1/ride/{id}` | 모집글 상세 조회 | 필수 | 200 |
| PATCH | `/api/v1/ride/{id}` | 모집글 수정 | 필수 | 200 |
| DELETE | `/api/v1/ride/{id}` | 모집글 삭제 | 필수 | 204 |
| GET | `/api/v1/rides` | 모집 중인 글 목록 | 필수 | 200 |
| GET | `/api/v1/rides/match` | 경로·시간이 비슷한 글 추천 | 필수 | 200 |
| GET | `/api/v1/rides/me` | 내가 작성하거나 참여 중인 글 | 필수 | 200 |
| POST | `/api/v1/ride/{id}/participant` | 모집글 참여 | 필수 | 200 |
| DELETE | `/api/v1/ride/{id}/participant/me` | 모집글 참여 취소 | 필수 | 204 |
| GET | `/api/v1/chats/rooms` | 내 채팅방 목록 | 필수 | 200 |
| GET | `/api/v1/chats/{rideId}/messages` | 채팅 메시지 조회 | 필수 | 200 |
| POST | `/api/v1/chats/{rideId}/messages` | 채팅 메시지 전송 | 필수 | 200 |
| POST | `/api/v1/chats/{rideId}/read` | 마지막 읽은 메시지 갱신 | 필수 | 204 |
| GET | `/ping` | 서버 상태 확인 | 없음 | 200 |
| GET | `/ready` | DB 연결 상태 확인 | 없음 | 200 |

## 3. 인증

### POST /api/v1/auth/oauth2/kakao

프론트엔드에서 카카오 로그인으로 받은 액세스 토큰을 혼디 JWT로 교환한다. 처음 로그인한 사용자는 카카오 닉네임으로 자동 가입되며 `USER` 권한이 부여된다.

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `accessToken` | string | 예 | 카카오 액세스 토큰. 빈 문자열·공백만 있는 문자열 불가 |

```json
{
  "accessToken": "<kakao-access-token>"
}
```

응답 `200 OK`:

```json
{
  "role": "USER",
  "accessToken": "<hondi-access-token>",
  "tokenType": "Bearer",
  "exprTime": 3600,
  "refreshToken": "<hondi-refresh-token>"
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `role` | string | `USER` 또는 `ADMIN` |
| `accessToken` | string | 혼디 API 인증용 JWT |
| `tokenType` | string | `Bearer` |
| `exprTime` | integer | 액세스 토큰 유효 기간, **초 단위**. 실제 값은 서버 설정에 따라 달라진다. |
| `refreshToken` | string | 혼디 토큰 재발급용 JWT |

서비스 오류: `400 INVALID_REQUEST`, `400 INVALID_TOKEN`, `500 KAKAO_REQUEST_ERROR`.

카카오 사용자 정보에 ID 또는 유효한 닉네임이 없을 때도 현재 구현은 `KAKAO_REQUEST_ERROR`를 반환한다.

### POST /api/v1/auth/token/refresh

혼디 리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 발급한다. Authorization 헤더는 필요하지 않다.

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `refreshToken` | string | 예 | 혼디 리프레시 토큰. 빈 문자열·공백만 있는 문자열 불가 |

```json
{
  "refreshToken": "<hondi-refresh-token>"
}
```

응답 `200 OK`:

```json
{
  "accessToken": "<hondi-access-token>",
  "tokenType": "Bearer",
  "exprTime": 3600,
  "refreshToken": "<hondi-refresh-token>"
}
```

필드 의미는 로그인 응답과 같으며, **`role` 필드는 없다.** `exprTime`의 예시 값은 고정 계약이 아니다. 재발급한 액세스 토큰에는 현재 DB의 사용자 권한을 반영한다.

잘못되거나 만료된 리프레시 토큰, 또는 액세스 토큰을 본문에 넣으면 `400 INVALID_TOKEN`이다. 현재 구현은 이전 리프레시 토큰을 재발급 시점에 폐기하지 않는다.

서비스 오류: `400 INVALID_REQUEST`, `400 INVALID_TOKEN`, `400 USER_NOT_EXIST`.

## 4. 사용자

### 프로필 응답 형식

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `id` | integer | 아니오 | 사용자 ID |
| `nickname` | string | 아니오 | 닉네임, 중복 허용 |
| `profileImage` | string | 예 | 프로필 이미지 URL |
| `name` | string | 예 | 저장된 이름. 현재 카카오 가입에서는 닉네임을 저장하며, 다른 사용자 조회 시 `null` |
| `role` | string | 아니오 | `USER` 또는 `ADMIN` |
| `createdAt` | string(date-time) | 아니오 | 가입 시각 |

```json
{
  "id": 1,
  "nickname": "제주여행자",
  "profileImage": null,
  "name": "제주여행자",
  "role": "USER",
  "createdAt": "2030-01-15T03:00:00Z"
}
```

### GET /api/v1/user/me

인증된 내 프로필을 조회한다. 쿼리와 본문은 없다.

응답: `200 OK`, 위 프로필 객체.

서비스 오류: `400 USER_NOT_EXIST`.

### GET /api/v1/user/{id}

사용자 ID로 프로필을 조회한다. 쿼리와 본문은 없다. 다른 사용자의 프로필도 조회할 수 있지만, 해당 응답의 `name`은 `null`이다.

응답: `200 OK`, 위 프로필 객체.

서비스 오류: `400 USER_NOT_EXIST`.

### PATCH /api/v1/user/{id}

`{id}`가 인증된 내 사용자 ID인 경우에만 수정할 수 있다.

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `nickname` | string | 아니오 | 전달 시 2~20자 |
| `profileImage` | string | 아니오 | 전달 시 최대 2,048자. URL 문자열을 저장하며 파일 업로드는 받지 않는다. |

```json
{
  "nickname": "혼디여행자",
  "profileImage": "https://example.com/profile.png"
}
```

- 생략 또는 `null`인 필드는 변경하지 않는다. 빈 객체 `{}`도 허용한다.
- 길이 검증을 통과한 공백 닉네임은 변경하지 않는다. 예를 들어 `"  "`는 변경 없음, `""`는 길이 검증 실패다.
- `profileImage`가 빈 문자열·공백·`null`이면 변경하지 않는다. 이 값들로 기존 이미지를 삭제할 수는 없다.
- `profileImage`의 URL 형식 자체를 검증하지는 않는다.

응답: `200 OK`, 수정된 프로필 객체.

서비스 오류: `400 INVALID_REQUEST`, `400 USER_NOT_EXIST`, `403 FORBIDDEN_USER_RESOURCE_ACCESS`.

### 간단한 사용자 객체

모집글의 `host`·`members`, 참여 응답의 `user`, 채팅의 `sender`는 다음 형식이다.

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `id` | integer | 아니오 | 사용자 ID |
| `nickname` | string | 아니오 | 닉네임 |
| `profileImage` | string | 예 | 프로필 이미지 URL |

```json
{
  "id": 1,
  "nickname": "제주여행자",
  "profileImage": null
}
```

## 5. 모집글

### 상세 응답 형식

생성·상세 조회·수정은 같은 응답 형식을 사용한다.

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `id` | integer | 아니오 | 모집글 ID |
| `host` | object | 아니오 | 간단한 사용자 객체, 방장 |
| `originName` | string | 아니오 | 출발지 이름 |
| `originLat` | number | 아니오 | 출발지 위도 |
| `originLon` | number | 아니오 | 출발지 경도 |
| `destName` | string | 아니오 | 목적지 이름 |
| `destLat` | number | 아니오 | 목적지 위도 |
| `destLon` | number | 아니오 | 목적지 경도 |
| `departureAt` | string(date-time) | 아니오 | 출발 예정 시각 |
| `capacity` | integer | 아니오 | 방장 포함 최대 인원 |
| `currentCount` | integer | 아니오 | 방장 포함 현재 인원 |
| `memo` | string | 예 | 합류 지점 등 메모 |
| `createdAt` | string(date-time) | 아니오 | 모집글 생성 시각 |
| `myStatus` | string | 아니오 | `HOST`, `JOINED`, `NONE` |
| `members` | object[] | 아니오 | 방장 제외 참여자. 각 항목은 간단한 사용자 객체이며 참여 기록 ID 오름차순 |

```json
{
  "id": 100,
  "host": {"id": 1, "nickname": "제주여행자", "profileImage": null},
  "originName": "제주국제공항",
  "originLat": 33.507,
  "originLon": 126.493,
  "destName": "성산일출봉",
  "destLat": 33.4581,
  "destLon": 126.9425,
  "departureAt": "2030-01-15T06:00:00Z",
  "capacity": 4,
  "currentCount": 1,
  "memo": "3번 게이트 앞",
  "createdAt": "2030-01-15T03:10:00Z",
  "myStatus": "HOST",
  "members": []
}
```

### POST /api/v1/ride

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `originName` | string | 예 | 공백만 있는 값 불가, 최대 100자 |
| `originLat` | number | 예 | 33.0 이상 34.1 이하 |
| `originLon` | number | 예 | 126.0 이상 127.1 이하 |
| `destName` | string | 예 | 공백만 있는 값 불가, 최대 100자 |
| `destLat` | number | 예 | 33.0 이상 34.1 이하 |
| `destLon` | number | 예 | 126.0 이상 127.1 이하 |
| `departureAt` | string(date-time) | 예 | 서버 검증 시점보다 미래 |
| `capacity` | integer | 예 | 2~6, 방장 포함 |
| `memo` | string | 아니오 | 최대 500자, `null` 허용 |

```json
{
  "originName": "제주국제공항",
  "originLat": 33.507,
  "originLon": 126.493,
  "destName": "성산일출봉",
  "destLat": 33.4581,
  "destLon": 126.9425,
  "departureAt": "2030-01-15T06:00:00Z",
  "capacity": 4,
  "memo": "3번 게이트 앞"
}
```

좌표의 서비스 지역 검사는 위도·경도 범위로 수행한다. 행정구역 경계나 실제 도로 경로를 검증하는 방식은 아니다. 좌표는 DB에 소수점 이하 6자리로 저장된다.

응답: `200 OK`, 상세 응답 객체. 생성 직후 `currentCount = 1`, `myStatus = HOST`, `members = []`이다.

서비스 오류: `400 INVALID_REQUEST`, `400 OUT_OF_SERVICE_AREA`, `400 USER_NOT_EXIST`.

### GET /api/v1/ride/{id}

모집글 상세 정보와 참여자를 조회한다. 로그인한 사용자는 참여 여부와 관계없이 조회할 수 있다. 정원이 찼거나 출발 시각이 지난 글도 존재하면 조회된다. 쿼리와 본문은 없다.

응답: `200 OK`, 상세 응답 객체. `myStatus`는 요청한 사용자 기준이다.

서비스 오류: `400 RIDE_NOT_EXIST`.

### PATCH /api/v1/ride/{id}

방장만 수정할 수 있다.

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `departureAt` | string(date-time) | 아니오 | 전달 시 서버 검증 시점보다 미래 |
| `capacity` | integer | 아니오 | 전달 시 2~6이며 현재 인원 이상 |
| `memo` | string | 아니오 | 최대 500자 |

```json
{
  "departureAt": "2030-01-15T06:30:00Z",
  "capacity": 3,
  "memo": "택시 승강장 2번 앞"
}
```

- 생략 또는 `null`인 필드는 변경하지 않는다. 빈 객체 `{}`도 허용한다.
- 메모를 비우려면 `"memo": ""`를 보낸다.
- 출발지·목적지 이름과 좌표는 수정 대상이 아니며, 전달해도 반영하지 않는다.
- 출발 시각이 지난 기존 글을 수정하는 행위 자체를 제한하지는 않는다. 새 출발 시각을 보낸다면 미래여야 한다.

응답: `200 OK`, 수정된 상세 응답 객체.

서비스 오류: `400 INVALID_REQUEST`, `400 RIDE_NOT_EXIST`, `400 INVALID_CAPACITY`, `403 NOT_RIDE_HOST`.

### DELETE /api/v1/ride/{id}

방장만 삭제할 수 있다. 쿼리와 본문은 없다. 모집글과 연결된 참여 기록·채팅 메시지·읽음 기록을 함께 삭제한다.

응답: `204 No Content`.

서비스 오류: `400 RIDE_NOT_EXIST`, `403 NOT_RIDE_HOST`. 이미 삭제된 글을 다시 삭제하면 `RIDE_NOT_EXIST`다.

## 6. 모집글 목록과 추천

### 목록 응답 형식

세 목록 API는 모두 다음 형식이다. 전체 개수나 전체 페이지 수는 반환하지 않는다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `rides` | object[] | 모집글 목록 항목 |
| `hasNext` | boolean | 의미는 각 API 설명 참고 |

모집글 목록 항목:

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `id` | integer | 아니오 | 모집글 ID |
| `host` | object | 아니오 | 간단한 사용자 객체 |
| `originName` | string | 아니오 | 출발지 이름 |
| `originLat` | number | 아니오 | 출발지 위도 |
| `originLon` | number | 아니오 | 출발지 경도 |
| `destName` | string | 아니오 | 목적지 이름 |
| `destLat` | number | 아니오 | 목적지 위도 |
| `destLon` | number | 아니오 | 목적지 경도 |
| `departureAt` | string(date-time) | 아니오 | 출발 예정 시각 |
| `capacity` | integer | 아니오 | 방장 포함 최대 인원 |
| `currentCount` | integer | 아니오 | 방장 포함 현재 인원 |
| `myStatus` | string | 아니오 | `HOST`, `JOINED`, `NONE` |
| `originDistanceKm` | number | 예 | 검색 출발지와 모집글 출발지 간 직선거리, km. 소수점 둘째 자리까지 반올림 |
| `destDistanceKm` | number | 예 | 검색 목적지와 모집글 목적지 간 직선거리, km. 소수점 둘째 자리까지 반올림 |
| `timeDiffMinutes` | integer | 예 | 희망 시각과 모집글 출발 시각의 차이, 분 단위 절댓값. 초 단위 차이는 잘림 |

목록 항목에는 상세 응답의 `memo`, `createdAt`, `members`가 없다. 거리와 시간차는 추천 API에서만 값이 있고 나머지 목록에서는 `null`이다.

일반 목록 응답 예시:

```json
{
  "rides": [
    {
      "id": 100,
      "host": {"id": 1, "nickname": "제주여행자", "profileImage": null},
      "originName": "제주국제공항",
      "originLat": 33.507,
      "originLon": 126.493,
      "destName": "성산일출봉",
      "destLat": 33.4581,
      "destLon": 126.9425,
      "departureAt": "2030-01-15T06:00:00Z",
      "capacity": 4,
      "currentCount": 2,
      "myStatus": "JOINED",
      "originDistanceKm": null,
      "destDistanceKm": null,
      "timeDiffMinutes": null
    }
  ],
  "hasNext": false
}
```

### GET /api/v1/rides

쿼리 파라미터:

| 이름 | 타입 | 필수 | 기본값 | 조건 |
| --- | --- | --- | --- | --- |
| `page` | integer | 아니오 | 0 | 0 이상, 첫 페이지는 0 |
| `size` | integer | 아니오 | 20 | 1~100 |

```http
GET /api/v1/rides?page=0&size=20
Authorization: Bearer <hondi-access-token>
```

- 현재 시각보다 출발 시각이 미래이고, `currentCount < capacity`인 글만 조회한다.
- 출발 시각 오름차순이다. 같은 출발 시각의 글 사이 정렬 순서는 별도로 정하지 않는다.
- 내 글과 참여 중인 글도 위 조건을 만족하면 포함되며, 각각 `HOST`·`JOINED`로 표시한다.
- `hasNext = true`면 `page + 1`로 다음 페이지를 요청한다.

응답: `200 OK`, 목록 응답 객체.

서비스 오류: `400 INVALID_PAGING_PARAMETER`.

### GET /api/v1/rides/match

쿼리 파라미터:

| 이름 | 타입 | 필수 | 기본값 | 조건 |
| --- | --- | --- | --- | --- |
| `originLat` | number | 예 | 없음 | 33.0~34.1 |
| `originLon` | number | 예 | 없음 | 126.0~127.1 |
| `destLat` | number | 예 | 없음 | 33.0~34.1 |
| `destLon` | number | 예 | 없음 | 126.0~127.1 |
| `departureAt` | string(date-time) | 아니오 | 서버 현재 시각 | 희망 출발 시각 |
| `size` | integer | 아니오 | 20 | 1~50 |

```http
GET /api/v1/rides/match?originLat=33.507&originLon=126.493&destLat=33.4581&destLon=126.9425&departureAt=2030-01-15T06%3A00%3A00Z&size=20
Authorization: Bearer <hondi-access-token>
```

추천 조건:

- 출발지 사이 직선거리 3km 이내, 목적지 사이 직선거리 5km 이내.
- 출발 시각은 희망 시각 전후 60분 범위이며, 이미 출발한 글은 제외.
- 정원이 찬 글, 내가 작성한 글, 내가 이미 참여 중인 글은 제외.
- 희망 시각과 출발 시각의 차이가 작은 순서로 정렬한다. 별도 점수 필드는 없다.
- 반환된 항목의 `myStatus`는 `NONE`이다.
- 참여를 취소한 글은 다시 추천 대상이 될 수 있다.

`hasNext`는 조건에 맞는 결과가 `size`보다 많다는 뜻이다. **이 API에는 `page`나 다음 커서가 없다.** 더 많은 결과가 필요하면 `size`를 최대 50까지 늘려 다시 요청한다.

쿼리 문자열은 `URLSearchParams` 등으로 인코딩한다. 특히 시간대의 `+09:00`을 직접 문자열로 붙일 때는 `+`를 `%2B`로 인코딩해야 한다.

응답: `200 OK`, 거리·시간차 값이 채워진 목록 응답 객체. 조건에 맞는 글이 없으면 `{"rides": [], "hasNext": false}`다.

서비스 오류: `400 INVALID_REQUEST`, `400 OUT_OF_SERVICE_AREA`, `400 INVALID_PAGING_PARAMETER`, `400 USER_NOT_EXIST`.

### GET /api/v1/rides/me

쿼리와 본문은 없다.

- 내가 작성한 글과 현재 참여 중인 글을 합쳐 반환한다.
- 출발 시각 내림차순이다.
- 이미 출발했거나 정원이 찬 글도 포함한다.
- 참여를 취소한 글은 참여 목록에서 빠진다. 과거 참여 이력을 별도로 반환하지 않는다.
- 페이지네이션이 없으며 `hasNext`는 항상 `false`다.

응답: `200 OK`, 목록 응답 객체. 작성한 글의 `myStatus`는 `HOST`, 참여 중인 글은 `JOINED`다.

## 7. 모집글 참여

### POST /api/v1/ride/{id}/participant

본문과 쿼리는 없다. 방장 수락 절차 없이 인증된 사용자가 즉시 참여한다. 요청 한 번이 한 명의 참여를 뜻하며, 방장은 참여 기록을 따로 만들지 않는다.

참여 조건:

- 내가 작성한 글이 아니어야 한다.
- 이미 참여 중이면 안 된다.
- 출발 시각이 현재보다 미래여야 한다.
- 현재 인원이 정원보다 적어야 한다.

응답 `200 OK`:

```json
{
  "id": 500,
  "rideId": 100,
  "user": {"id": 2, "nickname": "혼디여행자", "profileImage": null},
  "createdAt": "2030-01-15T03:20:00Z"
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | integer | **참여 기록 ID**. 사용자 ID나 모집글 ID와 다르다. |
| `rideId` | integer | 모집글 ID |
| `user` | object | 참여한 사용자의 간단한 사용자 객체 |
| `createdAt` | string(date-time) | 참여 시각 |

이 응답에는 최신 `currentCount`가 없다. 화면에서 필요하면 모집글 상세를 다시 조회한다.

서비스 오류: `400 RIDE_NOT_EXIST`, `400 CANNOT_JOIN_OWN_RIDE`, `400 RIDE_ALREADY_JOINED`, `400 RIDE_NOT_RECRUITING`, `400 RIDE_FULL`, `400 USER_NOT_EXIST`.

### DELETE /api/v1/ride/{id}/participant/me

본문과 쿼리는 없다. 내 참여 기록을 삭제하고 현재 인원을 한 명 줄인다.

- 현재 참여자만 취소할 수 있다. 방장이 호출하면 참여 기록이 없으므로 `PARTICIPANT_NOT_EXIST`다.
- 이미 출발한 글에서도 현재 구현은 참여 취소를 허용한다.
- 취소하면 해당 채팅방 조회·전송·읽음 처리를 할 수 없다.
- 조건을 만족하면 다시 참여할 수 있다.
- 참여 취소 시 기존 채팅 메시지와 읽음 기록은 삭제하지 않는다.

응답: `204 No Content`.

서비스 오류: `400 RIDE_NOT_EXIST`, `400 PARTICIPANT_NOT_EXIST`. 반복 취소는 성공으로 처리하지 않는다.

## 8. 채팅

모집글 한 개가 채팅방 한 개다. **채팅방 ID로 모집글 ID를 사용한다.** 방장 또는 현재 참여자만 메시지 조회·전송·읽음 처리를 할 수 있다. 채팅은 HTTP 폴링 방식이다.

출발 시각이나 정원 충족 여부로 채팅을 막지 않는다. 새 참여자는 참여 전 메시지도 조회할 수 있다. 메시지를 조회하거나 전송하는 것만으로 읽음 위치가 자동 갱신되지는 않는다.

### GET /api/v1/chats/rooms

내가 방장이거나 참여 중인 채팅방 목록을 조회한다. 쿼리와 본문은 없다.

- 마지막 메시지 시각 내림차순, 메시지가 없는 방은 뒤에 배치한다.
- 정원이 찼거나 출발 시각이 지난 방도 포함한다.
- 페이지네이션은 없다.

응답 `200 OK`:

```json
{
  "rooms": [
    {
      "rideId": 100,
      "originName": "제주국제공항",
      "destName": "성산일출봉",
      "departureAt": "2030-01-15T06:00:00Z",
      "lastMessage": "택시 승강장에서 만나요",
      "lastMessageAt": "2030-01-15T03:30:00Z",
      "unreadCount": 2
    }
  ]
}
```

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `rooms` | object[] | 아니오 | 채팅방 목록 |
| `rooms[].rideId` | integer | 아니오 | 모집글 ID, 채팅 API에 사용할 ID |
| `rooms[].originName` | string | 아니오 | 출발지 이름 |
| `rooms[].destName` | string | 아니오 | 목적지 이름 |
| `rooms[].departureAt` | string(date-time) | 아니오 | 출발 예정 시각 |
| `rooms[].lastMessage` | string | 예 | 마지막 메시지 내용. 메시지가 없으면 `null` |
| `rooms[].lastMessageAt` | string(date-time) | 예 | 마지막 메시지 시각. 메시지가 없으면 `null` |
| `rooms[].unreadCount` | integer | 아니오 | 저장된 읽음 위치보다 ID가 큰 메시지 개수 |

현재 `unreadCount` 계산에는 **내가 보낸 메시지도 포함**된다. 읽음 기록이 없으면 해당 방의 모든 메시지를 센다. 읽음 처리는 별도 API로 보낸다.

### 메시지 객체

| 필드 | 타입 | null 가능 | 설명 |
| --- | --- | --- | --- |
| `id` | integer | 아니오 | 메시지 ID, 조회·읽음 커서로 사용 |
| `sender` | object | 아니오 | 보낸 사용자의 간단한 사용자 객체 |
| `content` | string | 아니오 | 메시지 내용 |
| `createdAt` | string(date-time) | 아니오 | 생성 시각 |

### GET /api/v1/chats/{rideId}/messages

쿼리 파라미터:

| 이름 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `afterId` | integer | 아니오 | 없음 | 이 ID보다 큰 새 메시지 조회 |
| `beforeId` | integer | 아니오 | 없음 | 이 ID보다 작은 과거 메시지 조회 |
| `size` | integer | 아니오 | 50 | 1~100 |

| 조회 방식 | 요청 예시 | 반환 범위 | `hasNext = true`의 의미 |
| --- | --- | --- | --- |
| 최초 조회 | `/api/v1/chats/100/messages?size=50` | 가장 최근 50개 | 더 오래된 메시지가 있음 |
| 새 메시지 조회 | `/api/v1/chats/100/messages?afterId=900&size=50` | 900보다 큰 ID 중 작은 순서로 최대 50개 | 해당 커서 이후에 아직 가져오지 않은 새 메시지가 더 있음 |
| 과거 조회 | `/api/v1/chats/100/messages?beforeId=850&size=50` | 850보다 작은 ID 중 가장 최근 50개 | 더 오래된 메시지가 있음 |

- **응답 배열은 모든 방식에서 메시지 ID 오름차순**이다.
- `afterId`와 `beforeId`를 동시에 보내면 현재 구현은 `afterId`를 우선하고 `beforeId`를 무시한다. 의도한 방식의 커서 하나만 보낸다.
- 조회 커서는 기준 숫자로만 사용한다. 실제 존재하는 메시지 ID인지 검증하지 않는다.
- 처음부터 새 메시지를 조회하려면 `afterId=0`을 사용할 수 있다.

응답 `200 OK`:

```json
{
  "messages": [
    {
      "id": 900,
      "sender": {"id": 1, "nickname": "제주여행자", "profileImage": null},
      "content": "택시 승강장에서 만나요",
      "createdAt": "2030-01-15T03:30:00Z"
    },
    {
      "id": 901,
      "sender": {"id": 2, "nickname": "혼디여행자", "profileImage": null},
      "content": "네, 곧 도착해요",
      "createdAt": "2030-01-15T03:31:00Z"
    }
  ],
  "hasNext": false
}
```

`messages`는 메시지 객체 배열, `hasNext`는 위 표의 조회 방향에 따른 추가 결과 여부다. 결과가 없으면 `{"messages": [], "hasNext": false}`다.

프론트엔드 커서 처리:

1. 최초 조회 결과의 마지막 ID를 새 메시지 조회용 `afterId`로 보관한다. 결과가 비어 있으면 0을 사용한다.
2. 폴링 응답이 비어 있지 않으면 마지막 ID로 `afterId`를 갱신한다. `hasNext = true`면 다음 묶음도 이어서 조회한다.
3. 과거 메시지를 불러올 때는 화면에 보관한 가장 작은 ID를 `beforeId`로 보낸다. 새 메시지용 커서를 과거 커서로 덮어쓰지 않는다.
4. 읽음 처리는 사용자가 실제 확인한 마지막 메시지 ID로 별도 요청한다.

서비스 오류: `400 INVALID_PAGING_PARAMETER`, `400 RIDE_NOT_EXIST`, `403 NOT_RIDE_MEMBER`.

### POST /api/v1/chats/{rideId}/messages

요청 본문:

| 필드 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- |
| `content` | string | 예 | 빈 문자열·공백만 있는 값 불가, 최대 1,000자 |

```json
{
  "content": "택시 승강장에서 만나요"
}
```

응답 `200 OK`:

```json
{
  "id": 900,
  "sender": {"id": 1, "nickname": "제주여행자", "profileImage": null},
  "content": "택시 승강장에서 만나요",
  "createdAt": "2030-01-15T03:30:00Z"
}
```

서비스 오류: `400 INVALID_REQUEST`, `400 RIDE_NOT_EXIST`, `400 USER_NOT_EXIST`, `403 NOT_RIDE_MEMBER`.

### POST /api/v1/chats/{rideId}/read

**JSON 본문 대신 쿼리 파라미터**로 마지막 읽은 메시지 ID를 전달한다.

| 이름 | 위치 | 타입 | 필수 | 조건 |
| --- | --- | --- | --- | --- |
| `rideId` | path | integer | 예 | 모집글 ID |
| `lastMessageId` | query | integer | 예 | 해당 방에 실제로 존재하는 메시지 ID |

```http
POST /api/v1/chats/100/read?lastMessageId=901
Authorization: Bearer <hondi-access-token>
```

- 해당 메시지까지 읽은 것으로 기록한다.
- 이미 저장한 ID보다 작은 값이나 같은 값을 보내도 읽은 위치가 되돌아가지 않는다.
- 다른 방의 메시지 ID 또는 존재하지 않는 ID는 `CHAT_MESSAGE_NOT_EXIST`다.

응답: `204 No Content`.

서비스 오류: `400 RIDE_NOT_EXIST`, `400 CHAT_MESSAGE_NOT_EXIST`, `403 NOT_RIDE_MEMBER`.

## 9. 상태 확인

### GET /ping

인증, 본문, 쿼리 없이 호출한다. 서버 프로세스가 요청을 처리하는지 확인한다.

응답: `200 OK`, `text/plain` 본문 `pong`.

### GET /ready

인증, 본문, 쿼리 없이 호출한다. DB에 확인 쿼리를 실행한다.

응답: `200 OK`, `text/plain` 본문 `ready`.

DB 확인 실패: `503 SERVICE_UNAVAILABLE`, 아래 공통 오류 객체.

## 10. 오류 응답

### 서비스 오류 형식

애플리케이션에서 처리한 서비스 오류와 요청 DTO의 Bean Validation 실패는 다음 형식이다.

```json
{
  "timestamp": "2030-01-15T03:40:00Z",
  "httpStatus": "BAD_REQUEST",
  "code": "RIDE_FULL",
  "message": "모집 인원이 다 찼습니다."
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `timestamp` | string(date-time) | 오류 발생 시각 |
| `httpStatus` | string | HTTP 상태 enum 이름. 숫자 `400`이 아니라 `BAD_REQUEST` 등 |
| `code` | string | 애플리케이션 오류 코드 |
| `message` | string | 오류 설명 |

없는 사용자·모집글·메시지에 대한 서비스 오류는 현재 **404가 아니라 400**이다. DTO 검증 오류는 필드별 상세 목록 없이 `INVALID_REQUEST`로 반환한다.

### 형식이 다른 오류

- 인증/인가 필터의 `401`, `403`은 `sendError`로 처리하므로 위 오류 객체가 보장되지 않는다. `code`가 없어도 HTTP 상태로 처리한다.
- 필수 쿼리 파라미터 누락, 숫자·날짜 변환 실패, 잘못된 JSON 등은 Spring MVC 기본 오류 응답이 될 수 있다. 모든 `400`에 `INVALID_REQUEST`가 있다고 가정하지 않는다.
- 지원하지 않는 HTTP 메서드나 Content-Type 역시 프레임워크 오류 응답을 사용할 수 있다.

### 오류 코드 목록

| HTTP 상태 | 코드 | 메시지 |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 요청 정보가 잘못되어 있습니다. |
| 400 | `INVALID_TOKEN` | 잘못된 토큰입니다. |
| 400 | `INVALID_ENUM_VALUE` | 올바르지 않은 Enum입니다. |
| 400 | `USER_NOT_EXIST` | 유저가 존재하지 않습니다. |
| 400 | `INVALID_PAGING_PARAMETER` | 페이지네이션 파라미터가 잘못되어있습니다. |
| 400 | `OUT_OF_SERVICE_AREA` | 서비스 지역(제주도)을 벗어난 위치입니다. |
| 400 | `RIDE_NOT_EXIST` | 모집글이 존재하지 않습니다. |
| 400 | `RIDE_NOT_RECRUITING` | 모집 중인 글이 아닙니다. |
| 400 | `RIDE_FULL` | 모집 인원이 다 찼습니다. |
| 400 | `INVALID_CAPACITY` | 현재 인원보다 적게 최대 인원을 설정할 수 없습니다. |
| 400 | `CANNOT_JOIN_OWN_RIDE` | 자신의 모집글에는 참여할 수 없습니다. |
| 400 | `RIDE_ALREADY_JOINED` | 이미 참여 중입니다. |
| 400 | `PARTICIPANT_NOT_EXIST` | 참여 중인 모집글이 아닙니다. |
| 400 | `CHAT_MESSAGE_NOT_EXIST` | 존재하지 않는 메시지입니다. |
| 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 403 | `ACCESS_DENIED` | 접근 권한이 없습니다. |
| 403 | `FORBIDDEN_USER_RESOURCE_ACCESS` | 해당 정보에 접근할 수 없습니다. |
| 403 | `NOT_RIDE_HOST` | 모집글 작성자만 할 수 있습니다. |
| 403 | `NOT_RIDE_MEMBER` | 참여 중인 모집글의 채팅만 이용할 수 있습니다. |
| 500 | `INTERNAL_SERVER_ERROR` | 서버에서 에러가 발생했습니다. |
| 500 | `KAKAO_REQUEST_ERROR` | 카카오와 통신 중 오류가 발생했습니다. |
| 503 | `SERVICE_UNAVAILABLE` | 서비스를 사용할 수 없습니다. |

이 표는 `ExceptionCode`에 정의된 전체 목록이다. `UNAUTHORIZED`, `ACCESS_DENIED`는 정의되어 있지만 현재 보안 필터가 이 코드의 JSON을 직접 반환하지 않는다. `INVALID_ENUM_VALUE`는 공통 유틸리티용이며 위 API의 요청 필드에는 enum 입력이 없다. 예상하지 못한 서버 예외는 `500 INTERNAL_SERVER_ERROR`로 처리된다.

## 11. 연동 순서 예시

1. 카카오 로그인 후 `POST /api/v1/auth/oauth2/kakao`에 카카오 액세스 토큰을 보낸다.
2. 혼디 액세스 토큰을 Authorization 헤더에 넣고 `GET /api/v1/user/me`로 내 ID를 조회한다.
3. `GET /api/v1/rides/match`로 후보를 찾거나 `POST /api/v1/ride`로 모집글을 만든다.
4. 다른 사람의 글에 합류하려면 `POST /api/v1/ride/{id}/participant`를 호출한다.
5. `GET /api/v1/chats/rooms`와 메시지 조회 API로 채팅방·메시지를 표시한다.
6. 메시지 전송과 `afterId` 폴링을 사용하고, 확인한 메시지는 읽음 API로 처리한다.
7. 액세스 토큰 만료 시 `POST /api/v1/auth/token/refresh` 응답의 토큰을 저장하고 원래 요청을 재시도한다. `401`만으로 만료와 다른 인증 실패를 구분할 수는 없으며, 재발급도 실패하면 다시 로그인한다.

## 12. 구현 확인 위치

아래 경로는 이 문서 위치를 기준으로 한 상대 링크다.

- [카카오 로그인 컨트롤러](../src/main/java/com/seohamin/hondi/domain/auth/oauth2/controller/Oauth2Controller.java)
- [토큰 컨트롤러](../src/main/java/com/seohamin/hondi/domain/auth/token/controller/TokenController.java)
- [사용자 컨트롤러](../src/main/java/com/seohamin/hondi/domain/user/controller/UserController.java)
- [모집글 컨트롤러](../src/main/java/com/seohamin/hondi/domain/ride/controller/RideController.java)
- [목록 컨트롤러](../src/main/java/com/seohamin/hondi/domain/ride/controller/list/RideListController.java)
- [참여 컨트롤러](../src/main/java/com/seohamin/hondi/domain/ride/controller/participant/RideParticipantController.java)
- [채팅 컨트롤러](../src/main/java/com/seohamin/hondi/domain/chat/controller/ChatController.java)
- [상태 확인 컨트롤러](../src/main/java/com/seohamin/hondi/global/health/HealthController.java)
- [인증·인가 설정](../src/main/java/com/seohamin/hondi/global/config/SecurityConfig.java)
- [오류 코드](../src/main/java/com/seohamin/hondi/global/exception/constants/ExceptionCode.java)
- [오류 응답 처리](../src/main/java/com/seohamin/hondi/global/exception/handler/CustomizedResponseEntityExceptionHandler.java)
