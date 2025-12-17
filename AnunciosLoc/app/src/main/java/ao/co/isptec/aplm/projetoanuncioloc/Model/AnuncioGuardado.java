package ao.co.isptec.aplm.projetoanuncioloc.Model;

import android.os.Parcel;
import android.os.Parcelable;
public class AnuncioGuardado implements Parcelable {
    private Long id;
    private Long usuarioId;
    private Long anuncioId;
    private Anuncio anuncio;

    public AnuncioGuardado() {}

    public AnuncioGuardado(Long usuarioId, Long anuncioId) {
        this.usuarioId = usuarioId;
        this.anuncioId = anuncioId;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getAnuncioId() { return anuncioId; }
    public void setAnuncioId(Long anuncioId) { this.anuncioId = anuncioId; }

    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }

    // Parcelable implementation
    protected AnuncioGuardado(Parcel in) {
        id = in.readByte() == 0 ? null : in.readLong();
        usuarioId = in.readByte() == 0 ? null : in.readLong();
        anuncioId = in.readByte() == 0 ? null : in.readLong();
        anuncio = in.readParcelable(Anuncio.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        if (usuarioId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(usuarioId);
        }
        if (anuncioId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(anuncioId);
        }
        dest.writeParcelable(anuncio, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AnuncioGuardado> CREATOR = new Creator<AnuncioGuardado>() {
        @Override
        public AnuncioGuardado createFromParcel(Parcel in) {
            return new AnuncioGuardado(in);
        }

        @Override
        public AnuncioGuardado[] newArray(int size) {
            return new AnuncioGuardado[size];
        }
    };
}