package it.tdt.edu.vn.airmessenger.utils.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import it.tdt.edu.vn.airmessenger.utils.fragments.ContactListFragment;
import it.tdt.edu.vn.airmessenger.utils.fragments.ConversationListFragment;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    final Fragment[] FRAGMENTS = {
            new ConversationListFragment(),
            new ContactListFragment()
    };
    public static String[] TITLES = {"CONVERSATIONS", "CONTACTS"};

    // TODO(2) do something

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return FRAGMENTS[position];
    }

    @Override
    public int getCount() {
        return FRAGMENTS.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
