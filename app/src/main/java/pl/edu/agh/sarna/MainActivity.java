package pl.edu.agh.sarna;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("displays");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.sample_text);

        Process p;
        try {
            p = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();

            p.waitFor();
            if (p.exitValue() == 0) {
                Log.i("rootAccess", "Success");
                tv.setText(displayMessage(true));
            }
            else {
                Log.e("rootAccess", "Failed");
                tv.setText(displayMessage(false));

            }

        } catch (Exception e) {
            Log.i("rootAccess", "Failed");
            tv.setText(displayMessage(false));
        }
    }

    public native String displayMessage(boolean privilleges);
}
