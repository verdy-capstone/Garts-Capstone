package garts.domain.com.garts.common.fragments;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import garts.domain.com.garts.utils.Configs;

public class BaseFragment extends Fragment {

    private AlertDialog progressDialog;

    protected void showLoading() {
        progressDialog = Configs.buildProgressLoadingDialog(getActivity());
        progressDialog.show();
    }

    protected void hideLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
