package com.ferrariapps.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);

    }

    public void logarUsuario(Usuario usuario){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    abrirTelaPrincial();
                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao= "Usuário não está cadastrado!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: "+e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void validarAutenticacaoUsuario(View view){

        String txtEmail = editEmail.getText().toString();
        String txtSenha = editSenha.getText().toString();

            if (!txtEmail.isEmpty()){
                if (!txtSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setEmail(txtEmail);
                    usuario.setSenha(txtSenha);

                    logarUsuario(usuario);

                }else {
                    editSenha.setError("*");
                    editSenha.requestFocus();
                    Toast.makeText(LoginActivity.this,"Preencha a senha!",Toast.LENGTH_SHORT).show();
                }
            }else{
                editEmail.setError("*");
                editEmail.requestFocus();
                Toast.makeText(LoginActivity.this,"Preencha o email!",Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if(usuarioAtual != null){
            abrirTelaPrincial();
        }
    }

    public void abrirTelaCadastro(View view){
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    public void abrirTelaPrincial(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }


}