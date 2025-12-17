package ao.co.isptec.aplm.projetoanuncioloc.Model;  // Ajusta a package se necessário

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Local implements Serializable {
    @SerializedName("id")
    private long id;

    private String nome;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("raioMetros")
    private int raio;

    private List<String> wifiIds;

    public Local() {
        this.wifiIds = new ArrayList<>();
    }

    public Local(long id, String nome, Double latitude, Double longitude, int raio, List<String> wifiIds) {
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.wifiIds = wifiIds != null ? wifiIds : new ArrayList<>();
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() {
        return latitude != null ? latitude : 0.0;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude != null ? longitude : 0.0;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getRaio() { return raio; }
    public void setRaio(int raio) { this.raio = raio; }

    public List<String> getWifiIds() { return wifiIds; }
    public void setWifiIds(List<String> wifiIds) {
        this.wifiIds = wifiIds != null ? wifiIds : new ArrayList<>();
    }

    public boolean temWiFi() {
        return wifiIds != null && !wifiIds.isEmpty();
    }

    public String getTipo() {
        return temWiFi() ? "WiFi" : "GPS";
    }

    @Override
    public String toString() {
        return "Local{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", lat=" + latitude +
                ", lng=" + longitude +
                ", raio=" + raio +
                ", wifiIds=" + wifiIds +
                '}';
    }
}