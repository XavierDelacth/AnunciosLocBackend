package ao.co.isptec.aplm.projetoanuncioloc.Interface;  // Ajusta a package se necessário

import java.util.List;

// Interface comum para callbacks de adicionar local (GPS ou WiFi)
public interface OnLocalAddedListener {
    void onLocalAddedGPS(String nome, double lat, double lng, int raio);  // Para GPS
    void onLocalAddedWiFi(String nome, List<String> ssids);  // Para WiFi
}