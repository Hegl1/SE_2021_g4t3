package at.qe.timeguess.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import at.qe.timeguess.dto.TestDTO;

@Controller
public class IngameMessageController {

	@Autowired
	private SimpMessagingTemplate simpMessageingTemplate;

	@MessageMapping("/test")
	public void reply(@Payload final TestDTO message) {
		System.out.println(message.getFrom() + " " + message.getText());
		simpMessageingTemplate.convertAndSend("/messagequeue/ingame", message);
	}

}
