package br.com.uol.pagseguro.smartcoffee.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;

public class FragmentFlowManager {

    public void showFragment(Fragment fragment, Context context) {
        FragmentTransaction transaction = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
