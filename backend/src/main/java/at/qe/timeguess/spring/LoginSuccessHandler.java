package at.qe.timeguess.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import at.qe.timeguess.ui.controllers.demo.ChatManagerController;
import at.qe.timeguess.ui.controllers.demo.UserStatusController;

/**
 * This handler is triggered after a login is performed.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 *
 */
@Component
public class LoginSuccessHandler implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    @Autowired
    private UserStatusController userStatusController;
    @Autowired
    private ChatManagerController chatManagerController;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        // update chat-manager
        this.chatManagerController.onLogin(username);
        // update online-status
        this.userStatusController.afterLogin(username);
    }

}
