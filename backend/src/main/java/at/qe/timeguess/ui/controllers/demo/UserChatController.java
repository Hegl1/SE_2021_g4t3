package at.qe.timeguess.ui.controllers.demo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import at.qe.timeguess.model.User;
import at.qe.timeguess.model.demo.Message;
import at.qe.timeguess.ui.beans.SessionInfoBean;

/**
 * This controller holds the chat-content of the logged-in user and provides a
 * method to send messages to dedicated recipients.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
@Controller
@Scope("session")
public class UserChatController implements Serializable {

    @Autowired
    private ChatManagerController chatController;
    @Autowired
    private SessionInfoBean sessionInfoBean;
    private Message currentMessage = new Message();
    private List<Message> chatContent;

    /**
     *
     * invoked when bean constructed (user navigates to chat-page, i.e. opens the
     * chat)
     */
    @PostConstruct
    public void setup() {
        User currentUser = this.sessionInfoBean.getCurrentUser();
        // get the reference once at setup, then reuse it
        this.chatContent = this.chatController.getChatContentRef(currentUser);
    }

    /**
     * Sends a message to selected recipients using websockets
     *
     * @return Navigation-target
     */
    public String sendMessage() {
        this.currentMessage.setFrom(this.sessionInfoBean.getCurrentUser());
        this.currentMessage.setTimestamp(new Date());
        this.chatController.deliver(this.currentMessage);
        return this.clearChat();
    }

    /**
     * When a user logs out, there shouldn't be the possibility to send him messages
     * anymore and hence it should be removed from any undelivered
     * message-recipient-list
     */
    public void synchronizeRecipients() {
        if (!this.currentMessage.getTo().isEmpty()) {
            this.currentMessage.getTo().removeIf(user -> !this.chatController.getPossibleRecipients().contains(user));
        }
    }

    /**
     * Reset current message.
     *
     * @return Navigation-target
     */
    private String clearChat() {
        this.currentMessage = new Message();
        return "";
    }

    public Message getCurrentMessage() {
        return currentMessage;
    }

    /**
     * Constructs the selectable recipient-list. It must be individual for every
     * user, because a user should not send a message to itself
     *
     * @return The selectable recipients
     */
    public List<SelectItem> getPossibleRecipients() {
        Set<User> loggedInUsers = this.chatController.getPossibleRecipients();
        return loggedInUsers.stream().filter(u -> !u.equals(this.sessionInfoBean.getCurrentUser()))
                        .map(user -> new SelectItem(user, user.getUsername())).collect(Collectors.toList());
    }

    public List<Message> getChatContent() {
        return chatContent;
    }

}
