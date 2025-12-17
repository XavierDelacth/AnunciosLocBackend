package ao.co.isptec.aplm.projetoanuncioloc.Request;

import java.util.List;

public class LocalRequest {
    private String nome;
    private String tipo; // "GPS" ou "WIFI"
    private Double latitude;
    private Double longitude;
    private Integer raioMetros;
    private List<String> wifiIds;

    public LocalRequest(String nome, String tipo, Double latitude, Double longitude, Integer raioMetros, List<String> wifiIds) {
        this.nome = nome;
        this.tipo = tipo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raioMetros = raioMetros;
        this.wifiIds = wifiIds;
    }
}
