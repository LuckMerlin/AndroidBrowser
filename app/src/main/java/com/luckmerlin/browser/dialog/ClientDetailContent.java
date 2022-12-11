package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;

import androidx.databinding.ObservableField;

import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.databinding.ClientDetailBinding;

public class ClientDetailContent extends BaseContent {
    private final ObservableField<Client> mClient=new ObservableField<>();

    @Override
    protected View onCreateContent(Context context) {
        ClientDetailBinding binding=inflate(context, R.layout.client_detail);
        if (null!=binding){
            binding.setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    public final ClientDetailContent setClient(Client client){
        mClient.set(client);
        return this;
    }

    public ObservableField<Client> getClient() {
        return mClient;
    }
}
