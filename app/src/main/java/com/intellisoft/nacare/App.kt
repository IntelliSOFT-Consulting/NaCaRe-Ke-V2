package com.intellisoft.nacare

import android.app.Application
import android.content.Intent
import android.util.Log
import com.intellisoft.nacare.core.Sdk
import com.intellisoft.nacare.sync.Dhis2
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager.instantiateD2


class App : Application() {

    private var disposable: Disposable? = null
    override fun onCreate() {
        super.onCreate()
        try {
            val serviceIntent = Intent(this, Dhis2::class.java)
//      startService(serviceIntent)
            disposable = instantiateD2(Sdk.getD2Configuration(this))
                .flatMap<Boolean> { d2: D2 ->
                    d2.userModule().isLogged()
                }
                .doOnSuccess { isLogged: Boolean ->
                    Log.e("TAG", "Success *****")
                }.doOnError { throwable: Throwable ->
                    throwable.printStackTrace()
                    Log.e("TAG", "Error")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("App", "Service Failed to Start.....")
        }
    }


}
