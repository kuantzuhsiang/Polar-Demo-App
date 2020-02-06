package e.eric.polartest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.reactivestreams.Publisher;

import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarAccelerometerData;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarEcgData;
//import polar.com.sdk.api.model.PolarExerciseData;
//import polar.com.sdk.api.model.PolarExerciseEntry;
//import polar.com.sdk.api.model.PolarHrBroadcastData;
import polar.com.sdk.api.model.PolarHrData;
import polar.com.sdk.api.model.PolarOhrPPGData;
import polar.com.sdk.api.model.PolarOhrPPIData;
import polar.com.sdk.api.model.PolarSensorSetting;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    PolarBleApi api;
//    Disposable broadcastDisposable;
    Disposable ecgDisposable;
    Disposable accDisposable;
    Disposable ppgDisposable;
    Disposable ppiDisposable;
//    Disposable scanDisposable;
    String DEVICE_ID = "2FD0EA26";
//    Disposable autoConnectDisposable;
//    PolarExerciseEntry exerciseEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);

        final Button connect = this.findViewById(R.id.connect_button);
        final Button disconnect = this.findViewById(R.id.disconnect_button);
        final Button ecg = this.findViewById(R.id.ecg_button);
        final Button acc = this.findViewById(R.id.acc_button);
        final Button ppg = this.findViewById(R.id.ohr_ppg_button);
        final Button ppi = this.findViewById(R.id.ohr_ppi_button);

        final TextView ecgTV = this.findViewById(R.id.ecg);
        final TextView accxTV = this.findViewById(R.id.x);
        final TextView accyTV = this.findViewById(R.id.y);
        final TextView acczTV = this.findViewById(R.id.z);
        final TextView ppg0TV = this.findViewById(R.id.ppg0);
        final TextView ppg1TV = this.findViewById(R.id.ppg1);
        final TextView ppg2TV = this.findViewById(R.id.ppg2);
        final TextView ambientTV = this.findViewById(R.id.ambient);
        final TextView ppiTV = this.findViewById(R.id.ppi);
        final TextView errorTV = this.findViewById(R.id.error);

        api.setApiLogger(new PolarBleApi.PolarBleApiLogger() {
            @Override
            public void message(String s) {
                Log.d(TAG,s);
            }
        });

        Log.d(TAG,"version: " + PolarBleApiDefaultImpl.versionInfo());

        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered) {
                Log.d(TAG,"BLE power: " + powered);
            }

            @Override
            public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"CONNECTED: " + polarDeviceInfo.deviceId);
                DEVICE_ID = polarDeviceInfo.deviceId;
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"CONNECTING: " + polarDeviceInfo.deviceId);
                DEVICE_ID = polarDeviceInfo.deviceId;
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"DISCONNECTED: " + polarDeviceInfo.deviceId);
                ecgDisposable = null;
                accDisposable = null;
                ppgDisposable = null;
                ppiDisposable = null;
            }

            @Override
            public void ecgFeatureReady(String identifier) {
                Log.d(TAG,"ECG READY: " + identifier);
                // ecg streaming can be started now if needed
            }

            @Override
            public void accelerometerFeatureReady(String identifier) {
                Log.d(TAG,"ACC READY: " + identifier);
                // acc streaming can be started now if needed
            }

            @Override
            public void ppgFeatureReady(String identifier) {
                Log.d(TAG,"PPG READY: " + identifier);
                // ohr ppg can be started
            }

            @Override
            public void ppiFeatureReady(String identifier) {
                Log.d(TAG,"PPI READY: " + identifier);
                // ohr ppi can be started
            }

            @Override
            public void biozFeatureReady(String identifier) {
                Log.d(TAG,"BIOZ READY: " + identifier);
                // ohr ppi can be started
            }

            @Override
            public void hrFeatureReady(String identifier) {
                Log.d(TAG,"HR READY: " + identifier);
                // hr notifications are about to start
            }

            @Override
            public void disInformationReceived(String identifier, UUID uuid, String value) {
                Log.d(TAG,"uuid: " + uuid + " value: " + value);

            }

            @Override
            public void batteryLevelReceived(String identifier, int level) {
                Log.d(TAG,"BATTERY LEVEL: " + level);

            }

            @Override
            public void hrNotificationReceived(String identifier, PolarHrData data) {
                Log.d(TAG,"HR value: " + data.hr + " rrsMs: " + data.rrsMs + " rr: " + data.rrs + " contact: " + data.contactStatus + "," + data.contactStatusSupported);
            }

            @Override
            public void polarFtpFeatureReady(String s) {
                Log.d(TAG,"FTP ready");
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    api.connectToDevice(DEVICE_ID);
                } catch (PolarInvalidArgument polarInvalidArgument) {
                    polarInvalidArgument.printStackTrace();
                }
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    api.disconnectFromDevice(DEVICE_ID);
                } catch (PolarInvalidArgument polarInvalidArgument) {
                    polarInvalidArgument.printStackTrace();
                }
            }
        });

        ecg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ecgDisposable == null) {
                    ecgDisposable = api.requestEcgSettings(DEVICE_ID).toFlowable().flatMap(new Function<PolarSensorSetting, Publisher<PolarEcgData>>() {
                        @Override
                        public Publisher<PolarEcgData> apply(PolarSensorSetting polarEcgSettings) throws Exception {
                            PolarSensorSetting sensorSetting = polarEcgSettings.maxSettings();
                            return api.startEcgStreaming(DEVICE_ID,sensorSetting);
                        }
                    }).subscribe(
                            new Consumer<PolarEcgData>() {
                                @Override
                                public void accept(PolarEcgData polarEcgData) throws Exception {
                                    for( Integer microVolts : polarEcgData.samples ){
                                        Log.d(TAG,"    yV: " + microVolts);
                                        String microVoltsStr = microVolts + " mV";
                                        ecgTV.setText(microVoltsStr);
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG, ""+throwable.toString());
                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d(TAG, "complete");
                                }
                            }
                    );
                } else {
                    // NOTE stops streaming if it is "running"
                    ecgDisposable.dispose();
                    ecgDisposable = null;
                }
            }
        });

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accDisposable == null) {
                    accDisposable = api.requestAccSettings(DEVICE_ID).toFlowable().flatMap(new Function<PolarSensorSetting, Publisher<PolarAccelerometerData>>() {
                        @Override
                        public Publisher<PolarAccelerometerData> apply(PolarSensorSetting settings) throws Exception {
                            PolarSensorSetting sensorSetting = settings.maxSettings();
                            return api.startAccStreaming(DEVICE_ID,sensorSetting);
                        }
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe(
                            new Consumer<PolarAccelerometerData>() {
                                @Override
                                public void accept(PolarAccelerometerData polarAccelerometerData) throws Exception {
                                    for( PolarAccelerometerData.PolarAccelerometerSample data : polarAccelerometerData.samples ){
                                        Log.d(TAG,"    x: " + data.x + " y: " + data.y + " z: "+ data.z);
                                        accxTV.setText(data.x);
                                        accyTV.setText(data.y);
                                        acczTV.setText(data.z);
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG,""+throwable.getLocalizedMessage());
                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d(TAG,"complete");
                                }
                            }
                    );
                } else {
                    // NOTE dispose will stop streaming if it is "running"
                    accDisposable.dispose();
                    accDisposable = null;
                }
            }
        });

        ppg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ppgDisposable == null) {
                    ppgDisposable = api.requestPpgSettings(DEVICE_ID).toFlowable().flatMap(new Function<PolarSensorSetting, Publisher<PolarOhrPPGData>>() {
                        @Override
                        public Publisher<PolarOhrPPGData> apply(PolarSensorSetting polarPPGSettings) throws Exception {
                            return api.startOhrPPGStreaming(DEVICE_ID,polarPPGSettings.maxSettings());
                        }
                    }).subscribe(
                            new Consumer<PolarOhrPPGData>() {
                                @Override
                                public void accept(PolarOhrPPGData polarOhrPPGData) throws Exception {
                                    for( PolarOhrPPGData.PolarOhrPPGSample data : polarOhrPPGData.samples ){
                                        Log.d(TAG,"    ppg0: " + data.ppg0 + " ppg1: " + data.ppg1 + " ppg2: " + data.ppg2 + "ambient: " + data.ambient);
                                        ppg0TV.setText(data.ppg0);
                                        ppg1TV.setText(data.ppg1);
                                        ppg2TV.setText(data.ppg2);
                                        ambientTV.setText(data.ambient);
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG,""+throwable.getLocalizedMessage());
                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d(TAG,"complete");
                                }
                            }
                    );
                } else {
                    ppgDisposable.dispose();
                    ppgDisposable = null;
                }
            }
        });

        ppi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ppiDisposable == null) {
                    ppiDisposable = api.startOhrPPIStreaming(DEVICE_ID).observeOn(AndroidSchedulers.mainThread()).subscribe(
                            new Consumer<PolarOhrPPIData>() {
                                @Override
                                public void accept(PolarOhrPPIData ppiData) throws Exception {
                                    for(PolarOhrPPIData.PolarOhrPPISample sample : ppiData.samples) {
                                        Log.d(TAG, "ppi: " + sample.ppi
                                                + " blocker: " + sample.blockerBit + " errorEstimate: " + sample.errorEstimate);
                                        ppiTV.setText(sample.ppi);
                                        errorTV.setText(sample.errorEstimate);
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG,""+throwable.getLocalizedMessage());
                                }
                            },
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    Log.d(TAG,"complete");
                                }
                            }
                    );
                } else {
                    ppiDisposable.dispose();
                    ppiDisposable = null;
                }
            }
        });
    }
}
