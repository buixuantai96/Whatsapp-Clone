package won.kma.buitai.whatsapp.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import won.kma.buitai.whatsapp.fragment.ChatsFragment;
import won.kma.buitai.whatsapp.fragment.ContactsFragment;
import won.kma.buitai.whatsapp.fragment.GroupsFragment;
import won.kma.buitai.whatsapp.fragment.RequestFragment;

public class TabsAccesstorApdapter extends FragmentPagerAdapter {
    public TabsAccesstorApdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: return "Chats";
            case 1: return "Groups";
            case 2: return "Contacts";
            case 3: return "Request";
            default: return null;
        }
    }
}