package ao.co.isptec.aplm.projetoanuncioloc.Service;
import java.util.Map;
import java.util.List;


import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocationUpdateRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import ao.co.isptec.aplm.projetoanuncioloc.Request.AlterarSenhaRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Request.ChangeUsernameRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LoginRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/users/register")
    Call<User> register(@Body User user);

    @POST("/api/users/login")
    Call<User> login(@Body LoginRequest request);

    @Multipart
    @POST("/api/anuncios")
    Call<AnuncioResponse> criarAnuncio(
            @Part("userId") RequestBody userId,
            @Part("localId") RequestBody localId,
            @Part("titulo") RequestBody titulo,
            @Part("descricao") RequestBody descricao,
            @Part("dataInicio") RequestBody dataInicio,
            @Part("dataFim") RequestBody dataFim,
            @Part("horaInicio") RequestBody horaInicio,
            @Part("horaFim") RequestBody horaFim,
            @Part("policyType") RequestBody policyType,
            @Part("modoEntrega") RequestBody modoEntrega,
            @Part List<MultipartBody.Part> perfilChave,
            @Part List<MultipartBody.Part> perfilValor,
            @Part MultipartBody.Part imagem
    );

    @Multipart
    @PUT("/api/anuncios/{id}")
    Call<AnuncioResponse> atualizarAnuncio(
            @Path("id") Long id,
            @Part("userId") RequestBody userId,
            @Part("localId") RequestBody localId,
            @Part("titulo") RequestBody titulo,
            @Part("descricao") RequestBody descricao,
            @Part("dataInicio") RequestBody dataInicio,
            @Part("dataFim") RequestBody dataFim,
            @Part("horaInicio") RequestBody horaInicio,
            @Part("horaFim") RequestBody horaFim,
            @Part("policyType") RequestBody policyType,
            @Part("modoEntrega") RequestBody modoEntrega,
            @Part List<MultipartBody.Part> perfilChave,
            @Part List<MultipartBody.Part> perfilValor,
            @Part MultipartBody.Part imagem
    );

    @PATCH("/api/users/{id}/alterar-senha")
    Call<User> alterarSenha(@Path("id") Long id, @Body AlterarSenhaRequest request);

    @POST("/api/users/logout/{userId}")
    Call<Void> logout(@Path("userId") Long userId);

        @GET("/api/users/{id}/perfil")
        Call<java.util.Map<String, String>> getUserPerfis(@Path("id") Long id);

        @POST("/api/users/{id}/perfil")
        Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> adicionarPerfil(@Path("id") Long id, @Query("chave") String chave, @Query("valor") String valor);

        @DELETE("/api/users/{id}/perfil/{chave}/{valor}")
        Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> removerPerfilValor(@Path("id") Long id, @Path("chave") String chave, @Path("valor") String valor);

    @GET("/api/notificacoes")
    Call<List<Notificacao>> getNotificacoes(@Query("userId") Long userId);

    @DELETE("/api/notificacoes")
    Call<Void> limparNotificacoes(@Query("userId") Long userId);

    @GET("/api/notificacoes/count")
    Call<Integer> getContagemNotificacoes(@Query("userId") Long userId);




    // Anúncios Guardados - CORRIGIDOS
    @GET("/api/guardados/usuario/{usuarioId}")
    Call<List<AnuncioResponse>> listarAnunciosGuardados(@Path("usuarioId") Long usuarioId);

    @DELETE("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}")
    Call<Void> removerAnuncioGuardado(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);

    @POST("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}")
    Call<Void> guardarAnuncio(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);

    @GET("/api/guardados/usuario/{usuarioId}/anuncio/{anuncioId}/verificar")
    Call<Boolean> verificarAnuncioGuardado(@Path("usuarioId") Long usuarioId, @Path("anuncioId") Long anuncioId);


    // Meus Anúncios
    @GET("/api/anuncios/meus")
    Call<List<AnuncioResponse>> getMeusAnuncios(@Query("userId") Long userId);

    // Eliminar Anúncio
    @DELETE("/api/anuncios/{id}")
    Call<Void> eliminarAnuncio(
            @Path("id") Long id,
            @Query("userId") Long userId
    );
    // PERFIS - Compatível com seu backend
    @GET("/api/perfis")
    Call<List<ProfileKey>> getAllPerfis();

    @GET("/api/perfis/chave/{chave}")
    Call<ProfileKey> getPerfilPorChave(@Path("chave") String chave);

    @GET("/api/perfis/search")
    Call<List<ProfileKey>> searchPerfis(@Query("q") String query);

    @POST("/api/perfis")
    Call<ProfileKey> criarPerfil(@Body Map<String, Object> request);

    @POST("/api/perfis/object")
    Call<ProfileKey> criarPerfilComObjeto(@Body ProfileKey perfil);

    @PATCH("/api/perfis/chave/{chave}/valores")
    Call<ProfileKey> adicionarValores(@Path("chave") String chave, @Body List<String> valores);

    @DELETE("/api/perfis/chave/{chave}/valor/{valor}")
    Call<ProfileKey> removerValor(@Path("chave") String chave, @Path("valor") String valor);

    @DELETE("/api/perfis/chave/{chave}")
    Call<String> removerPerfilPorChave(@Path("chave") String chave);

    // Buscar anúncios próximos (centralizado)
    @GET("/api/anuncios/centralizado/proximos")
    Call<List<AnuncioResponse>> buscarAnunciosProximos(
            @Query("userId") Long userId,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("distanciaKm") Double distanciaKm
    );

    // Obter anúncio por ID
    @GET("/api/anuncios/{id}")
    Call<AnuncioResponse> getAnuncioPorId(@Path("id") Long id);

    // Locais
    @POST("/api/locais")
    Call<Local> criarLocal(@Body LocalRequest request, @Query("userId") Long userId);
    @GET("/api/locais/user/{userId}")
    Call<List<Local>> getLocaisDoUsuario(@Path("userId") Long userId);
    @GET("/api/locais")
    Call<List<Local>> getTodosLocais();
    @GET("/api/locais/search")
    Call<List<Local>> searchLocais(@Query("query") String query);
    @DELETE("/api/locais/{id}")
    Call<Void> excluirLocal(@Path("id") Long id);
    @PUT("/api/locais/{id}")
    Call<Local> atualizarLocal(@Path("id") Long id, @Body LocalRequest request);


    @PATCH("/api/users/{id}/change-username")
    Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> changeUsername(
            @Path("id") Long id,
            @Body ChangeUsernameRequest request
    );

    @POST("/api/locations/update")
    Call<Void> updateLocation(@Body LocationUpdateRequest request);

    @PATCH("/api/users/{id}/fcm-token")
    Call<Void> updateFcmToken(@Path("id") Long id, @Body Map<String, String> body);

    @retrofit2.http.HTTP(method = "DELETE", path = "/api/users/{id}/fcm-token", hasBody = true)
    Call<Void> unregisterFcmToken(@Path("id") Long id, @Body Map<String, String> body);
}
