package it.tdt.edu.vn.airmessenger.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.fragments.FriendsFragment;
import it.tdt.edu.vn.airmessenger.fragments.ConversationListFragment;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    public static final String CURRENT_PAGE_NUMBER_KEY = "current_page";

    private final Fragment[] FRAGMENTS = {
            new ConversationListFragment(),
            new FriendsFragment()
    };

    public final String[] TITLES = {App.getContext().getString(R.string.tab_conversation_label),
            App.getContext().getString(R.string.tab_friends_label)};

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
