package it.tdt.edu.vn.airmessenger.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnChatSelectedListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;


public class ConversationAdapter extends FirestoreAdapter<ConversationAdapter.ConversationViewHolder> {

    OnChatSelectedListener mListener;
    private SparseBooleanArray mSelectedItemsIds;

    public ConversationAdapter(Query query, OnChatSelectedListener listener) {
        super(query);
        mListener = listener;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.row.setBackgroundColor(mSelectedItemsIds.get(position) ?
                App.getContext().getResources().getColor(R.color.primaryLightColor_Light) : Color.TRANSPARENT);
        holder.bind(getSnapshot(position), position, mListener);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.conversation_summary, parent, false);
        return new ConversationViewHolder(view);
    }

    // For ActionMode
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    @Override
    public DocumentSnapshot getSnapshot(int index) {
        return super.getSnapshot(index);
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder must be a static class so that we can use {@link ButterKnife}
     */
    static class ConversationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout)
        RelativeLayout row;

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

        private void bind(final DocumentSnapshot conversationSnapshot, final int position,
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
                    listener.onConversationClicked(position);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onConversationLongClicked(position);
                    return true;
                }
            });
        }
    }
}
