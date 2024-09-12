package ma.dev7hd.studentspringngapp.websoket.config;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WebSocketService {

    private SimpMessagingTemplate messagingTemplate;

    public void sendToSpecificUser(String destination, Object object) {
        messagingTemplate.convertAndSend(destination, object);
    }
}
