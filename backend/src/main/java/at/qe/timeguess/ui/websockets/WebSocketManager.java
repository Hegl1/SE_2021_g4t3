package at.qe.timeguess.ui.websockets;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

/**
 * Due to technical restrictions (see [1] and [2]), spring cannot initialize the
 * pushContexts required for websockets (with simpler words: autowiring does not
 * work). Only a CDI-implementation (in our case "Weld" by jBoss) is capable of
 * doing that. Hence this class is used as a "container" for the required
 * pushContexts which is fully managed by CDI and excluded from the spring
 * DI-capabilities (autowiring, etc...).
 *
 * Doing this way, the webSocketManager's fields, i.e. the various pushContexts,
 * can be initialized properly and accessed over this class within a
 * spring-managed bean. DO NOT mix spring-annotations with CDI annotations AND
 * only use this manager for managing push-contexts!
 *
 * If you require additional pushContexts, just append them and include a proper
 * getter for it. Please note that if you don't specify any channel-name, the
 * name of the variable will be used instead.
 *
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 *
 *
 * [1] https://github.com/joinfaces/joinfaces/issues/690#issuecomment-654059926
 * [2]
 * https://github.com/spring-projects/spring-framework/issues/22243#issuecomment-460228188
 */
@Named
@SessionScoped
public class WebSocketManager implements Serializable {

    // add other channels here + getter for them
    @Inject
    @Push(channel = "userRegistrationChannel")
    private PushContext userRegistrationChannel;
    @Inject
    @Push(channel = "messageChannel")
    private PushContext messageChannel;

    public PushContext getUserRegistrationChannel() {
        return userRegistrationChannel;
    }

    public PushContext getMessageChannel() {
        return messageChannel;
    }

}