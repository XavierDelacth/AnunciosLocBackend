// AnuncioResponse.java
package ao.co.isptec.aplm.projetoanuncioloc.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AnuncioResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("imagemUrl")
    private String imagemUrl;

    @SerializedName("local")
    private Local local;

    @SerializedName("dataInicio")
    private String dataInicio;

    @SerializedName("dataFim")
    private String dataFim;

    @SerializedName("horaInicio")
    private String horaInicio;

    @SerializedName("horaFim")
    private String horaFim;

    @SerializedName("policyType")
    private String policyType;

    @SerializedName("modoEntrega")
    private String modoEntrega;

    @SerializedName("restricoes")
    private Map<String, String> restricoes;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public String getModoEntrega() { return modoEntrega; }
    public void setModoEntrega(String modoEntrega) { this.modoEntrega = modoEntrega; }

    public Map<String, String> getRestricoes() { return restricoes; }
    public void setRestricoes(Map<String, String> restricoes) { this.restricoes = restricoes; }

    // Método para converter para o modelo Anuncio do Android
    public Anuncio toAnuncio() {

        String imagemCompleta = "";
        if (imagemUrl != null && !imagemUrl.isEmpty()) {
            if (imagemUrl.startsWith("http")) {
                imagemCompleta = imagemUrl;
            } else {
                imagemCompleta = imagemUrl;
            }
        }

        // Formatar datas se necessário (de "2025-11-17" para "17/11/2025")
        String dataInicioFormatada = formatarData(dataInicio);
        String dataFimFormatada = formatarData(dataFim);

        Anuncio anuncio = new Anuncio(
                id,
                titulo,
                descricao,
                true, // salvo = true pois é um anúncio guardado
                local != null ? local.getNome() : "",
                imagemCompleta,
                dataInicioFormatada,
                dataFimFormatada,
                horaInicio,
                horaFim,
                policyType,
                modoEntrega
        );

        // Adicionar restrições se existirem
        if (restricoes != null) {
            for (Map.Entry<String, String> entry : restricoes.entrySet()) {
                anuncio.addChave(entry.getKey(), java.util.Arrays.asList(entry.getValue()));
            }
        }

        return anuncio;
    }

    private String formatarData(String data) {
        if (data == null || data.isEmpty()) return "";

        try {
            // Converte de "2025-11-17" para "17/11/2025"
            String[] partes = data.split("-");
            if (partes.length == 3) {
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}