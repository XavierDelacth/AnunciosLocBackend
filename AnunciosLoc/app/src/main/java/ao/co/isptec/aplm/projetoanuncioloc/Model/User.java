package ao.co.isptec.aplm.projetoanuncioloc.Model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
public class User {
    private Long id;

    @SerializedName("username")
    private String username;

    @SerializedName("passwordHash")
    private String passwordHash;

    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("profiles")
    private Map<String, String> profiles;

    // CONSTRUTOR PARA REGISTRO
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password;
    }

    // GETTERS (para uso futuro)
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getSessionId() { return sessionId; }
    public Map<String, String> getProfiles() { return profiles; }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
