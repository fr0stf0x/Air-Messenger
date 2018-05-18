package it.tdt.edu.vn.airmessenger.interfaces;

import android.view.View;

public interface MessageClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
