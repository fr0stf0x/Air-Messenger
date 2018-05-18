package it.tdt.edu.vn.airmessenger.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnMessageClickListener;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class MessageAdapter extends FirestoreAdapter<MessageAdapter.MessageViewHolder> {
    public static final String TAG = "MessageAdapter";

    public static final int TYPE_SEND = 0;
    public static final int TYPE_NEW_DAY_SEND = 10;
    public static final int TYPE_RECEIVE = 1;
    public static final int TYPE_NEW_DAY_RECEIVE = 11;

    private OnMessageClickListener mListener;
    private String senderPhoto;
    private String receiverPhoto;
    private TextView tvTime;
    private SparseBooleanArray mSelectedItemsIds;

    public MessageAdapter(Query query, String senderPhoto, String receiverPhoto, OnMessageClickListener listener) {
        super(query);
        this.mListener = listener;
        this.senderPhoto = senderPhoto;
        this.receiverPhoto = receiverPhoto;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case TYPE_RECEIVE:
                view = inflater.inflate(R.layout.message_receive_layout, parent, false);
                break;
            case TYPE_NEW_DAY_RECEIVE:
                view = inflater.inflate(R.layout.message_receive_layout, parent, false);
                tvTime = view.findViewById(R.id.tvTime);
                tvTime.setVisibility(View.VISIBLE);
                break;
            case TYPE_NEW_DAY_SEND:
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
        if (type == TYPE_SEND || type == TYPE_NEW_DAY_SEND) {
            photo = senderPhoto;
        } else {
            photo = receiverPhoto;
        }
        holder.row.setBackgroundColor(mSelectedItemsIds.get(position) ?
                App.getContext().getResources().getColor(R.color.primaryLightColor_Light) : Color.TRANSPARENT);
        holder.bind(getSnapshot(position), position, photo, mListener);
    }

    @Override
    public int getItemViewType(int position) {
        String receiverId = getSnapshot(position)
                .getString(Message.FIELD_RECEIVER_ID);
        boolean isNewDay = true;
        if (position != 0) {
            Date previousDate = (Date) getSnapshot(position - 1).get(Message.FIELD_TIME);
            Date today = (Date) getSnapshot(position).get(Message.FIELD_TIME);
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            int previousDay = Integer.parseInt(dayFormat.format(previousDate));
            int thisDay = Integer.parseInt(dayFormat.format(today));
            if (thisDay == previousDay) {
                isNewDay = false;
            }
        }

        if (FirebaseHelper.getCurrentUser().getUid().equals(receiverId)) {
            // Receive case
            if (isNewDay) {
                return TYPE_NEW_DAY_RECEIVE;
            }
            return TYPE_RECEIVE;
        }
        // Send case
        if (isNewDay) {
            return TYPE_NEW_DAY_SEND;
        }
        return TYPE_SEND;
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

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout)
        RelativeLayout layout;

        @BindView(R.id.row)
        RelativeLayout row;

        @BindView(R.id.bubble)
        RelativeLayout bubble;

        @BindView(R.id.tvTime)
        TextView tvTime;

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

        protected void bind(final DocumentSnapshot messageSnapshot,
                            final int position, String photoUrl,
                            final OnMessageClickListener listener) {

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
//            msgContent.setTextIsSelectable(true);
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTimeSend.setText(dt.format(msg.getTime()));

            // Set time for textview time
            dt = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String day = dt.format(msg.getTime());
            if (day.equals(dt.format(Calendar.getInstance().getTime()))) {
                day = App.getContext().getResources().getString(R.string.default_time_today);
            }
            tvTime.setText(day);

            bubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (listener == null) {
                        return false;
                    }
                    return listener.onMessageLongClicked(position);
                }
            });
            bubble.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onMessageClicked(position);
                }
            });
        }
    }
}
