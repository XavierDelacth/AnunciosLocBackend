/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.security;

/**
 *
 * @author delacth
 */

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtBlacklist {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    public void add(String token) {
        blacklist.add(token);
    }

    public boolean contains(String token) {
        return blacklist.contains(token);
    }
}
