package lunis.work.mindflow.ai;

import java.util.List;

public interface AiChatClient {
    String chat(List<AiChatMessage> messages, AiChatOptions options);
}
