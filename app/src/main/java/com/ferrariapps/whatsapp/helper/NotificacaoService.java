package com.ferrariapps.whatsapp.helper;


import com.ferrariapps.whatsapp.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {

    @Headers({"Authorization:key=INSIRA_A_CHAVE_FIREBASE_CLOUDMESSAGING"
            "Content-Type:application/json"})
    @POST("/fcm/send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);

}
