package br.com.uol.pagseguro.smartcoffee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.databinding.ActivityApplicationSelectorBinding;
import br.com.uol.pagseguro.smartcoffee.demoInterno.DemoInternoActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FragmentFlowManager;

public class ApplicationSelectorActivity extends Activity {

    @Inject
    FragmentFlowManager mFragmentFlowManager;

    private ActivityApplicationSelectorBinding binding;

    public static ApplicationSelectorActivity getInstance() {
        return new ApplicationSelectorActivity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicationSelectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clickButtons();
    }

    private void clickButtons() {
        binding.btnDemo.setOnClickListener(click ->
                startActivity(DemoInternoActivity.class)
        );
        binding.btnAllFeatures.setOnClickListener(click ->
                startActivity(MainActivity.class)
        );
    }

    private void startActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}
