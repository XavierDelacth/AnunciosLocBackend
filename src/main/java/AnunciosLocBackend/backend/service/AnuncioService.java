/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;







import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.AnuncioRepository;
import AnunciosLocBackend.backend.repository.LocalRepository;
import AnunciosLocBackend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
/**
 *
 * @author hp
 */

@Service
public class AnuncioService
{
    @Autowired private AnuncioRepository anuncioRepo;
    @Autowired private LocalRepository localRepo;
    @Autowired private UserRepository userRepo;

    private static final String UPLOAD_DIR = "uploads/imagens/";

    public Anuncio criarAnuncio(Anuncio anuncio, Long userId, Long localId, MultipartFile imagem) throws IOException {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Local local = localRepo.findById(localId).orElseThrow(() -> new RuntimeException("Local não encontrado"));

        // Upload da imagem
        String imagemUrl = salvarImagem(imagem);
        anuncio.setImagemUrl(imagemUrl);
        anuncio.setUsuario(usuario);
        anuncio.setLocal(local);

        // Validações
        if (anuncio.getDataInicio().isAfter(anuncio.getDataFim())) {
            throw new RuntimeException("Data início deve ser antes da data fim");
        }

        return anuncioRepo.save(anuncio);
    }

    private String salvarImagem(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + filename);
        Files.copy(file.getInputStream(), path);
        return "/uploads/imagens/" + filename;
    }
}
