package com.nacare.capture.data;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.nacare.capture.data.service.FlipperManager;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Configuration;
import org.hisp.dhis.android.core.D2Manager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;

public class Sdk extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


    }

    public static D2 d2() {
        return D2Manager.getD2();
    }

    public static D2Configuration getD2Configuration(Context context) {
        // This will be null if not debug mode to make sure your data is safe
        Interceptor flipperInterceptor = FlipperManager.setUp(context.getApplicationContext());

        List<Interceptor> networkInterceptors = new ArrayList<>();
        if (flipperInterceptor != null) {
            networkInterceptors.add(flipperInterceptor);
        }

        return D2Configuration.builder()
                .appName("Nacare_Ke_App")
                .appVersion("0.0.1")
                .readTimeoutInSeconds(30)
                .connectTimeoutInSeconds(30)
                .writeTimeoutInSeconds(30)
                .networkInterceptors(networkInterceptors)
                .context(context)
                .build();
    }
}