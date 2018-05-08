package it.tdt.edu.vn.airmessenger.adapters;

import android.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.models.Message;

public class MessageAdapter extends FirestoreAdapter<MessageAdapter.MessageViewHolder> {

    public interface OnMessageLongClickListener {
        void onMessageLongClicked(DocumentSnapshot msg);
    }

    OnMessageLongClickListener mListener;

    public MessageAdapter(Query query, OnMessageLongClickListener listener) {
        super(query);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // TODO
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
