package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Message> messages;

    public MessageAdapter(ArrayList<Message> messageArrayList) {
        this.messages = messageArrayList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(View itemView) {
            super(itemView);
        }
    }
}
