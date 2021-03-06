package com.ferrariapps.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.activity.ChatActivity;
import com.ferrariapps.whatsapp.activity.GrupoActivity;
import com.ferrariapps.whatsapp.adapter.ContatosAdapter;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.helper.RecyclerItemClickListener;
import com.ferrariapps.whatsapp.helper.UsuarioFirebase;
import com.ferrariapps.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        adapter = new ContatosAdapter(listaContatos, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter(adapter);

        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get(position);
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if (cabecalho){
                                    Intent intent = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatContato",usuarioSelecionado);
                                    startActivity(intent);
                                }

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }

    public void recuperarContatos(){

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaContatos.clear();

                Usuario itemGrupo = new Usuario();
                itemGrupo.setNome("Novo Grupo");
                itemGrupo.setEmail("");
                listaContatos.add(itemGrupo);

                for (DataSnapshot dados : snapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);
                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    assert emailUsuarioAtual != null;
                    assert usuario != null;
                    if (!emailUsuarioAtual.equals(usuario.getEmail())){
                    listaContatos.add(usuario);
                    }
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void pesquisarContatos(String texto) {

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for (Usuario usuario : listaContatos) {

            String nome = usuario.getNome().toLowerCase();
            if (nome.contains(texto)){
                listaContatosBusca.add(usuario);
            }

        }

        adapter = new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos() {
        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}