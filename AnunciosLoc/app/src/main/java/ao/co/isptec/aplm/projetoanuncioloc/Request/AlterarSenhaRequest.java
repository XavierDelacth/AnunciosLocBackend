package ao.co.isptec.aplm.projetoanuncioloc.Request;

public class AlterarSenhaRequest {
    private String senhaAtual;
    private String novaSenha;

    public AlterarSenhaRequest(String senhaAtual, String novaSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
    }

    public String getSenhaAtual() { return senhaAtual; }
    public String getNovaSenha() { return novaSenha; }
}
