package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;

public class FriendRequestAdapter extends FirestoreAdapter<FriendRequestAdapter.FriendRequestViewHolder> {

    public interface FriendRequestHandler {
        void onFriendRequestAcceptedListener(DocumentSnapshot user);

        void onFriendRequestRejectedListener();
    }

    private FriendRequestHandler mHandler;

    public FriendRequestAdapter(Query query, FriendRequestHandler handler) {
        super(query);
        mHandler = handler;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_layout, parent, false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mHandler);
    }

    static class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvUser)
        TextView tvUser;

        @BindView(R.id.tvTimeSend)
        TextView tvTimeSend;

        @BindView(R.id.btnAccept)
        Button btnAccept;

        @BindView(R.id.btnReject)
        Button btnReject;

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void bind(final DocumentSnapshot requestRef, final FriendRequestHandler handler) {
            FriendRequest request = requestRef.toObject(FriendRequest.class);
            tvUser.setText(request.getUserName());
            tvTimeSend.setText(request.getSendTime().toString());
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.onFriendRequestAcceptedListener(requestRef);
                }
            });
            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.onFriendRequestRejectedListener();
                }
            });
        }

    }
}
