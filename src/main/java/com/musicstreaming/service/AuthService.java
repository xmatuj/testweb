package com.musicstreaming.service;

import com.musicstreaming.model.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class AuthService {

    private static final String USER_SESSION_ATTRIBUTE = "loggedInUser";

    public void login(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_ATTRIBUTE, user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(USER_SESSION_ATTRIBUTE);
            session.invalidate();
        }
    }

    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(USER_SESSION_ATTRIBUTE);
        }
        return null;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    public boolean hasRole(HttpServletRequest request, User.UserRole role) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() == role;
    }

    public boolean hasAnyRole(HttpServletRequest request, User.UserRole... roles) {
        User user = getCurrentUser(request);
        if (user == null) return false;

        for (User.UserRole role : roles) {
            if (user.getRole() == role) return true;
        }
        return false;
    }

    public boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, User.UserRole.Admin);
    }

    public boolean isMusician(HttpServletRequest request) {
        return hasAnyRole(request, User.UserRole.Musician, User.UserRole.Admin);
    }

    public boolean canUploadTracks(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && user.canUploadTracks();
    }
}