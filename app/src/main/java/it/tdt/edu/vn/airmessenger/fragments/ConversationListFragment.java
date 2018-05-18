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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.MainActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.adapters.ConversationAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnChatSelectedListener;
import it.tdt.edu.vn.airmessenger.interfaces.RecyclerClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class ConversationListFragment extends Fragment implements OnChatSelectedListener, RecyclerClickListener {
    FirebaseUser firebaseUser;
    ConversationAdapter adapter;
    Query mQuery;
    FirebaseFirestore db;
    private ActionMode mActionMode;

    final String TAG = "ConversationList";

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.list_conversation)
    RecyclerView rvChats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            Log.d(TAG, "onStop: Adapter started listening");
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            Log.d(TAG, "onStop: Adapter stopped listening");
            adapter.stopListening();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversation_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.GONE);
        mQuery = db.collection(User.COLLECTION_NAME)
                .document(firebaseUser.getUid())
                .collection(Conversation.COLLECTION_NAME)
                .orderBy(Conversation.FIELD_LAST_MESSAGE + "." + Message.FIELD_TIME, Query.Direction.ASCENDING);
        adapter = new ConversationAdapter(mQuery, this);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(App.getContext()));
    }


    public void startChatting(DocumentSnapshot chat) {

        String chatId = chat.getId();
        String receiverId = chat.getString(User.FIELD_CHAT_WITH);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(User.USER_ID_KEY, receiverId);
        intent.putExtra(Conversation.CONVERSATION_ID_KEY, chatId);
        startActivity(intent);
    }

    @Override
    public boolean onConversationLongClicked(int position) {
        onItemSelected(position);
        return true;
    }

    @Override
    public void onConversationClicked(int position) {
        Log.d(TAG, "onMessageClicked: At " + position);
        if (mActionMode != null) {
            onItemSelected(position);
        } else {
            startChatting(adapter.getSnapshot(position));
        }
    }

    @Override
    public void onItemSelected(int position) {
        adapter.toggleSelection(position);//Toggle the selection
        Toast.makeText(App.getContext(), "Selected " + position, Toast.LENGTH_SHORT).show();
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((MainActivity) getContext()).startSupportActionMode(new ConversationActionModeCallBack() {
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

    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.d(TAG, "setNullToActionMode: Completed");
            mActionMode = null;
        }
    }

    class ConversationActionModeCallBack implements ActionMode.Callback, DialogInterface.OnClickListener {
        final String TAG = "ActionModeCallBack";

        SparseBooleanArray selected;
        int selectedItemSize;

        public ConversationActionModeCallBack() {
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
                            getString(R.string.arg_conversations) : getString(R.string.arg_conversation);
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
                            firebaseUser.getUid(),
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

}
