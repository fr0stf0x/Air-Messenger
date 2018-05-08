package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserAdapter extends FirestoreAdapter<UserAdapter.UserViewHolder> {
    public static final String TAG = "UserAdapter";

    public interface OnUserClickListener {
        void onUserClick(DocumentSnapshot user);
    }

    private OnUserClickListener mListener;
    private int flag;

    public UserAdapter(Query query, OnUserClickListener listener, int flag) {
        super(query);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.contact_summary, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, final int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvUser)
        TextView tvUser;

        @BindView(R.id.tvStatus)
        TextView tvStatus;

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void bind(
                final DocumentSnapshot userSnapshot,
                final OnUserClickListener listener) {

            User user = userSnapshot.toObject(User.class);
            if (user == null) {
                Log.d(TAG, "something wrong");
            }
            tvUser.setText(user.getName());
            Log.d(TAG, user.getName());
            tvStatus.setText(user.getStatus());

            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (listener != null) {
                                listener.onUserClick(userSnapshot);
                            }
                        }
                    }
            );
        }
    }
}
