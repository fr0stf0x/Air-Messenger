package it.tdt.edu.vn.airmessenger.utils.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.UserClickHandler;
import it.tdt.edu.vn.airmessenger.utils.models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> friends;
    private UserClickHandler handler;

    public UserAdapter(ArrayList<User> friendList, UserClickHandler handler) {
        this.friends = friendList;
        this.handler = handler;
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
        User user = friends.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.onUserClick(position);
            }
        });
        holder.tvUser.setText(user.getDisplayName());
        holder.tvStatus.setText(user.getStatus());
        // TODO(1) parse image and thumb image
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser;
        TextView tvStatus;
        CircleImageView ivAvatar;

        public UserViewHolder(View itemView) {
            super(itemView);
            this.tvUser = itemView.findViewById(R.id.tvUser);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
