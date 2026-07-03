# Step3 Auth / Access Control Guide

이 문서는 다음 파일들을 기준으로 정리한 학습 노트입니다.

- [`src/main/java/com/example/cookiesession/step3/AuthServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step3/AuthServlet.java)
- [`src/main/java/com/example/cookiesession/step3/FreeServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step3/FreeServlet.java)
- [`src/main/java/com/example/cookiesession/step3/PremiumServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step3/PremiumServlet.java)
- [`src/main/webapp/WEB-INF/views/step03/auth.jsp`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/webapp/WEB-INF/views/step03/auth.jsp)

## 1. 전체 흐름

- `/auth`에서 로그인 폼을 보여준다.
- `POST /auth`에서 아이디와 비밀번호를 검사한다.
- 로그인 성공 시 세션에 사용자 정보와 등급을 저장한다.
- `/free`는 로그인만 되어 있으면 접근 가능하다.
- `/premium`은 로그인뿐 아니라 `premium` 등급이어야 접근 가능하다.
- JSP는 세션에 저장된 `user`, `msg`를 화면에 보여준다.

---

## 2. 초심자를 위한 비유

이 단계는 `회원권 팔찌 + 등급표` 구조로 이해하면 쉽다.

- `auth`는 헬스장 접수대다.
- 로그인하면 손목에 회원권 팔찌를 채워준다. 이게 세션의 `user`다.
- `free`는 일반 회원이면 들어갈 수 있는 라커룸이다.
- `premium`은 VIP 전용 라운지다.
- `grade`는 팔찌에 붙어 있는 등급 스티커다.
- `msg`는 접수대 직원이 “지금 상황은 이렇습니다”라고 메모해 둔 안내판이다.

즉, 로그인은 단순히 이름을 확인하는 행위가 아니라, 이후 화면과 권한 판정을 위해 서버가 기억할 상태를 만드는 과정이다.

---

## 3. 능숙자를 위한 절차 설명

### 3-1. 인증 흐름

`AuthServlet#doGet()`

1. `auth.jsp`를 forward로 보여준다.
2. 폼에서 `username`, `password`를 `POST`로 전송한다.

`AuthServlet#doPost()`

1. `req.getParameter("username")`, `req.getParameter("password")`로 입력값을 읽는다.
2. `admin / 1234` 조합이면 세션을 만든다.
3. 세션에 `user`, `grade=free`, `msg=Login success (free)`를 저장한다.
4. `admin2 / 1234` 조합이면 세션에 `user`, `grade=premium`, `msg=Login success (premium)`를 저장한다.
5. `resp.sendRedirect("/auth")`로 다시 인증 화면으로 돌려보낸다.

### 3-2. 권한 제어 흐름

`FreeServlet#doGet()`

1. `req.getSession()`으로 세션을 가져온다.
2. `session.getAttribute("user") == null`이면 로그인되지 않은 상태로 판단한다.
3. 로그인되지 않았으면 `/auth`로 redirect한다.
4. 로그인되어 있으면 응답 본문에 “유저면 무료로 사용 가능한 페이지”를 출력한다.

`PremiumServlet#doGet()`

1. `req.getSession()`으로 세션을 가져온다.
2. `user`가 없으면 로그인 필요 상태로 판단하고 `msg`를 세션에 넣은 뒤 `/auth`로 redirect한다.
3. `user`가 있어도 `grade`가 `premium`이 아니면 등급 부족으로 판단하고 `msg`를 세션에 넣은 뒤 `/auth`로 redirect한다.
4. 조건을 만족하면 “프리미엄 유저만 사용 가능한 페이지”를 출력한다.

### 3-3. JSP 렌더링

`auth.jsp`

- `<form method="post">`로 로그인 값을 보낸다.
- `${user}`와 `${msg}`를 화면에 보여준다.
- 여기서 `${user}`와 `${msg}`는 세션 스코프 또는 요청 스코프의 값으로 해석될 수 있다.

### 3-4. 설계 포인트

- `forward`와 `redirect`의 역할을 구분해야 한다.
  - `forward`는 서버 내부 이동이고, 요청 정보가 유지된다.
  - `redirect`는 브라우저에 새 요청을 보내게 한다.
- 로그인 성공 후 `redirect`를 쓰는 이유는 보통 PRG 패턴에 가깝게 동작시키기 위함이다.
- 권한 체크는 컨트롤러 단계에서 먼저 하고, 성공한 경우에만 본문을 출력하는 편이 안전하다.
- `session.getAttribute("grade").equals("premium")`는 `grade`가 `null`일 경우 NPE 위험이 있으므로, 실무에서는 `Objects.equals(...)`나 null 방어를 고려한다.

---

## 4. 면접 대비 포인트

### 4-1. 인증과 인가

- 인증(Authentication)은 “누구인지 확인하는 것”이다.
- 인가(Authorization)는 “무엇을 할 수 있는지 결정하는 것”이다.
- 이 코드는 `AuthServlet`에서 인증 비슷한 과정을 하고, `FreeServlet`과 `PremiumServlet`에서 인가를 판정한다.

### 4-2. 세션 활용

- 로그인 성공 시 세션에 사용자 상태를 저장한다.
- 이후 요청에서는 세션 값을 확인해서 접근을 통제한다.
- 브라우저가 다시 로그인하지 않아도 상태를 이어갈 수 있다.

### 4-3. 리다이렉트 사용 이유

- 권한이 없을 때 다른 페이지로 보내기 위해 `sendRedirect()`를 사용한다.
- redirect는 주소가 바뀌고 새 요청이 발생한다.
- 로그인 실패나 권한 부족 메시지는 세션에 저장해 다음 화면에서 읽게 할 수 있다.

---

## 5. 취업 대비 면접 예상 문항

1. 인증과 인가의 차이를 설명해보세요.
2. `AuthServlet`에서 로그인 성공 시 세션에 값을 저장하는 이유는 무엇인가요?
3. 왜 로그인 후 `forward`가 아니라 `redirect`를 사용했나요?
4. `/free`와 `/premium`의 권한 체크 방식 차이는 무엇인가요?
5. 세션에 `user`, `grade`, `msg`를 저장하는 설계의 장점과 단점은 무엇인가요?
6. `session.getAttribute("grade").equals("premium")`에서 발생할 수 있는 문제는 무엇인가요?
7. 로그인 실패 메시지를 세션에 저장하는 이유는 무엇인가요?
8. 이 구조를 Spring Security로 바꾸면 어떤 부분이 달라지나요?
9. 권한 체크 로직을 서블릿마다 중복하지 않으려면 어떻게 리팩터링할 수 있나요?
10. 세션 기반 인증의 보안상 주의점은 무엇인가요?

---

## 6. 한 줄 정리

- `auth`는 로그인 상태를 만든다.
- `free`는 로그인만 확인한다.
- `premium`은 로그인과 등급까지 확인한다.
- 이 구조는 인증과 인가를 분리해서 이해하기 좋은 예제다.
