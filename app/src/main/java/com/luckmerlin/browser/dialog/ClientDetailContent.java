package com.luckmerlin.browser.dialog;

import android.content.Context;
import android.view.View;
import androidx.databinding.ObservableField;
import com.luckmerlin.browser.BaseContent;
import com.luckmerlin.browser.BrowserExecutor;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.databinding.ClientDetailBinding;
import com.luckmerlin.click.OnClickListener;

public class ClientDetailContent extends BaseContent implements OnClickListener {
    private final ObservableField<Client> mClient=new ObservableField<>();
    private final ObservableField<Boolean> mEditEnable=new ObservableField<>(false);
    private final ObservableField<Boolean> mHostEditEnable=new ObservableField<>(false);
    private final ObservableField<String> mInputName=new ObservableField<>();
    private final ObservableField<String> mInputHost=new ObservableField<>();
    private final BrowserExecutor mExecutor;

    public ClientDetailContent(BrowserExecutor executor){
        mExecutor=executor;
    }

    @Override
    protected View onCreateContent(Context context) {
        ClientDetailBinding binding=inflate(context, R.layout.client_detail);
        if (null!=binding){
            binding.setVm(this);
            return binding.getRoot();
        }
        return null;
    }

    @Override
    public boolean onClick(View view, int clickId, int count, Object obj) {
        switch (clickId){
            case R.string.edit:
                return enableEdit(true);
            case R.string.cancel:
                return enableEdit(false);
            case R.string.close:
                return removeFromParent()||true;
            case R.string.save:
                BrowserExecutor executor=mExecutor;
                Client client=mClient.get();
                ClientMeta clientMeta=null!=client?client.getMeta():null;
                boolean succeed=false;
                if (null!=clientMeta&&null!=executor){
                    clientMeta.setName(mInputName.get());
                    clientMeta.setHost(mInputHost.get());
                    succeed=executor.saveClient(client,false);
                }
                return (toast(getString(succeed?R.string.succeed:R.string.fail))&&succeed&&
                        removeFromParent())||true;
        }
        return false;
    }

    public final boolean enableEdit(boolean enable){
        mEditEnable.set(enable);
        return true;
    }

    public final ClientDetailContent setClient(Client client){
        mClient.set(client);
        mHostEditEnable.set(null==client||!(client instanceof LocalClient));
        ClientMeta meta=null!=client?client.getMeta():null;
        mInputName.set(null!=meta?meta.getName():null);
        mInputHost.set(null!=meta?meta.getHost():null);
        return this;
    }

    public ObservableField<Client> getClient() {
        return mClient;
    }

    public ObservableField<Boolean> getEditEnable() {
        return mEditEnable;
    }

    public ObservableField<Boolean> getHostEditEnable() {
        return mHostEditEnable;
    }

    public ObservableField<String> getInputHost() {
        return mInputHost;
    }

    public ObservableField<String> getInputName() {
        return mInputName;
    }
}
