package uol.pagseguro.com.br.smartcoffee.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import uol.pagseguro.com.br.smartcoffee.MainActivity;
import uol.pagseguro.com.br.smartcoffee.R;

public class FragmentFlowManager {

    public void showFragment(Fragment fragment, Context context) {
        FragmentTransaction transaction = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
