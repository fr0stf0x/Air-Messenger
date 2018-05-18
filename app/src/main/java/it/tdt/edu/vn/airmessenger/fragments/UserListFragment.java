package it.tdt.edu.vn.airmessenger.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

import it.tdt.edu.vn.airmessenger.AllUsersActivity;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.MainActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.UserInfoActivity;
import it.tdt.edu.vn.airmessenger.adapters.UserAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnUserClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserListFragment extends Fragment implements OnUserClickListener {

    public static String COLLECTION_NAME = "friends";

    public static final String TAG = "User_list_fragment";

    public static final int USERS_FLAG = 1;
    public static final int FRIENDS_FLAG = 0;
    private int flag = FRIENDS_FLAG;

    private ActionMode mActionMode;

    public static final int LIMIT = 50;

    FirebaseUser user;
    FirebaseFirestore db;

    RecyclerView rvContacts;

    TextView tvEmptyList;

    UserAdapter adapter;
    Query mQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (context instanceof AllUsersActivity) {
            flag = USERS_FLAG;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onItemSelected(int position) {
        adapter.toggleSelection(position);//Toggle the selection
        Toast.makeText(App.getContext(), "Selected " + position, Toast.LENGTH_SHORT).show();
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((MainActivity) getContext()).startSupportActionMode(new UserActionModeCallBack() {
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    adapter.removeSelection();
                    Log.d(TAG, "onDestroyActionMode: setNullToActionMode");
                    setNullToActionMode();
                }
            });
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(adapter
                    .getSelectedCount()));
    }

    @Override
    public void onUserClick(int position) {
        Log.d(TAG, "onMessageClicked: At " + position);
        if (mActionMode != null) {
            onItemSelected(position);
        } else {
            startActivity(adapter.getSnapshot(position));
        }
    }

    private void startActivity(DocumentSnapshot user) {
        String userId = user.getId();
        Intent intent;
        switch (flag) {
            case USERS_FLAG:
                intent = new Intent(getContext(), UserInfoActivity.class);
                startIntentWithExtra(intent, userId);
                break;
            default: // jump to chat activity
                intent = new Intent(getContext(), ChatActivity.class);
                putExtrasAndStart(intent, userId);
                break;
        }
    }

    @Override
    public boolean onUserLongClick(int position) {
        Log.d(TAG, "onUserLongClick: At " + position);
        onItemSelected(position);

        return true;
    }

    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.d(TAG, "setNullToActionMode: Completed");
            mActionMode = null;
        }
    }

    class UserActionModeCallBack implements ActionMode.Callback, DialogInterface.OnClickListener {
        final String TAG = "ActionModeCallBack";

        SparseBooleanArray selected;
        int selectedItemSize;

        public UserActionModeCallBack() {
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.conversation_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

            selected = adapter.getSelectedIds();
            selectedItemSize = selected.size();

            switch (item.getItemId()) {
                case R.id.action_delete:
                    String arg = selectedItemSize > 1 ?
                            getString(R.string.arg_users) : getString(R.string.arg_user);
                    createDialog(
                            String.format(Locale.getDefault(),
                                    getString(R.string.delete_alert), arg)).show();
                    mode.finish();
                    break;
                case R.id.action_fav:
                    Toast.makeText(App.getContext(), "Function 'favorite' is WIP",
                            Toast.LENGTH_SHORT)
                            .show();
                    mode.finish();
                    break;
            }
            return true;
        }

        private void deleteSelected() {
            for (int i = (selected.size() - 1); i >= 0; i--) {
                if (selected.valueAt(i)) {
                    //get selected data in Model
                    Log.d(TAG, "onActionItemClicked: index " + i);
                    DocumentSnapshot conversation = adapter.getSnapshot(i);
                    Conversation.deleteConversation(
                            conversation.getId(),
                            user.getUid(),
                            conversation.getString(User.FIELD_CHAT_WITH)
                    );
                }
            }
        }

        private AlertDialog createDialog(String alertString) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialog dialog;
            dialog = builder
                    .setMessage(alertString)
                    .setPositiveButton(
                            R.string.delete_okay, this)
                    .setNegativeButton(
                            R.string.cancel, this)
                    .create();
            return dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                deleteSelected();
                dialog.dismiss();
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    private void startIntentWithExtra(Intent intent, String userId) {
        intent.putExtra(User.USER_ID_KEY, userId);
        startActivity(intent);
    }

    private void putExtrasAndStart(final Intent intent, final String receiverId) {
        db.collection(User.COLLECTION_NAME)
                .document(this.user.getUid())
                .collection(Conversation.COLLECTION_NAME)
                .whereEqualTo(User.FIELD_CHAT_WITH, receiverId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> result = task.getResult().getDocuments();
                            if (result.size() > 1) {
                                Log.d(TAG, "onComplete: ERROR - More than 1 conversation");
                                return;
                            }
                            if (result.size() == 1) {
                                String chatId = result.get(0).getId();
                                intent.putExtra(Conversation.CONVERSATION_ID_KEY, chatId);
                                Log.d(TAG, "onComplete: There 1 conversation: " + chatId);
                            }
                            startIntentWithExtra(intent, receiverId);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        rvContacts = view.findViewById(R.id.userList);

        if (user != null) {
            switch (flag) {
                case FRIENDS_FLAG:
                    mQuery = db.collection(User.COLLECTION_NAME)
                            .document(user.getUid()).collection(COLLECTION_NAME);
                    Log.d(TAG, "Case friends");
                    break;
                case USERS_FLAG:
                    mQuery = db.collection(User.COLLECTION_NAME);
                    Log.d(TAG, "Case users");
                    break;
            }

            adapter = new UserAdapter(mQuery, this, flag) {
                @Override
                protected void onDataChanged() {
                    if (flag == FRIENDS_FLAG) {
                        if (getItemCount() == 0) {
                            rvContacts.setVisibility(View.GONE);
                            tvEmptyList.setVisibility(View.VISIBLE);
                        } else {
                            rvContacts.setVisibility(View.VISIBLE);
                            tvEmptyList.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                protected void onError(FirebaseFirestoreException e) {
                    Log.d(TAG, "Error: check logs for info.");
                    rvContacts.setVisibility(View.GONE);
                    tvEmptyList.setVisibility(View.GONE);
                }
            };

            rvContacts.setAdapter(adapter);
            rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

}
