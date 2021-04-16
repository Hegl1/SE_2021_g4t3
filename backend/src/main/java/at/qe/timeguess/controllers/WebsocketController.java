package at.qe.timeguess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import at.qe.timeguess.dto.TemporaryTestDTO;

@Controller
public class WebsocketController {

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	@MessageMapping("/placeholder/{id}")
	public void reply(@DestinationVariable final Integer id, @Payload final TemporaryTestDTO message) {
		simpMessageingTemplate.convertAndSend("/messagequeue/ingame/" + id, message);
	}

	@EventListener
	public void onDisconnectEvent(final SessionDisconnectEvent event) {
		// TODO detect user, do usefull stuff
	}

	@EventListener
	public void onConnectedEvent(final SessionConnectedEvent event) {
		// TODO detect user, do usefull stuff
	}

}
