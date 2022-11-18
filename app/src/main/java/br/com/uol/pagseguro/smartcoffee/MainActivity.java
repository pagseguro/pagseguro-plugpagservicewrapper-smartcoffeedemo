package br.com.uol.pagseguro.smartcoffee;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.auth.AuthFragment;
import br.com.uol.pagseguro.smartcoffee.databinding.ActivityMainBinding;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerMainComponent;
import br.com.uol.pagseguro.smartcoffee.injection.MainComponent;
import br.com.uol.pagseguro.smartcoffee.injection.ScreenFlowModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.nfc.NFCFragment;
import br.com.uol.pagseguro.smartcoffee.otherFeatures.OtherFeaturesFragment;
import br.com.uol.pagseguro.smartcoffee.payments.transactions.TransactionsFragment;
import br.com.uol.pagseguro.smartcoffee.printer.PrinterFragment;
import br.com.uol.pagseguro.smartcoffee.utils.FragmentFlowManager;

public class MainActivity extends AppCompatActivity {

    @Inject
    FragmentFlowManager mFlowManager;

    MainComponent mInjector;

    private ActivityMainBinding binding;

    BottomNavigationView.OnNavigationItemSelectedListener bottonMenuListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int menuId = item.getItemId();
            Fragment fragment = AuthFragment.getInstance();

            switch (menuId) {
                case R.id.menu_auth:
                    fragment = AuthFragment.getInstance();
                    break;
                case R.id.menu_transactions:
                    fragment = TransactionsFragment.getInstance();
                    break;
                case R.id.menu_nfc:
                    fragment = NFCFragment.getInstance();
                    break;
                case R.id.menu_printer:
                    fragment = PrinterFragment.getInstance();
                    break;
                case R.id.other_features:
                    fragment = OtherFeaturesFragment.getInstance();
                    break;
            }

            mFlowManager.showFragment(fragment, MainActivity.this);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mInjector = DaggerMainComponent.builder()
                .screenFlowModule(new ScreenFlowModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_content) instanceof HomeFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void initView() {
        mFlowManager.showFragment(AuthFragment.getInstance(), this);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(bottonMenuListener);
    }

    public MainComponent getMainComponent() {
        return mInjector;
    }
}
