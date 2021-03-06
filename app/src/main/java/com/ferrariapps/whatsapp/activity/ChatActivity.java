package com.ferrariapps.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.adapter.MensagensAdapter;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.helper.Base64Custom;
import com.ferrariapps.whatsapp.helper.NotificacaoService;
import com.ferrariapps.whatsapp.helper.UsuarioFirebase;
import com.ferrariapps.whatsapp.model.Conversa;
import com.ferrariapps.whatsapp.model.Grupo;
import com.ferrariapps.whatsapp.model.Mensagem;
import com.ferrariapps.whatsapp.model.Notificacao;
import com.ferrariapps.whatsapp.model.NotificacaoDados;
import com.ferrariapps.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity{

    private TextView textViewNomeChat;
    private CircleImageView circleImageFotoChat;
    private EditText editMensagem;
    private ImageView imageCamera;
    private Usuario usuarioDestinatario, usuarioRemetente;
    private FirebaseUser userRemetente;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private static final int SELECAO_CAMERA = 100;
    private Retrofit retrofit;
    private String baseUrl;
    private String tokento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewNomeChat = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);
        editMensagem.requestFocus();

        baseUrl = "https://fcm.googleapis.com";
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                usuarioRemetente.setToken(s);
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.containsKey("chatGrupo")) {

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNomeChat.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if (foto != null && !foto.equals("")) {
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

            } else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNomeChat.setText(usuarioDestinatario.getNome());
                tokento = usuarioDestinatario.getToken();

                String foto = usuarioDestinatario.getFoto();
                if (foto != null && !foto.equals("")) {
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.getRecycledViewPool().setMaxRecycledViews(0, 0);
        recyclerMensagens.getRecycledViewPool().setMaxRecycledViews(1, 0);
        adapter = new MensagensAdapter(mensagens, getApplicationContext());
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_CAMERA);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                if (requestCode == SELECAO_CAMERA) {
                    assert data != null;
                    imagem = (Bitmap) data.getExtras().get("data");
                }

                if (imagem != null) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dadosImagem = baos.toByteArray();
                    String nomeImagem = UUID.randomUUID().toString();
                    final StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Falha ao realizar upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String downloadUrl = Objects.requireNonNull(task.getResult()).toString();


                                    if (usuarioDestinatario != null) {

                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("Imagem.jpeg");
                                        mensagem.setImagem(downloadUrl);

                                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);


                                        salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);
                                        salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

                                    } else {

                                        for (Usuario membro : grupo.getMembros()) {


                                            String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                            mensagem.setMensagem("Imagem.jpeg");
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setImagem(downloadUrl);

                                            salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);
                                            salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                                        }

                                    }



                                }
                            });


                        }
                    });


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void enviarMensagem(View view) {
        String textoMensagem = editMensagem.getText().toString();
        if (!textoMensagem.isEmpty()) {

            if (usuarioDestinatario != null) {

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

                Notificacao notificacao = new Notificacao(usuarioRemetente.getNome(), mensagem.getMensagem(), usuarioRemetente.getFoto());
                NotificacaoDados notificacaoDados = new NotificacaoDados(usuarioDestinatario.getToken(), notificacao);

                NotificacaoService service = retrofit.create(NotificacaoService.class);
                Call<NotificacaoDados> call = service.salvarNotificacao(notificacaoDados);

                call.enqueue(new Callback<NotificacaoDados>() {
                    @Override
                    public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                        if (response.isSuccessful()){
                            Toast.makeText(ChatActivity.this,
                                    "Código: "+response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NotificacaoDados> call, Throwable t) {
                        t.getCause();
                    }
                });

            } else {

                for (Usuario membro : grupo.getMembros()) {

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                }
            }

        }

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup) {

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if (isGroup) {
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);

        } else {
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
        }

        conversaRemetente.salvar();

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        editMensagem.setText(null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        recuperarMensagens();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens() {

        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
                recyclerMensagens.smoothScrollToPosition(mensagens.size());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}