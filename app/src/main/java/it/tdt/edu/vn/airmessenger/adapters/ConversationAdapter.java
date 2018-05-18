package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnChatSelectedListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;


public class ConversationAdapter extends FirestoreAdapter<ConversationAdapter.ConversationViewHolder> {

    OnChatSelectedListener mListener;

    public ConversationAdapter(Query query, OnChatSelectedListener listener) {
        super(query);
        mListener = listener;
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

    /**
     * ViewHolder must be a static class so that we can use {@link ButterKnife}
     */
    static class ConversationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivChatPhoto)
        ImageView ivChatPhoto;

        @BindView(R.id.tvChatName)
        TextView tvChatName;

        @BindView(R.id.tvLastMsgTime)
        TextView tvLastMsgTime;

        @BindView(R.id.tvSummary)
        TextView tvSummary;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(final DocumentSnapshot conversationSnapshot,
                          final OnChatSelectedListener listener) {
            final String TAG = "Conversation bind";
            final String chatId = conversationSnapshot.getId();
            String receiverId = conversationSnapshot.getString(User.FIELD_CHAT_WITH);
            String chatPhoto = conversationSnapshot.getString(Conversation.FIELD_CHAT_PHOTO);
            Map<String, Object> lastMessage = (Map<String, Object>) conversationSnapshot.get(Conversation.FIELD_LAST_MESSAGE);

            Date lastMsgTime = (Date) lastMessage.get(Message.FIELD_TIME);
            SimpleDateFormat dt = new SimpleDateFormat("d MMM ''yy HH:mm", Locale.getDefault());

            if (chatPhoto != null && !chatPhoto.equals("")) {
                Picasso.get()
                        .load(chatPhoto)
                        .placeholder(R.drawable.male_50)
                        .into(ivChatPhoto);
            }
            tvChatName.setText(conversationSnapshot.getString(Conversation.FIELD_CONVERSATION_NAME));
            tvLastMsgTime.setText(dt.format(lastMsgTime));
            tvSummary.setText((String) lastMessage.get(Message.FIELD_CONTENT));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onConversationClicked(conversationSnapshot);
                }
            });

        }
    }
}
