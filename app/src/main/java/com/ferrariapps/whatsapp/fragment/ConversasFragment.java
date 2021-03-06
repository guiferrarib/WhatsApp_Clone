package com.ferrariapps.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.activity.ChatActivity;
import com.ferrariapps.whatsapp.adapter.ConversasAdapter;
import com.ferrariapps.whatsapp.config.ConfiguracaoFirebase;
import com.ferrariapps.whatsapp.helper.RecyclerItemClickListener;
import com.ferrariapps.whatsapp.helper.UsuarioFirebase;
import com.ferrariapps.whatsapp.model.Conversa;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversas = new ArrayList<>();
    private ArrayList<String> mKeys = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas);

        adapter = new ConversasAdapter(listaConversas, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listaConversasAtualizada = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizada.get(position);
                                if (conversaSelecionada.getIsGroup().equals("true")) {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
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

        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas").child(identificadorUsuario);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    @Override
    public void onPause() {
        super.onPause();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    public void pesquisarConversas(String texto) {

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa : listaConversas) {

            if (conversa.getUsuarioExibicao() != null){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }

            }else{

                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }

            }
        }

        adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarConversas() {
        adapter = new ConversasAdapter(listaConversas, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas() {

        listaConversas.clear();
        mKeys.clear();

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
                String key = snapshot.getKey();
                mKeys.add(key);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversa conversa = snapshot.getValue(Conversa.class);
                String key = snapshot.getKey();
                int index = mKeys.indexOf(key);
                listaConversas.set(index, conversa);
                adapter.notifyDataSetChanged();
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