package it.tdt.edu.vn.airmessenger.interfaces;

public interface OnUserClickListener extends RecyclerClickListener {
    void onUserClick(int position);

    boolean onUserLongClick(int position);

    @Override
    void onItemSelected(int position);
}
