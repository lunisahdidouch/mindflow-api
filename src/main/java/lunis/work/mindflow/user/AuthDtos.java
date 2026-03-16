package lunis.work.mindflow.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @Email(message = "Voer een geldig e-mailadres in.") @NotBlank(message = "E-mail is verplicht.") String email,
            @NotBlank(message = "Wachtwoord is verplicht.")
            @Size(min = 8, message = "Wachtwoord moet minstens 8 tekens bevatten.") String password) {
    }

    public record LoginRequest(
            @Email(message = "Voer een geldig e-mailadres in.") @NotBlank(message = "E-mail is verplicht.") String email,
            @NotBlank(message = "Wachtwoord is verplicht.") String password) {
    }

    public record UserView(Long id, String email) {
    }

    public record AuthResponse(String token, UserView user) {
    }
}
