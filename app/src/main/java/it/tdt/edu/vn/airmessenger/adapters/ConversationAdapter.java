package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.models.Conversation;

public class ConversationAdapter extends FirestoreAdapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnChatSelectedListener {
        void onChatClicked(DocumentSnapshot chat);
//        void onChatLongClicked(DocumentSnapshot chat);
    }

    OnChatSelectedListener mListener;

    public ConversationAdapter(Query query, OnChatSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgUserAvatar)
        ImageView imgUserAvatar;

        @BindView(R.id.tvUser)
        TextView tvUser;

        @BindView(R.id.tvLastMsgTime)
        TextView tvLastMsgTime;

        @BindView(R.id.tvSummary)
        TextView tvSummary;


        public ConversationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(final DocumentSnapshot snapshot,
                          final OnChatSelectedListener listener) {
            Conversation chat = snapshot.toObject(Conversation.class);
            Picasso.get()
                    .load(chat.getChatPhoto())
                    .into(imgUserAvatar);
            tvUser.setText(chat.getSecondUser());
            // TODO set textview lastmsgtime

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onChatClicked(snapshot);
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.conversation_summary, parent, false);
        return new ConversationViewHolder(view);
    }

}
