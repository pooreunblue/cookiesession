# Step4 Filter Guide

이 문서는 다음 파일들을 기준으로 정리한 학습 노트입니다.

- [`src/main/java/com/example/cookiesession/step4/AuthFilter.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step4/AuthFilter.java)
- [`src/main/java/com/example/cookiesession/step4/EncodingFilter.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step4/EncodingFilter.java)
- [`src/main/java/com/example/cookiesession/step4/FilterServlet.java`](/Users/baegseungho/IdeaProjects/cookiesession/src/main/java/com/example/cookiesession/step4/FilterServlet.java)

## 1. 전체 흐름

- `EncodingFilter`는 모든 요청 전에 인코딩과 응답 타입을 정리한다.
- `AuthFilter`는 `/filter/*` 요청에서 로그인 여부와 권한을 검사한다.
- `FilterServlet`은 실제로 화면에 출력할 본문을 담당한다.
- 필터는 서블릿보다 먼저 실행되며, 필요하면 요청을 막고 다른 곳으로 보낼 수 있다.

---

## 2. 초심자를 위한 비유

필터는 `건물 입구의 보안 요원`과 같다.

- `EncodingFilter`는 입장 전에 신분증을 읽기 편하게 정리하는 안내 직원이다.
- `AuthFilter`는 출입 명단을 확인하는 보안 요원이다.
- `FilterServlet`은 건물 안에서 실제 업무를 보는 사무실이다.

즉, 손님이 사무실에 도착하기 전에 먼저 입구에서 검사하고 준비시키는 장치가 필터다.

이렇게 생각하면 이해하기 쉽다.

- 인코딩 필터: 말이 안 깨지도록 입장 전에 통역 세팅
- 인증 필터: 들어와도 되는 사람인지 확인
- 인가 필터: 들어올 수 있어도 특정 방까지 갈 수 있는지 확인

---

## 3. 능숙자를 위한 절차 설명

### 3-1. EncodingFilter

`EncodingFilter#doFilter()` 흐름

1. `ServletRequest`와 `ServletResponse`를 `HttpServletRequest`로 다운캐스팅한다.
2. `request.setCharacterEncoding("UTF-8")`로 요청 파라미터 인코딩을 지정한다.
3. `response.setCharacterEncoding("UTF-8")`로 응답 인코딩을 지정한다.
4. `response.setContentType("text/html;charset=UTF-8")`로 브라우저 해석 방식을 명시한다.
5. `chain.doFilter(request, response)`로 다음 필터 또는 서블릿으로 요청을 넘긴다.

핵심은 요청 처리 전에 공통 전처리를 수행한다는 점이다.

### 3-2. AuthFilter

`AuthFilter#doFilter()` 흐름

1. `req.getRequestURI()`로 요청 경로를 확인한다.
2. 경로가 `/filter`로 시작하면 인증 검사를 수행한다.
3. 세션에서 `user`가 없으면 로그인하지 않은 상태로 판단한다.
4. 경로에 `premium`이 포함되면 인가 검사를 추가로 수행한다.
5. `grade`가 `premium`이 아니면 프리미엄 권한이 없다고 판단한다.
6. 마지막에 `chain.doFilter(request, response)`로 다음 단계로 넘긴다.

핵심은 URL 패턴을 기준으로 공통 접근 제어를 한 곳에서 처리한다는 점이다.

### 3-3. FilterServlet

`FilterServlet#doGet()`은 `/filter/*` 요청에 대해 본문을 출력한다.

- 필터를 통과한 요청만 실제 서블릿에 도착한다.
- 서블릿은 비즈니스 응답만 담당한다.
- 권한 검사는 앞단 필터에서, 화면 출력은 서블릿에서 처리하는 분리가 된다.

---

## 4. 코드에서 눈여겨볼 점

- `@WebFilter("/*")`
  - 모든 요청에 대해 필터를 적용한다.
  - 실제로는 필요한 경로만 좁혀서 거는 편이 더 안전하다.
- `chain.doFilter(...)`
  - 이 호출이 있어야 다음 필터나 서블릿으로 요청이 이어진다.
  - 호출하지 않으면 요청이 중간에서 멈춘다.
- `sendRedirect("/auth")`
  - 권한이 없을 때 로그인 화면으로 보낸다.
  - redirect는 새로운 요청을 발생시킨다.
- `session.getAttribute("grade").equals("premium")`
  - `grade`가 `null`이면 NPE가 날 수 있다.
  - 실무에서는 null 방어가 필요하다.
- 필터는 순서와 범위가 중요하다.
  - 인코딩 필터가 먼저 실행되어야 한글 파라미터가 깨지지 않는다.
  - 인증 필터는 그 다음에 요청을 차단하거나 통과시킨다.

---

## 5. 면접 대비 포인트

### 5-1. 필터의 역할

- 필터는 서블릿 요청/응답 전후에 공통 작업을 끼워 넣는 기능이다.
- 인코딩, 인증, 로깅, 압축, 공통 헤더 같은 처리를 담당할 수 있다.

### 5-2. 서블릿과의 차이

- 서블릿은 요청을 받아 실제 응답을 생성한다.
- 필터는 그 앞뒤에서 가로채기(intercept) 역할을 한다.
- 여러 서블릿에 공통으로 적용할 작업을 필터로 분리하면 중복을 줄일 수 있다.

### 5-3. 체인 구조

- `FilterChain`은 필터를 순서대로 이어주는 구조다.
- 앞 필터가 `chain.doFilter()`를 호출해야 뒤 필터와 서블릿이 실행된다.
- 전처리와 후처리를 넣기 좋다.

---

## 6. 취업 대비 면접 예상 문항

1. 필터와 서블릿의 차이는 무엇인가요?
2. 필터가 필요한 이유는 무엇인가요?
3. `chain.doFilter()`를 호출하지 않으면 어떻게 되나요?
4. `EncodingFilter`에서 UTF-8을 설정하는 이유는 무엇인가요?
5. `request.setCharacterEncoding()`과 `response.setCharacterEncoding()`의 역할 차이는 무엇인가요?
6. 인증 필터와 인가 필터는 어떻게 다르나요?
7. `@WebFilter("/*")`처럼 전체 경로에 필터를 거는 것의 장단점은 무엇인가요?
8. 필터에서 `sendRedirect()`를 했는데도 `chain.doFilter()`를 호출하면 어떤 문제가 생기나요?
9. 필터 순서가 중요한 이유는 무엇인가요?
10. 인코딩 필터와 인증 필터를 분리하는 이유는 무엇인가요?

---

## 7. 한 줄 정리

- `EncodingFilter`는 요청과 응답의 언어 환경을 정리한다.
- `AuthFilter`는 로그인과 권한을 검사한다.
- `FilterServlet`은 검사를 통과한 요청의 본문을 출력한다.
- 필터는 서블릿 앞단의 공통 관문이다.
