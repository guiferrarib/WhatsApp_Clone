package com.ferrariapps.whatsapp.model;

public class NotificacaoDados {

    private String to;
    private final String priority = "high";
    private Notificacao data;

    public NotificacaoDados(String to, Notificacao data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notificacao getData() {
        return data;
    }

    public void setData(Notificacao data) {
        this.data = data;
    }
}
