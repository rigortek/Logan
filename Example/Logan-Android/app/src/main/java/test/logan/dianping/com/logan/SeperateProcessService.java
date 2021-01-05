package test.logan.dianping.com.logan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.dianping.logan.Logan;

public class SeperateProcessService extends Service {
    public static final String TAG = "logan";

    public SeperateProcessService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final String processName = MyApplication.getProcessName();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 900; i++) {
//                    Log.d(TAG, "times : " + i);
//                    Logan.w(processName + " " + i, 1);
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Logan.f();
//                }
//            }
//        }).start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}