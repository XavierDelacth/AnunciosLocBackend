/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.Notificacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author delacth
 */
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long>{
    List<Notificacao> findByUserIdOrderByDataEnvioDesc(Long userId);
    boolean existsByUserIdAndAnuncioId(Long userId, Long anuncioId);
    void deleteByUserId(Long userId);
}
