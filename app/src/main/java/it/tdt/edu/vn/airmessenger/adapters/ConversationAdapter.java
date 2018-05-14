package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.App;
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

        private void bind(final DocumentSnapshot chatSnapshot,
                          final OnChatSelectedListener listener) {

            final String chatId = chatSnapshot.getId();
            final String TAG = "Conversation bind";
            String receiverId = chatSnapshot.getString(Conversation.FIELD_WITH);
            FirebaseHelper.getFirestore().collection(User.COLLECTION_NAME)
                    .document(receiverId)
                    .get()
                    .continueWith(new Continuation<DocumentSnapshot, Void>() {
                        @Override
                        public Void then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            if (task.isSuccessful()) {
                                String userName = task.getResult().getString(User.FIELD_NAME);
                                tvUser.setText(userName);

                                FirebaseHelper.getFirestore()
                                        .collection(Conversation.COLLECTION_NAME)
                                        .document(chatId)
                                        .collection(Conversation.FIELD_MESSAGE)
                                        .orderBy(Message.FIELD_TIME, Query.Direction.DESCENDING)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot lastMessage = task.getResult().getDocuments().get(0);
                                                    Date lastMsgTime = (Date) lastMessage.get(Message.FIELD_TIME);
                                                    SimpleDateFormat dt = new SimpleDateFormat("d MMM ''yy HH:mm", Locale.getDefault());
                                                    tvLastMsgTime.setText(dt.format(lastMsgTime));
                                                    tvSummary.setText(lastMessage.getString(Message.FIELD_CONTENT));

                                                    itemView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            listener.onChatClicked(chatSnapshot);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                            return null;
                        }
                    });

        }
    }
}
