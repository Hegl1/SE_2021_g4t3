package at.qe.timeguess.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import at.qe.timeguess.ui.controllers.demo.UserStatusController;

/**
 * This handler is triggered after the application-context is refreshed, i.e.
 * configurations are setup.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 *
 */
@Component
public class UserStatusInitializationHandler implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserStatusController userStatusController;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // init
        this.userStatusController.setupUserStatus();
    }

}
