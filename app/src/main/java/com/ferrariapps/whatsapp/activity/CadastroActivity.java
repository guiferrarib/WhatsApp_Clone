package com.ferrariapps.whatsapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.helper.Base64Custom;
import com.ferrariapps.whatsapp.helper.UsuarioFirebase;
import com.ferrariapps.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.messaging.FirebaseMessaging;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText editNome, editEmail, editSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);

    }

    public void cadastrarUsuario(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar usuário!", Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();

                    try {

                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                usuario.setToken(s);
                                String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                                usuario.setId(identificadorUsuario);
                                usuario.salvar();
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao= "Por favor digite um e-mail válido!";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Essa conta já foi cadastrada!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: "+e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void validarCadastroUsuario(View view){
        String txtNome = editNome.getText().toString();
        String txtEmail = editEmail.getText().toString();
        String txtSenha = editSenha.getText().toString();

        if (!txtNome.isEmpty()){
            if (!txtEmail.isEmpty()){
                if (!txtSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(txtNome);
                    usuario.setEmail(txtEmail);
                    usuario.setSenha(txtSenha);

                    cadastrarUsuario(usuario);


                }else {
                    editSenha.setError("*");
                    editSenha.requestFocus();
                    Toast.makeText(CadastroActivity.this,"Preencha a senha!",Toast.LENGTH_SHORT).show();
                }
            }else{
                editEmail.setError("*");
                editEmail.requestFocus();
                Toast.makeText(CadastroActivity.this,"Preencha o email!",Toast.LENGTH_SHORT).show();
            }
        }else{
            editNome.setError("*");
            editNome.requestFocus();
            Toast.makeText(CadastroActivity.this,"Preencha seu nome!",Toast.LENGTH_SHORT).show();
        }

    }

}