package com.android.forceupdate.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class InstallBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val installIntent = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
                context.startActivity(installIntent?.addFlags(FLAG_ACTIVITY_NEW_TASK))
                mutableInstallBroadcastState.value = InstallProgress

                when (PackageManager.GET_PERMISSIONS){
                    PackageManager.PERMISSION_GRANTED -> {
                        mutableInstallBroadcastState.value = InstallProgress
                    }
                    PackageManager.PERMISSION_DENIED-> {
                        mutableInstallBroadcastState.value = InstallCanceled
                    }

                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                mutableInstallBroadcastState.value = InstallSucceeded
            }
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                mutableInstallBroadcastState.value = InstallCanceled
            }
            else -> intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)?.let { message ->
                mutableInstallBroadcastState.value = InstallFailure(message)
            }
        }
    }

    companion object {
        private val mutableInstallBroadcastState = MutableStateFlow<InstallStatus>(InstallIdle)
        val installBroadcastState = mutableInstallBroadcastState.asStateFlow()
    }
}