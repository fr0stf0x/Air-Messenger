package it.tdt.edu.vn.airmessenger.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.utils.Fragments.ContactListFragment;
import it.tdt.edu.vn.airmessenger.utils.Fragments.ConversationListFragment;

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    public final int CONVERSATION = 0;
    public final int CONTACTS = 1;

    // TODO(2) do something

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case CONVERSATION:
                return new ConversationListFragment();

            case CONTACTS:
                return new ContactListFragment();
            // add other tabs
        }
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
