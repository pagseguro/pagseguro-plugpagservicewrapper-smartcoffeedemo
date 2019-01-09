package uol.pagseguro.com.br.smartcoffee;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import uol.pagseguro.com.br.smartcoffee.auth.AuthFragment;
import uol.pagseguro.com.br.smartcoffee.injection.DaggerMainComponent;
import uol.pagseguro.com.br.smartcoffee.injection.MainComponent;
import uol.pagseguro.com.br.smartcoffee.injection.ScreenFlowModule;
import uol.pagseguro.com.br.smartcoffee.injection.WrapperModule;
import uol.pagseguro.com.br.smartcoffee.permissions.PermissionsFragment;
import uol.pagseguro.com.br.smartcoffee.transactions.TransactionsFragment;
import uol.pagseguro.com.br.smartcoffee.utils.FragmentFlowManager;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;

    @Inject
    FragmentFlowManager mFlowManager;

    MainComponent mInjector;

    BottomNavigationView.OnNavigationItemSelectedListener bottonMenuListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int menuId = item.getItemId();
            Fragment fragment = PermissionsFragment.getInstance();

            switch (menuId) {
                case R.id.menu_permissions:
                    fragment = PermissionsFragment.getInstance();
                    break;
                case R.id.menu_auth:
                    fragment = AuthFragment.getInstance();
                    break;
                case R.id.menu_transactions:
                    fragment = TransactionsFragment.getInstance();
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        mFlowManager.showFragment(PermissionsFragment.getInstance(), this);
        mBottomNavigationView.setOnNavigationItemSelectedListener(bottonMenuListener);
    }

    public MainComponent getMainComponent() {
        return mInjector;
    }
}
