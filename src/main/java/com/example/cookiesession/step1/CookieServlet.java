package com.example.cookiesession.step1;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/cookie")
public class CookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie[] cookies = req.getCookies();
        readCookies(cookies);
        // 쿠키를 보내려면
        resp.addCookie(new Cookie("firstCookie", "Hello_Cookie"));
        Cookie cookie = makeCookie();
        resp.addCookie(cookie);
        req.getRequestDispatcher("/WEB-INF/views/step1/cookie.jsp").forward(req, resp);
    }

    private Cookie makeCookie() {
        Cookie cookie = new Cookie("customCookie", "haha");
        cookie.setPath("/cookie"); // 이 경로에만 보낼 수 있게
        return cookie;
    }

    private void readCookies(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            System.out.println("cookie = " + cookie);
            System.out.println("cookie.getName() = " + cookie.getName());
            System.out.println("cookie.getValue() = " + cookie.getValue());
            System.out.println("cookie.getPath() = " + cookie.getPath());
            System.out.println("cookie.getDomain() = " + cookie.getDomain());
            System.out.println("cookie.getMaxAge() = " + cookie.getMaxAge());
            System.out.println("cookie.getSecure() = " + cookie.getSecure());

        }
    }
}
