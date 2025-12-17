package ao.co.isptec.aplm.projetoanuncioloc.Model;

import com.google.gson.annotations.SerializedName;

public class Notificacao {
    private Long id;

    @SerializedName("userId")
    private Long userId;

    private String titulo;

    private String descricao;

    @SerializedName("anuncioId")
    private Long anuncioId;

    // MUDOU AQUI: String em vez de LocalDateTime
    @SerializedName("dataEnvio")
    private String dataEnvio;

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitulo() { return titulo != null ? titulo : "Sem título"; }
    public String getDescricao() { return descricao != null ? descricao : "Sem descrição"; }
    public Long getAnuncioId() { return anuncioId; }
    public String getDataEnvio() { return dataEnvio; }

    public Anuncio toAnuncio() {
        return new Anuncio(
                this.anuncioId, // Usa o anuncioId como ID do anúncio
                this.getTitulo(),
                this.getDescricao(),
                false, // salvo = false inicialmente (não guardado)
                "Local não especificado", // local - você pode ajustar se tiver essa info
                "", // imagem - vazia por padrão
                "", // dataInicio - vazia
                "", // dataFim - vazia
                "", // horaInicio - vazia
                "", // horaFim - vazia
                "Whitelist", // tipoRestricao padrão
                "Centralizado" // modoEntrega padrão
        );
    }

}