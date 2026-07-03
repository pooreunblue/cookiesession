# Cookie / Session Step Guide

이 문서는 다음 파일들을 기준으로 정리한 학습 노트입니다.

- [`src/main/java/com/example/cookiesession/step1/CookieServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step1/CookieServlet.java)
- [`src/main/java/com/example/cookiesession/step2/DTO.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step2/DTO.java)
- [`src/main/java/com/example/cookiesession/step2/SessionServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step2/SessionServlet.java)
- [`src/main/webapp/WEB-INF/views/step01/cookie.jsp`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/webapp/WEB-INF/views/step01/cookie.jsp)
- [`src/main/webapp/WEB-INF/views/step02/session.jsp`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/webapp/WEB-INF/views/step02/session.jsp)

## 1. 전체 흐름

- `step1`은 쿠키를 읽고, 새 쿠키를 응답에 실어 보내는 예제다.
- `step2`는 세션을 만들고, 세션에 값을 저장한 뒤 JSP로 넘기는 예제다.
- JSP는 단순 출력만 담당하고, 실제 데이터 준비는 서블릿이 한다.

---

## 2. Step1 - Cookie

### 2-1. 초심자를 위한 비유

쿠키는 `방문자 팔찌`에 가깝다.

- 서버는 방문자에게 팔찌를 하나 채워준다.
- 다음에 다시 오면, 브라우저가 그 팔찌를 들고 와서 “나는 전에 왔던 사람”이라고 알려준다.
- 그래서 로그인 유지, 환경 설정 저장, 장바구니 같은 가벼운 상태 보관에 자주 쓴다.

다만 팔찌는 `내 손목에 붙어 있는 것`이라서, 사용자가 직접 볼 수도 있고 수정할 수도 있다. 그래서 민감한 정보는 넣지 않는다.

### 2-2. 능숙자를 위한 절차 설명

`CookieServlet#doGet()` 기준 흐름은 다음과 같다.

1. `req.getCookies()`로 요청에 포함된 쿠키 배열을 읽는다.
2. `readCookies()`에서 각 쿠키의 이름, 값, 경로, 도메인, 만료, 보안 속성을 확인한다.
3. `new Cookie("firstCookie", "Hello_Cookie")`로 단순 쿠키를 만든다.
4. `makeCookie()`로 보안/정책이 포함된 쿠키를 만든다.
5. `resp.addCookie(...)`로 응답 헤더에 쿠키를 실어 보낸다.
6. `forward()`로 `/WEB-INF/views/step01/cookie.jsp`를 렌더링한다.

핵심 포인트는 다음과 같다.

- `path`
  - 쿠키가 어떤 경로에서 전송될지 제한한다.
  - 기본값은 `/` 성격으로 넓게 동작한다.
- `maxAge`
  - 쿠키의 생명주기를 정한다.
  - `-1`은 세션 쿠키, `0`은 삭제, 양수는 초 단위 유지다.
- `HttpOnly`
  - `document.cookie`에서 접근을 막는다.
  - XSS 대응에 유리하다.
- `Secure`
  - HTTPS에서만 전송되도록 제한한다.
- `SameSite`
  - 교차 사이트 요청에서 쿠키가 어느 정도까지 전송될지 제어한다.
  - CSRF 완화에 중요하다.

### 2-3. 코드에서 눈여겨볼 점

- `req.getCookies()`는 요청에 쿠키가 없으면 `null`일 수 있다.
- `readCookies()`는 디버깅용으로 쿠키 상태를 확인하는 용도다.
- JSP에서는 `${cookie.get("firstCookie").value}`처럼 EL로 쿠키에 접근한다.
- JSP는 데이터를 생성하지 않고 표현만 한다.

### 2-4. 취업 대비 면접 예상 문항

1. 쿠키와 세션의 차이를 설명해보세요.
2. 쿠키를 사용할 때 `HttpOnly`, `Secure`, `SameSite`를 왜 설정하나요?
3. `maxAge = -1`, `0`, 양수의 차이는 무엇인가요?
4. `path`를 제한하면 어떤 효과가 있나요?
5. 쿠키에 로그인 비밀번호를 넣으면 왜 위험한가요?
6. `request.getCookies()`가 `null`일 수 있는 상황은 언제인가요?
7. `forward`와 `redirect`는 쿠키 처리 관점에서 어떤 차이가 있나요?

---

## 3. Step2 - Session

### 3-1. 초심자를 위한 비유

세션은 `가게의 고객 대장`에 가깝다.

- 손님에게는 번호표만 준다.
- 실제 기록은 가게 안쪽 장부에 적는다.
- 손님은 번호표만 보여주면 되고, 장부 내용은 외부에서 직접 보지 못한다.

즉, 쿠키가 “손에 들고 다니는 메모”라면, 세션은 “서버 안 금고에 보관한 메모”다.

### 3-2. 능숙자를 위한 절차 설명

`SessionServlet#doGet()` 기준 흐름은 다음과 같다.

1. `req.getSession()`으로 세션을 가져온다.
2. 세션이 없으면 생성하고, 있으면 기존 세션을 반환한다.
3. `session.getId()`로 세션 식별자를 확인한다.
4. `setAttribute("data", "my-data")`로 문자열 값을 저장한다.
5. `setAttribute("dto", new DTO("my-dto", 1))`로 객체를 저장한다.
6. `forward()`로 `/WEB-INF/views/step02/session.jsp`를 렌더링한다.

핵심 포인트는 다음과 같다.

- `getSession()`
  - 기본적으로 세션이 없으면 새로 만든다.
- `getSession(false)`
  - 세션이 없으면 `null`을 받을 수 있다.
  - 존재 여부만 확인하고 싶을 때 유용하다.
- `invalidate()`
  - 세션을 명시적으로 만료시킨다.
- `setAttribute / getAttribute`
  - 세션에 객체를 저장하고 꺼내는 표준 방식이다.
- `Object` 캐스팅
  - `getAttribute()`는 `Object`를 반환하므로 필요하면 다운캐스팅이 필요하다.

### 3-3. DTO 역할

`DTO`는 세션에 넣을 데이터를 묶어두는 용도다.

- `record DTO(String name, int count)`로 불변 데이터 구조를 만든다.
- `getName()`, `getCount()`를 직접 제공해서 JSP나 다른 코드에서 접근하기 쉽게 한다.
- 세션에는 단순 문자열뿐 아니라 이런 객체도 저장할 수 있다.

### 3-4. 코드에서 눈여겨볼 점

- 세션은 브라우저 쿠키의 `JSESSIONID`와 연결되는 경우가 일반적이다.
- 실무에서는 세션에 민감한 비즈니스 상태를 무작정 많이 넣지 않는다.
- 세션에는 최소 정보만 넣고, 나머지는 DB나 캐시로 분리하는 설계가 더 안전하다.
- JSP `session.jsp`는 현재는 표시용 뼈대이고, 실제로는 EL로 세션 데이터를 출력하는 쪽으로 확장할 수 있다.

### 3-5. 취업 대비 면접 예상 문항

1. 세션이 필요한 이유는 무엇인가요?
2. `JSESSIONID`는 어떤 역할을 하나요?
3. `getSession()`과 `getSession(false)`의 차이는 무엇인가요?
4. 세션에 객체를 저장할 때 주의할 점은 무엇인가요?
5. 세션 만료는 어떻게 처리하나요?
6. 쿠키 기반 인증과 세션 기반 인증의 장단점은 무엇인가요?
7. 세션에 데이터를 많이 넣으면 어떤 문제가 생기나요?
8. 세션을 서버 메모리에 둘 때와 Redis 같은 외부 저장소에 둘 때 차이는 무엇인가요?

---

## 4. Cookie vs Session 한 번에 정리

- 쿠키는 클라이언트에 저장된다.
- 세션은 서버에 저장된다.
- 쿠키는 가볍고 단순하지만 노출 위험이 있다.
- 세션은 비교적 안전하지만 서버 자원을 사용한다.
- 일반적으로 쿠키는 `식별자`나 `환경값`에, 세션은 `상태 정보`에 사용한다.

---

## 5. 면접에서 자주 나오는 답변 뼈대

쿠키와 세션의 차이를 설명할 때는 아래 순서로 말하면 정리된다.

1. 저장 위치
2. 보안성
3. 용량과 생명주기
4. 실제 사용 사례
5. 보안 옵션과 만료 정책

이 순서로 답하면 단순 암기보다 구조적으로 설명하는 인상을 줄 수 있다.
