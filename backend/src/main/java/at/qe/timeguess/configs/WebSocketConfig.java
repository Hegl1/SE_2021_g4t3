package at.qe.timeguess.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		// URL that websockets need to register to
		registry.addEndpoint("/websocket").setAllowedOriginPatterns("*").withSockJS();
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		// messages from server will be placed at message queues
		// with the path /messagequeue/ingame/...
		config.enableSimpleBroker("/messagequeue/ingame");
		// path for handling incoming messages in corresponding controller
		config.setApplicationDestinationPrefixes("/ingame");
	}
}
