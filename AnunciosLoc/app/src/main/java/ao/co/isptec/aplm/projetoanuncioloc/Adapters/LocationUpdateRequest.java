package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import java.util.List;

public class LocationUpdateRequest {
    private Long userId;
    private Double lat;
    private Double lng;
    private List<String> wifiIds;

    public LocationUpdateRequest(Long userId, Double lat, Double lng, List<String> wifiIds) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.wifiIds = wifiIds;
    }
}
