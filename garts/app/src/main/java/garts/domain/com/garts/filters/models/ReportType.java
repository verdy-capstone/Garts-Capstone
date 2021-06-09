package garts.domain.com.garts.filters.models;

import garts.domain.com.garts.R;
import garts.domain.com.garts.utils.UIUtils;

public enum ReportType {

    AD(UIUtils.getString(R.string.categories_report_type_ad)),
    USER(UIUtils.getString(R.string.categories_report_type_user));

    private String value;

    ReportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
