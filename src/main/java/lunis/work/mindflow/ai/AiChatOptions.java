package lunis.work.mindflow.ai;

public record AiChatOptions(Double temperature, boolean jsonMode) {
    public static AiChatOptions standard(Double temperature) {
        return new AiChatOptions(temperature, false);
    }

    public static AiChatOptions jsonMode(Double temperature) {
        return new AiChatOptions(temperature, true);
    }
}
