/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package AnunciosLocBackend.backend.enums;

/**
 *
 * @author delacth
 */

/**
 * Representa o tipo de localização usado para definir um local no sistema AnunciosLoc.
 * - GPS: baseado em coordenadas geográficas (latitude, longitude, raio)
 * - WIFI: baseado em identificadores de redes WiFi próximas (SSIDs)
 */


public enum TipoLocalizacao {
    GPS,
    WIFI
}
