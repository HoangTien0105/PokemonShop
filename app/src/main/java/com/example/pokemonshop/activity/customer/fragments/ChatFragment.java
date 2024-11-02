package com.example.pokemonshop.activity.customer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pokemonshop.R;
import com.example.pokemonshop.adapters.CustomerChatAdapter;
import com.example.pokemonshop.api.Message.MessageRepository;
import com.example.pokemonshop.model.ChatHistoryResponse;
import com.example.pokemonshop.model.MessageDtoRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private CustomerChatAdapter chatAdapter;
    private int customerId;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new CustomerChatAdapter();
        recyclerView.setAdapter(chatAdapter);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        customerId = sharedPreferences.getInt("customerId", -1);

        Log.d("ChatFragment", "Id khách hàng được truy xuất: " + customerId);

        if (customerId != -1) {
            loadChatHistory(customerId);
        } else {
            Toast.makeText(getContext(), "Customer ID không tìm thấy", Toast.LENGTH_SHORT).show();
        }

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void loadChatHistory(int customerId) {
        MessageRepository.getChatHistoryByCustomerId(customerId).enqueue(new Callback<ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<ChatHistoryResponse> call, Response<ChatHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.setMessages(response.body().getMessageHistory());
                } else {
                    Log.e("ChatFragment", "Tải lịch sử chat thất bại (onResponse)");
                }
            }

            @Override
            public void onFailure(Call<ChatHistoryResponse> call, Throwable t) {
                Log.e("ChatFragment", "Tải lịch sử chat thất bại (onFailure)", t);
            }
        });
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString();
        if (content.isEmpty()) {
            return;
        }

        Log.d("ChatFragment", "Đang gửi tin nhắn với CustomerId: " + customerId);

        MessageDtoRequest request = new MessageDtoRequest();
        request.setCustomerId(customerId);
        request.setContent(content);
        request.setSendTime(java.time.LocalDateTime.now().toString());
        request.setType("CUSTOMER");

        MessageRepository.sendMessage(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadChatHistory(customerId);
                    editTextMessage.setText("");
                } else {
                    Log.e("ChatFragment", "Gửi tin nhắn thất bại (onResponse)");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ChatFragment", "Gửi tin nhắn thất bại (onFailure)", t);
            }
        });
    }
}