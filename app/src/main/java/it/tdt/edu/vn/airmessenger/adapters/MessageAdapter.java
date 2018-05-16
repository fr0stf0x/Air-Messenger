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


import org.w3c.dom.Text;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnMessageClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class MessageAdapter extends FirestoreAdapter<MessageAdapter.MessageViewHolder> {

    public static final int TYPE_SEND = 0;
    public static final int TYPE_NEWDAY_SEND = 10;
    public static final int TYPE_RECEIVE = 1;
    public static final int TYPE_NEWDAY_RECEIVE = 11;
    public static final int TYPE_DATE_FALSE = 0;
    public static final int TYPE_DATE_TRUE = 1;

    public static final String TAG = "MessageAdapter";
    private OnMessageClickListener mListener;
    private String senderPhoto;
    private String receiverPhoto;
    private TextView tvTime;
    private CircleImageView senderAvatar, receiverAvatar;

    public MessageAdapter(Query query, OnMessageClickListener listener) {
        super(query);
        this.mListener = listener;
    }

    public void setSenderPhoto(String senderPhoto) {
        this.senderPhoto = senderPhoto;
    }

    public void setReceiverPhoto(String receiverPhoto) {
        this.receiverPhoto = receiverPhoto;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;

        switch (viewType) {
            case TYPE_RECEIVE:
                view = inflater.inflate(R.layout.message_receive_layout, parent, false);
                break;
            case TYPE_NEWDAY_RECEIVE:
                view = inflater.inflate(R.layout.message_receive_layout, parent, false);
                tvTime = view.findViewById(R.id.tvTime);
                tvTime.setVisibility(View.VISIBLE);
                break;
            case TYPE_NEWDAY_SEND:
                view = inflater.inflate(R.layout.message_sent_layout, parent, false);
                tvTime = view.findViewById(R.id.tvTime);
                tvTime.setVisibility(View.VISIBLE);
                break;
            default:
                view = inflater.inflate(R.layout.message_sent_layout, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String photo;
        int type = getItemViewType(position);
        if (type == TYPE_SEND || type == TYPE_NEWDAY_SEND) {
            photo = senderPhoto;
        } else {
            photo = receiverPhoto;
        }

        holder.bind(getSnapshot(position), photo, mListener);
    }


    //  TODO YOOYOYOY
    @Override
    public int getItemViewType(int position) {
        String receiverId = getSnapshot(position)
                .getString(Message.FIELD_RECEIVER_ID);
        boolean isNewDay = true;
        if (position != 0) {
            Date previousDate = (Date) getSnapshot(position - 1).get(Message.FIELD_TIME);
            Date today = (Date) getSnapshot(position).get(Message.FIELD_TIME);
            SimpleDateFormat day = new SimpleDateFormat("dd", Locale.getDefault());
            int previousDay = Integer.parseInt(day.format(previousDate));
            int thisDay = Integer.parseInt(day.format(today));
            if (thisDay == previousDay) {
                isNewDay = false;
            }
        }
        if (FirebaseHelper.getCurrentUser().getUid().equals(receiverId)) {
            if (isNewDay) {
                return TYPE_NEWDAY_RECEIVE;
            }
            return TYPE_RECEIVE;
        }
        if (isNewDay) {
            return TYPE_NEWDAY_SEND;
        }
        return TYPE_SEND;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTime)
        TextView tvTime;

        @BindView(R.id.bubble)
        RelativeLayout bubble;

        @BindView(R.id.ivAvatar)
        CircleImageView ivAvatar;

        @BindView(R.id.msgContent)
        TextView msgContent;

        @BindView(R.id.tvTimeSend)
        TextView tvTimeSend;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void bind(final DocumentSnapshot messageSnapshot, String photoUrl, final OnMessageClickListener listener) {
            Message msg = messageSnapshot.toObject(Message.class);
            if (msg == null) {
                return;
            }
            if (photoUrl != null && !photoUrl.equals("")) {
                Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.baseline_account_circle_black_48)
                        .into(ivAvatar);
            }
            msgContent.setText(msg.getContent());
            msgContent.setTextIsSelectable(true);
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTimeSend.setText(dt.format(msg.getTime()));

            // Set time for textview time
            dt = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String day = dt.format(msg.getTime());
            if (day.equals(dt.format(Calendar.getInstance().getTime()))) {
                day = App.getContext().getResources().getString(R.string.default_time_today);
            }
            tvTime.setText(day);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMessageClicked(messageSnapshot);
                }
            });
        }
    }
}
