/*
 * Copyright (c) 2018-present, 美团点评
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package test.logan.dianping.com.logan;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dianping.logan.Logan;
import com.dianping.logan.SendLogCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    private TextView mTvInfo;
    private EditText mEditIp;
    private RealSendLogRunnable mSendLogRunnable;

    private String processName;

    private OnFocusChangeListener mButtonOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            v.setBackgroundColor(hasFocus ? Color.BLUE : Color.BLACK);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mSendLogRunnable = new RealSendLogRunnable();

        processName = MyApplication.getProcessName();

        // start service -> new process
        Intent intent = new Intent("test.logan.dianping.com.logan.action.SERVICE");
        intent.setPackage(getPackageName());
        startService(intent);

        loganTest();
    }

    private void initView() {
        Button button = (Button) findViewById(R.id.write_btn);
        Button batchBtn = (Button) findViewById(R.id.write_batch_btn);
        Button sendBtn = (Button) findViewById(R.id.send_btn);
        Button logFileBtn = (Button) findViewById(R.id.show_log_file_btn);

        Button send_btn_default = (Button) findViewById(R.id.send_btn_default);
        send_btn_default.setOnFocusChangeListener(mButtonOnFocusChangeListener);

        button.setOnFocusChangeListener(mButtonOnFocusChangeListener);
        batchBtn.setOnFocusChangeListener(mButtonOnFocusChangeListener);
        sendBtn.setOnFocusChangeListener(mButtonOnFocusChangeListener);
        logFileBtn.setOnFocusChangeListener(mButtonOnFocusChangeListener);

        mTvInfo = (TextView) findViewById(R.id.info);
        mEditIp = (EditText) findViewById(R.id.send_ip);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logan.w(processName + " 啊哈哈哈哈66666", 2);
            }
        });
        batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loganTest();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loganSend();
            }
        });
        logFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loganFilesInfo();
            }
        });
        findViewById(R.id.send_btn_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loganSendByDefault();
            }
        });
    }

    private void loganTest() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    for (int i = 0; i < 900; i++) {
                        Log.d(TAG, "times : " + i);
                        Logan.w(processName + " " + i, 1);
                        Thread.sleep(50);
                    }
                    Log.d(TAG, "write log end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Logan.f();
            }
        }.start();
    }

    private void loganSend() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        String d = dataFormat.format(new Date(System.currentTimeMillis()));
        String[] temp = new String[1];
        temp[0] = d;
        String ip = mEditIp.getText().toString().trim();
        if (!TextUtils.isEmpty(ip)) {
            mSendLogRunnable.setIp(ip);
        }
        Logan.s(temp, mSendLogRunnable);
    }

    private void loganFilesInfo() {
        Map<String, Long> map = Logan.getAllFilesInfo();
        if (map != null) {
            StringBuilder info = new StringBuilder();
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                info.append("文件日期：").append(entry.getKey()).append("  文件大小（bytes）：").append(
                        entry.getValue()).append("\n");
            }
            mTvInfo.setText(info.toString());

            try {
                parseLoganFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loganSendByDefault() {
        String buildVersion = "";
        String appVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = pInfo.versionName;
            buildVersion = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final String url = "https://openlogan.inf.test.sankuai.com/logan/upload.json";
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String date = dataFormat.format(new Date(System.currentTimeMillis()));
        Logan.s(url, date, "1", "logan-test-unionid", "deviceId", buildVersion, appVersion, new SendLogCallback() {
            @Override
            public void onLogSendCompleted(int statusCode, byte[] data) {
                final String resultData = data != null ? new String(data) : "";
                Log.d(TAG, "日志上传结果, http状态码: " + statusCode + ", 详细: " + resultData);
            }
        });
    }


    private void parseLoganFile() throws IOException {
        String encryptedDir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath()
                + File.separator + MyApplication.FILE_NAME;
        File dir = new File(encryptedDir);
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        byte[] encryptKey16 = "0123456789012345".getBytes();
        byte[] encryptIv16 = "0123456789012345".getBytes();

        for (int i = 0; i < files.length; i++) {
            String encryptedFilePath = files[i].getAbsolutePath();
            if (encryptedFilePath.endsWith("de")) {
                continue;
            }
            String dencryptedFilePath = encryptedFilePath + "de";
            LoganParser loganParser = new LoganParser(encryptKey16, encryptIv16);
            File encryptedFile = new File(encryptedFilePath);
            InputStream inputStream = new FileInputStream(encryptedFile);

            File dencryptedFile = new File(dencryptedFilePath);
            OutputStream outputStream = new FileOutputStream(dencryptedFile);
            loganParser.parse(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        }
    }

}
