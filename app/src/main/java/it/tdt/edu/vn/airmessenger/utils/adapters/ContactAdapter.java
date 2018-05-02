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
import it.tdt.edu.vn.airmessenger.utils.models.User;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private ArrayList<User> friends;

    public ContactAdapter(ArrayList<User> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.contact_summary, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = friends.get(position);
        TextView tvUser = holder.mView.findViewById(R.id.tvUser);
        TextView tvStatus = holder.mView.findViewById(R.id.tvStatus);

        CircleImageView imageView = holder.mView.findViewById(R.id.imgUserAvatar);
        tvUser.setText(user.getDisplayName());
        tvStatus.setText(user.getStatus());
        // TODO(1) parse image and thumb image
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }
    }
}
