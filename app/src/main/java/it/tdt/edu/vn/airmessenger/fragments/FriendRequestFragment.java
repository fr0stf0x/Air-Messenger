package it.tdt.edu.vn.airmessenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Locale;

import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.adapters.FriendRequestAdapter;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;

public class FriendRequestFragment extends Fragment {


    public static final String TAG = "FriendRequestFragment";

    TextView tvNotification;

    RecyclerView requestList;

    String userId = "";
    private FriendRequestAdapter adapter;
    Query mQuery;
    FirebaseFirestore db;
    FriendRequestAdapter.FriendRequestHandler handler;

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString(User.USER_ID_KEY);
        }
        Log.d(TAG, "Handler set up : " + (handler != null));
        if (adapter != null) {
            adapter.startListening();
            Log.d(TAG, "Adapter started listening");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (adapter != null) {
            adapter.stopListening();
            Log.d(TAG, "Adapter stopped listening");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_request_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestList = view.findViewById(R.id.requestList);
        tvNotification = view.findViewById(R.id.tvNotification);

        if (!userId.equals("")) {
            mQuery = db.collection(User.COLLECTION_NAME).document(userId).collection(FriendRequest.COLLECTION_NAME);
            adapter = new FriendRequestAdapter(mQuery, handler) {
                @Override
                protected void onDataChanged() {
                    if (adapter.getItemCount() == 0) {
                        Log.d(TAG, "Empty requests");
                        tvNotification.setVisibility(View.GONE);
                        requestList.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, getItemCount() + " new requests");
                        tvNotification.setText(String.format(Locale.getDefault(),
                                "%d %s", adapter.getItemCount(),
                                getResources().getString(R.string.friend_request_notification)));
                        tvNotification.setVisibility(View.VISIBLE);
                        requestList.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                protected void onError(FirebaseFirestoreException e) {
                    Log.d(TAG, "Error occurred");
                }
            };

            requestList.setLayoutManager(new LinearLayoutManager(getContext()));
            requestList.setAdapter(adapter);
            Log.d(TAG, "Adapter created");
        }
    }

    protected void setHandler(FriendRequestAdapter.FriendRequestHandler handler) {
        this.handler = handler;
    }
}
