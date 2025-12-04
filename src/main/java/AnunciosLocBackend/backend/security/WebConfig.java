package AnunciosLocBackend.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Tenta vários caminhos possíveis
        String[] possiblePaths = {
            System.getProperty("user.dir") + "/uploads/imagens/",
            "uploads/imagens/",
            "./uploads/imagens/",
            Paths.get("").toAbsolutePath().toString() + "/uploads/imagens/"
        };
        
        String foundPath = null;
        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (java.nio.file.Files.exists(p) || java.nio.file.Files.exists(p.getParent())) {
                foundPath = path;
                break;
            }
        }
        
        if (foundPath == null) {
            // Cria a pasta se não existir
            foundPath = "uploads/imagens/";
            Path p = Paths.get(foundPath);
            try {
                java.nio.file.Files.createDirectories(p);
                logger.info(" Pasta criada: " + p.toAbsolutePath());
            } catch (Exception e) {
                logger.error(" Erro ao criar pasta: " + e.getMessage());
            }
        }
        
        logger.info(" Configurando servidor de arquivos estáticos...");
        logger.info(" Caminho: " + foundPath);
        logger.info(" URL: /uploads/imagens/** -> file:" + foundPath);
        
        // Adiciona o resource handler
        registry.addResourceHandler("/uploads/imagens/**")
                .addResourceLocations("file:" + foundPath)
                .setCachePeriod(0);
        
        // Adiciona também para caminho simples
        registry.addResourceHandler("/imagens/**")
                .addResourceLocations("file:" + foundPath)
                .setCachePeriod(0);
        
        logger.info(" Configuração concluída");
    }
}