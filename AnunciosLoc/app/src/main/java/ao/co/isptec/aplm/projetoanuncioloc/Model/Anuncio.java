package ao.co.isptec.aplm.projetoanuncioloc.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Anuncio implements Parcelable {

    public Long id;
    public String titulo;
    public String descricao;  // Mensagem do anúncio
    public boolean salvo;     // Para bookmark na lista
    public String local;      // Local de propagação (ex.: "Largo da Independência")
    public String imagem;     // Caminho ou URI da imagem

    // Novos campos baseados no layout activity_adicionar_anuncios.xml
    public String dataInicio; // ex.: "12/11/2025"
    public String dataFim;    // ex.: "15/11/2025"

    @SerializedName("restricoes")
    private Map<String, String> restricoesRaw = new HashMap<>();
    public String horaInicio; // ex.: "09:00"
    public String horaFim;    // ex.: "18:00"
    public String tipoRestricao; // "Whitelist" ou "Blacklist"
    public String modoEntrega;   // "Centralizado" ou "Descentralizado"

    // Mapa de restrições de perfil (já existente, mas integrado)
    private Map<String, List<String>> chavesPerfil = new HashMap<>();

    // CONSTRUTOR PARA DADOS DE TESTE (mantido e expandido com novos campos)
    public Anuncio(Long id, String titulo, String descricao, boolean salvo, String local, String imagem,
                   String dataInicio, String dataFim, String horaInicio, String horaFim,
                   String tipoRestricao, String modoEntrega) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.salvo = salvo;
        this.local = local;
        this.imagem = imagem;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.tipoRestricao = tipoRestricao;
        this.modoEntrega = modoEntrega;
        // chavesPerfil inicia vazio; popula com addChave depois
    }

    // CONSTRUTOR COMPLETO (para uso no AdicionarAnunciosActivity)
    public Anuncio(Long id,String titulo, String descricao, String local, String imagem,
                   String dataInicio, String dataFim, String horaInicio, String horaFim,
                   String tipoRestricao, String modoEntrega) {
        this(id ,titulo, descricao, false, local, imagem, dataInicio, dataFim, horaInicio, horaFim, tipoRestricao, modoEntrega);
    }

    // CONSTRUTOR DO PARCELABLE (atualizado com novos campos)
    protected Anuncio(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }

        titulo = in.readString();
        descricao = in.readString();
        salvo = in.readByte() != 0;
        local = in.readString();
        imagem = in.readString();
        dataInicio = in.readString();
        dataFim = in.readString();
        horaInicio = in.readString();
        horaFim = in.readString();
        tipoRestricao = in.readString();
        modoEntrega = in.readString();
        // Lê o mapa de chaves (simples para Map<String, List<String>>)
        chavesPerfil = (HashMap<String, List<String>>) in.readSerializable();
        if (chavesPerfil == null) {
            chavesPerfil = new HashMap<>();
        }
    }

    // MÉTODOS PARA CHAVES DE PERFIL (já existentes, mantidos)
    public void addChave(String keyName, List<String> values) {
        chavesPerfil.put(keyName, values);
    }

    public Map<String, String> getRestricoesRaw() {
        return restricoesRaw;
    }
    public Map<String, List<String>> getChavesPerfil() {
        Log.d("ANUNCIO_MODEL", "getChavesPerfil() chamado → retornando: " + chavesPerfil);
        if (chavesPerfil == null) {
            Log.e("ANUNCIO_MODEL", "chavesPerfil é NULL no modelo!");
        } else if (chavesPerfil.isEmpty()) {
            Log.w("ANUNCIO_MODEL", "chavesPerfil está VAZIO no modelo!");
        }
        return chavesPerfil;
    }

    // GETTERS E SETTERS PARA NOVOS CAMPOS (essenciais para o Adapter e Activity)
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public String getTipoRestricao() { return tipoRestricao; }
    public void setTipoRestricao(String tipoRestricao) { this.tipoRestricao = tipoRestricao; }


    public String getModoEntrega() { return modoEntrega; }

    public String getImagemUrl() {
        return imagem;
    }
    public void setModoEntrega(String modoEntrega) { this.modoEntrega = modoEntrega; }

    // CREATOR PARA PARCELABLE (atualizado, mas igual)
    public static final Creator<Anuncio> CREATOR = new Creator<Anuncio>() {
        @Override
        public Anuncio createFromParcel(Parcel in) {
            return new Anuncio(in);
        }

        @Override
        public Anuncio[] newArray(int size) {
            return new Anuncio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0); // flag para null
        } else {
            dest.writeByte((byte) 1); // flag para não-null
            dest.writeLong(id);
        }
        dest.writeString(titulo);
        dest.writeString(descricao);
        dest.writeByte((byte) (salvo ? 1 : 0));
        dest.writeString(local != null ? local : "");
        dest.writeString(imagem != null ? imagem : "");
        dest.writeString(dataInicio != null ? dataInicio : "");
        dest.writeString(dataFim != null ? dataFim : "");
        dest.writeString(horaInicio != null ? horaInicio : "");
        dest.writeString(horaFim != null ? horaFim : "");
        dest.writeString(tipoRestricao != null ? tipoRestricao : "");
        dest.writeString(modoEntrega != null ? modoEntrega : "");
        // Escreve o mapa de chaves
        dest.writeSerializable((HashMap<String, List<String>>) chavesPerfil);
    }
}