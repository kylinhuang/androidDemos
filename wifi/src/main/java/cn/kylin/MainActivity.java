package cn.kylin;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.TreeMap;

public class MainActivity extends Activity implements View.OnClickListener {

    private WifiManager mWifiManager;
    private boolean mHasPermission;

    Button mGetWifiInfoButton;
    RecyclerView mWifiInfoRecyclerView;

    private List<ScanResult> mScanResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        initView();


        mHasPermission = checkPermission();
        if (!mHasPermission) {
            requestPermission();
        }
    }

    private void initView() {
        findViewById(R.id.open_wifi).setOnClickListener(this);
        findViewById(R.id.close_wifi).setOnClickListener(this);

        mGetWifiInfoButton = (Button) findViewById(R.id.get_wifi_info);
        mGetWifiInfoButton.setOnClickListener(this);

        mWifiInfoRecyclerView = (RecyclerView) findViewById(R.id.wifi_info_detail);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mWifiInfoRecyclerView.setLayoutManager(linearLayoutManager);
        mWifiInfoRecyclerView.setAdapter(new ScanResultAdapter());

    }

    private void sortList(List<ScanResult> list) {
        TreeMap<String, ScanResult> map = new TreeMap<>();
        for (ScanResult scanResult : list) {
            map.put(scanResult.SSID, scanResult);
        }
        list.clear();
        list.addAll(map.values());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_wifi:
                if (!mWifiManager.isWifiEnabled()) {
                    boolean result = mWifiManager.setWifiEnabled(true);
                    if (result) {
                        mGetWifiInfoButton.setEnabled(true);
                    } else {
                        mGetWifiInfoButton.setEnabled(false);
                    }
                }
                break;
            case R.id.close_wifi:
                if (!mWifiManager.isWifiEnabled()) {
                    boolean result = mWifiManager.setWifiEnabled(false);
                    if (result) {
                        mGetWifiInfoButton.setEnabled(false);
                    } else {
                        mGetWifiInfoButton.setEnabled(true);
                    }
                }
                break;
            case R.id.get_wifi_info:
                if (mWifiManager.isWifiEnabled()) {
                    mScanResultList = mWifiManager.getScanResults();
                    sortList(mScanResultList);
                    mWifiInfoRecyclerView.getAdapter().notifyDataSetChanged();
                }
                break;


        }

    }

    private class ScanResultViewHolder extends RecyclerView.ViewHolder {
        private TextView mWifiCapabilities;
        private View mView;
        private TextView mWifiName;
        private TextView mWifiLevel;

        ScanResultViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mWifiName = (TextView) itemView.findViewById(R.id.ssid);
            mWifiLevel = (TextView) itemView.findViewById(R.id.level);
            mWifiCapabilities = (TextView) itemView.findViewById(R.id.capabilities);

        }

        void bindScanResult(final ScanResult scanResult) {
            mWifiName.setText(scanResult.SSID);
            mWifiLevel.setText(String.valueOf(scanResult.level));

            mWifiCapabilities.setText(scanResult.capabilities);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean wpa = scanResult.capabilities.contains("WPA-PSK");
                    boolean wpa2 = scanResult.capabilities.contains("WPA2-PSK");

                    if (wpa || wpa2){
                        //


                    }else {
                        int netId = mWifiManager.addNetwork(createWifiConfig(scanResult.SSID, "", WIFICIPHER_NOPASS));
                        boolean enable = mWifiManager.enableNetwork(netId, true);
                        Log.d(" wifi ", "enable: " + enable);

                        boolean reconnect = mWifiManager.reconnect();
                        Log.d(" wifi ", " reconnect: " + reconnect);
                    }




                }
            });
        }
    }

    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
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
            mWifiManager.disableNetwork(config.networkId);
        }

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }

    private class ScanResultAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {
        @Override
        public ScanResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext())
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
    }

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private static final int PERMISSION_REQUEST_CODE = 0;

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWifiManager.isWifiEnabled() && mHasPermission) {
            mGetWifiInfoButton.setEnabled(true);
        } else {
            mGetWifiInfoButton.setEnabled(false);
            if (mScanResultList != null) {
                mScanResultList.clear();
                mWifiInfoRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean hasAllPermission = true;

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;
                    break;
                }
            }

            if (hasAllPermission) {
                mHasPermission = true;
            } else {
                mHasPermission = false;
                Toast.makeText(
                        this, "Need More Permission",
                        Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterBroadcastReceiver();
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("wifi_state", 11);
                Log.d("ZJTest", "AP state: " + state);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        this.unregisterReceiver(mBroadcastReceiver);
    }
}
