package com.base512.callidus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * TODO Add documentation for BootUpReceiver
 */

public class BootUpReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        Intent intent = new Intent(context, CallPromptService.class);
        context.startService(intent);
        Log.i("Autostart", "started");
    }
}