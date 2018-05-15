package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;


import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnMessageClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class MessageAdapter extends FirestoreAdapter<MessageAdapter.MessageViewHolder> {
    public static final String TAG = "MessageAdapter";
    OnMessageClickListener mListener;

    public MessageAdapter(Query query, OnMessageClickListener listener) {
        super(query);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {

        }
        View view = inflater.inflate(R.layout.message_sent_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }


    //  TODO YOOYOYOY
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout)
        RelativeLayout outerLayout;

        @BindView(R.id.bubble)
        RelativeLayout bubble;

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;

        @BindView(R.id.msgContent)
        TextView msgContent;

        @BindView(R.id.tvTimeSend)
        TextView tvTimeSend;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void bind(final DocumentSnapshot messageSnapshot, final OnMessageClickListener listener) {
            Message msg = messageSnapshot.toObject(Message.class);
            if (msg == null) return;
            msgContent.setText(msg.getContent());

            SimpleDateFormat dt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
            String date = dt.format(msg.getTime());
            tvTimeSend.setText(date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMessageClicked(messageSnapshot);
                }
            });
        }
    }
}
