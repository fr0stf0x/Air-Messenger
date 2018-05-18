package it.tdt.edu.vn.airmessenger.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.adapters.MessageAdapter;
import it.tdt.edu.vn.airmessenger.models.Message;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageActionModeCallBack implements ActionMode.Callback {

    final String TAG = "ActionModeCallBack";

    private MessageAdapter adapter;

    public MessageActionModeCallBack(MessageAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        SparseBooleanArray selected;
        selected = adapter.getSelectedIds();
        int selectedMessageSize = selected.size();
        switch (item.getItemId()) {
            case R.id.action_copy:
                ClipboardManager clipboard = (ClipboardManager) App.getContext()
                        .getSystemService(CLIPBOARD_SERVICE);

                StringBuffer builder = new StringBuffer();
                for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        DocumentSnapshot message = adapter.getSnapshot(i);
                        builder.append(message.getString(Message.FIELD_SENDER_NAME)
                                + ": "
                                + message.getString(Message.FIELD_CONTENT)
                                + "\n");
                    }
                }
                ClipData clip = ClipData.newPlainText("Messages copied", builder);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(App.getContext(), "Messages has been copied", Toast.LENGTH_SHORT).show();
                mode.finish();//Finish action mode
                break;

            case R.id.action_delete:
                WriteBatch batch = FirebaseHelper.getFirestore().batch();
                for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        //get selected data in Model
                        Log.d(TAG, "onActionItemClicked: index " + i);
                        DocumentSnapshot message = adapter.getSnapshot(i);
                        batch.delete(message.getReference());
                    }
                }
                batch.commit()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(App.getContext(), "Delete messages successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mode.finish();
                break;
            case R.id.action_forward:
                Toast.makeText(App.getContext(), "Function 'forward' is WIP",
                        Toast.LENGTH_SHORT)
                        .show();
            case R.id.action_thumb_up:
                Toast.makeText(App.getContext(), "Function 'thumb' is WIP",
                        Toast.LENGTH_SHORT)
                        .show();
            case R.id.action_thumb_down:
                Toast.makeText(App.getContext(), "Function 'thumb' is WIP",
                        Toast.LENGTH_SHORT)
                        .show();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
