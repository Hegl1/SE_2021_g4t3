package at.qe.timeguess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import at.qe.timeguess.dto.TemporaryTestDTO;

@Controller
public class IngameMessageController {

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	@MessageMapping("/test")
	public void reply(@Payload final TemporaryTestDTO message) {
		System.out.println(message.getText());
		simpMessageingTemplate.convertAndSend("/messagequeue/ingame", message);
	}

	@EventListener
	public void onDisconnectEvent(final SessionDisconnectEvent event) {
		// TODO detect disconnecting user
		System.out.println("One client disconnected");
	}

}
