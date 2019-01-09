package uol.pagseguro.com.br.smartcoffee.permissions;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import uol.pagseguro.com.br.smartcoffee.R;

public class PermissionsFragment extends Fragment{

    //TODO estrutura esta igual ao demo antigo. Mudar?

    private static final int PERMISSIONS_REQUEST_CODE = 0x1234;

    public static PermissionsFragment getInstance() {
        return new PermissionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_permissions, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.btn_permissions)
    public void onRequestPermissionsClicked() {
        requestPermissions();
    }

    private String[] filterMissingPermissions(String[] permissions) {
        String[] missingPermissions = null;
        List<String> list = null;

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
        String[] missingPermissions = null;

        missingPermissions = this.filterMissingPermissions(this.getManifestPermissions());

        if (missingPermissions != null && missingPermissions.length > 0) {
            requestPermissions(missingPermissions, PERMISSIONS_REQUEST_CODE);
        } else {
            showMessage();
        }
    }

    private void showMessage() {
        Snackbar.make(getView().findViewById(R.id.btn_permissions), R.string.msg_all_permissions_granted, Snackbar.LENGTH_LONG).show();
    }

    private String[] getManifestPermissions() {
        String[] permissions = null;
        PackageInfo info = null;

        try {
            info = getContext().getPackageManager()
                    .getPackageInfo(this.getContext().getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("PlugPag", "Package name not found", e);
        }

        if (permissions == null) {
            permissions = new String[0];
        }

        return permissions;
    }
}
