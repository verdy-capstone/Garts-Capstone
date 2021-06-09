package garts.domain.com.garts.common.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import garts.domain.com.garts.utils.Configs;

public class BaseActivity extends AppCompatActivity {

    private AlertDialog progressDialog;

    protected void showLoading() {
        progressDialog = Configs.buildProgressLoadingDialog(this);
        progressDialog.show();
    }

    protected void hideLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
