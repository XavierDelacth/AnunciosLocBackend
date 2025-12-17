package ao.co.isptec.aplm.projetoanuncioloc.Request;

public class ChangeUsernameRequest {

    private String newUsername;

    public ChangeUsernameRequest() {}

    public ChangeUsernameRequest(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
