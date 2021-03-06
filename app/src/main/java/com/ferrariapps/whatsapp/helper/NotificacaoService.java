package com.ferrariapps.whatsapp.helper;


import com.ferrariapps.whatsapp.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {

    @Headers({"Authorization:key=AAAAlKcjOaU:APA91bFzSyWwK__GaKBzySKRa84vG62uMXh5oZxJpVY9TTCeUJgdHyYmyhpZcawBkGgrS7j5K4G3aKKK5D7HYxHnB5t-Jh2l7g3SxQGc7_eVHRkrRkV47oA0FLecYyLXgH5MaSNwDBga",
            "Content-Type:application/json"})
    @POST("/fcm/send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);

}
