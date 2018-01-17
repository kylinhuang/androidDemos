package cn.kylin;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by kylinhuang on 16/01/2018.
 */

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder> {

    public static final int WIFICIPHER_NOPASS = 0;
    public static final int WIFICIPHER_WEP = 1;
    public static final int WIFICIPHER_WPA = 2;
    private WifiManager mWifiManager;

    private Context mContext;
    private List<ScanResult> mScanResultList = new ArrayList<>();

    public ScanResultAdapter(Context context, WifiManager mWifiManager) {
        this.mContext = context ;
        this.mWifiManager = mWifiManager ;
    }

    public void upData(List<ScanResult> list) {
        this.mScanResultList = list ;
        notifyDataSetChanged();
    }

    @Override
        public ScanResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.item_scan_result, parent, false);
            return new ScanResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ScanResultViewHolder holder, int position) {
            if (mScanResultList != null) {
                holder.bindScanResult(mScanResultList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (mScanResultList == null) {
                return 0;
            } else {
                return mScanResultList.size();
            }
        }

    public class ScanResultViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mWifiName;
        private TextView mWifiLevel;

        ScanResultViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mWifiName = (TextView) itemView.findViewById(R.id.ssid);
            mWifiLevel = (TextView) itemView.findViewById(R.id.level);
        }

        void bindScanResult(final ScanResult scanResult) {
            Log.e(" wifi "," bindScanResult " + scanResult);
            mWifiName.setText(mContext.getResources().getString(R.string.scan_wifi_name, "" + scanResult.SSID));
            mWifiLevel.setText(mContext.getResources().getString(R.string.scan_wifi_level, "" + scanResult.level));
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(" wifi " , " onClick " + scanResult);

                    int netId = mWifiManager.addNetwork(createWifiConfig(scanResult.SSID, "", WIFICIPHER_NOPASS));
                    boolean enable = mWifiManager.enableNetwork(netId, true);
                    Log.d("ZJTest", "enable: " + enable);
                    boolean reconnect = mWifiManager.reconnect();
                    Log.d("ZJTest", "reconnect: " + reconnect);
                }
            });
        }
    }


    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if(tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if(type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if(type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if(type == WIFICIPHER_WPA) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }
}
