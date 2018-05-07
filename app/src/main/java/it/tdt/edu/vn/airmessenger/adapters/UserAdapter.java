package it.tdt.edu.vn.airmessenger.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.interfaces.UserClickHandler;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;
    private UserClickHandler handler;
    private int flag;

    public UserAdapter(ArrayList<User> users, int flag) {
        this.users = users;
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
        final User user = users.get(position);

        holder.tvUser.setText(user.getDisplayName());
        holder.tvStatus.setText(user.getStatus());

        // TODO(1) parse image and thumb image
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (flag) {
                    case UserListFragment.USERS_FLAG:

                    case UserListFragment.FRIENDS_FLAG:
                        Intent intent = new Intent(v.getContext(), ChatActivity.class);
                        intent.putExtra(User.FIELD_ID, user.getUserId());
                        v.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setHandler(UserClickHandler handler) {
        this.handler = handler;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser;
        TextView tvStatus;
        ImageView ivAvatar;

        public UserViewHolder(View itemView) {
            super(itemView);
            this.tvUser = itemView.findViewById(R.id.tvUser);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
