package it.tdt.edu.vn.airmessenger.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.OnUserClickListener;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserAdapter extends FirestoreAdapter<UserAdapter.UserViewHolder> {
    public static final String TAG = "UserAdapter";

    public static final int TYPE_SETTING = 1;
    public static final int TYPE_VIEW = 0;

    private OnUserClickListener mListener;

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

    /**
     * if userId equals {@link FirebaseUser#getUid()}
     * this activity will become account setting activity
     */
    @Override
    public int getItemViewType(int position) {
        if (getSnapshot(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return TYPE_SETTING;
        }
        return TYPE_VIEW;
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
            //

            User user = userSnapshot.toObject(User.class);
            if (user == null) {
                Log.d(TAG, "something wrong");
                return;
            }
            Log.d(TAG, "bind: Binding user " + user.getName());
            // TODO Set thumbImage
            String thumbImage = user.getThumbImage();
            if (thumbImage != null) {
                Log.d(TAG, "bind: Thumb image " + thumbImage);
                Picasso.get()
                        .load(thumbImage)
                        .placeholder(R.drawable.man_icon)
                        .into(ivAvatar);
            }
            tvStatus.setText(user.getStatus());

            /**
             * if userId equals {@link FirebaseUser#getUid()}
             * this activity will become account setting activity
             * {@link #tvUser} and {@link #tvUser} will remain default
             * and no clickListener attached to {@link #itemView}
             */
//            if (userSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                return;
//            }

            tvUser.setText(user.getName());
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
