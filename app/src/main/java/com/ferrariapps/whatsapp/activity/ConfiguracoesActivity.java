package com.ferrariapps.whatsapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.helper.Permissao;
import com.ferrariapps.whatsapp.helper.UsuarioFirebase;
import com.ferrariapps.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageViewPerfil;
    private EditText editNome;
    private ImageView imageAtualizarNome;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private Usuario usuarioLogado;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editNome = findViewById(R.id.editNome);
        imageAtualizarNome = findViewById(R.id.imageAtualizarNome);

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        Permissao.validarPermissoes(permissoesNecessarias,this,1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if (url != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(circleImageViewPerfil);
        }else {
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        editNome.setText(usuario.getDisplayName());

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_CAMERA);
                }


            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_GALERIA);
                }

            }
        });

        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = editNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);
                if (retorno){
                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            usuarioLogado.setToken(s);
                            usuarioLogado.setNome(nome);
                            usuarioLogado.atualizar();
                            Toast.makeText(ConfiguracoesActivity.this, "Nome alterado com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        assert data != null;
                        imagem =(Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        assert data != null;
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);
                        break;
                }

                if (imagem != null)

                circleImageViewPerfil.setImageBitmap(imagem);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                imagem.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] dadosImagem = baos.toByteArray();

                final StorageReference imagemRef = storageReference
                        .child("imagens")
                        .child("perfil")
                        .child(identificadorUsuario)
                        .child("perfil.jpeg");

                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ConfiguracoesActivity.this, "Falha ao realizar upload da imagem", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ConfiguracoesActivity.this, "Sucesso ao realizar upload da imagem", Toast.LENGTH_SHORT).show();
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Uri url = task.getResult();
                                atualizarFotoUsuario(url);
                            }
                        });
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void atualizarFotoUsuario(Uri url){
       boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);

       if (retorno){

           FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
               @Override
               public void onSuccess(String s) {
                   usuarioLogado.setToken(s);
                   usuarioLogado.setFoto(url.toString());
                   usuarioLogado.atualizar();
                   Toast.makeText(ConfiguracoesActivity.this, "Sua foto foi alterada!", Toast.LENGTH_SHORT).show();
               }
           });

       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){

            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }

        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar todas as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}