package br.com.uol.pagseguro.smartcoffee.permissions;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.FragmentPermissionsBinding;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerSoftwareCapabilityComponent;
import br.com.uol.pagseguro.smartcoffee.injection.SoftwareCapabilityComponent;
import br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability.SoftwareCapabilityContract;
import br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability.SoftwareCapabilityPresenter;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class PermissionsFragment extends MvpFragment<SoftwareCapabilityContract, SoftwareCapabilityPresenter> implements SoftwareCapabilityContract, HomeFragment {

    private static final int PERMISSIONS_REQUEST_CODE = 0x1234;

    @Inject
    SoftwareCapabilityComponent mInjector;

    public static PermissionsFragment getInstance() {
        return new PermissionsFragment();
    }

    private FragmentPermissionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerSoftwareCapabilityComponent.builder()
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .build();
        binding = FragmentPermissionsBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    private void clickButtons() {
        binding.btnPermissions.setOnClickListener(click -> requestPermissions());
        binding.btnSoftwareCapabilities.setOnClickListener(click -> softwareCapabilities());
        binding.btnProductInitialization.setOnClickListener(click ->
                getPresenter().doProductInitialization()
        );
    }

    private String[] filterMissingPermissions(String[] permissions) {
        String[] missingPermissions;
        List<String> list;

        list = new ArrayList<>();

        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this.getContext().getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }
        }

        missingPermissions = list.toArray(new String[0]);

        return missingPermissions;
    }

    private void requestPermissions() {
        String[] missingPermissions;

        missingPermissions = this.filterMissingPermissions(this.getManifestPermissions());

        if (missingPermissions.length > 0) {
            requestPermissions(missingPermissions, PERMISSIONS_REQUEST_CODE);
        } else {
            showMessage();
        }
    }

    private void softwareCapabilities() {
        getPresenter().loadCapabilities();
    }

    private void showMessage() {
        UIFeedback.showDialog(getContext(), R.string.msg_all_permissions_granted);
    }

    private String[] getManifestPermissions() {
        String[] permissions = null;
        PackageInfo info;

        try {
            info = getContext().getPackageManager()
                    .getPackageInfo(this.getContext().getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SmartCoffee", "Package name not found", e);
        }

        if (permissions == null) {
            permissions = new String[0];
        }

        return permissions;
    }

    @Override
    public SoftwareCapabilityPresenter createPresenter() {
        return mInjector.presenter();
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            UIFeedback.showProgress(getContext());
        } else {
            UIFeedback.dismissProgress();
        }
    }

    @Override
    public void showDialog(String message) {
        UIFeedback.showDialog(getContext(), message);
    }
}
