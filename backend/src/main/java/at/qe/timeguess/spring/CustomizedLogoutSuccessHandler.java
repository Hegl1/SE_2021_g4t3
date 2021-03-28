package at.qe.timeguess.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import at.qe.timeguess.ui.controllers.demo.ChatManagerController;
import at.qe.timeguess.ui.controllers.demo.UserStatusController;

/**
 * This handler is triggered after a logout is performed.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 *
 */
@Component
public class CustomizedLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private UserStatusController userStatusController;
    @Autowired
    private ChatManagerController chatManagerController;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                    throws IOException, ServletException {
        String username = authentication.getName();
        // update chat-manager
        this.chatManagerController.onLogout(username);
        // update online-status
        this.userStatusController.afterLogout(username);
        // continue as expected
        super.onLogoutSuccess(request, response, authentication);
    }

}
