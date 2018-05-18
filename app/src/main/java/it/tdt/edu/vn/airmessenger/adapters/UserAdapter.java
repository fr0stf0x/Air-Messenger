package it.tdt.edu.vn.airmessenger.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class UserAdapter extends FirestoreAdapter<UserAdapter.UserViewHolder> {
    public static final String TAG = "UserAdapter";

    public static final int TYPE_SETTING = 1;
    public static final int TYPE_VIEW = 0;

    private OnUserClickListener mListener;

    private SparseBooleanArray mSelectedItemsIds;

    public UserAdapter(Query query, OnUserClickListener listener, int flag) {
        super(query);
        this.mListener = listener;
        mSelectedItemsIds = new SparseBooleanArray();
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
        holder.row.setBackgroundColor(mSelectedItemsIds.get(position) ?
                App.getContext().getResources().getColor(R.color.primaryLightColor_Light) : Color.TRANSPARENT);
        holder.bind(getSnapshot(position).getId(), position, mListener);
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

    static class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout)
        RelativeLayout row;

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
                final String userId,
                final int position,
                final OnUserClickListener listener) {
            //
            FirebaseHelper.getFirestore()
                    .collection(User.COLLECTION_NAME)
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            final DocumentSnapshot userSnapshot = task.getResult();
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
                            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    if (listener == null) {
                                        return false;
                                    }
                                    listener.onUserLongClick(position);
                                    return true;
                                }
                            });
                            itemView.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (listener != null) {
                                                listener.onUserClick(position);
                                            }
                                        }
                                    }
                            );
                        }
                    });

        }
    }


}
