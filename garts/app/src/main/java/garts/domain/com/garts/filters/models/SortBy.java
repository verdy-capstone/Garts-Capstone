package garts.domain.com.garts.filters.models;

import garts.domain.com.garts.R;
import garts.domain.com.garts.utils.UIUtils;
public enum SortBy {

    RECENT(UIUtils.getString(R.string.categories_sort_by_recent_title)),
    LOWEST_PRICE(UIUtils.getString(R.string.categories_sort_by_lowest_price_title)),
    HIGHEST_PRICE(UIUtils.getString(R.string.categories_sort_by_highest_price_title)),
    NEW(UIUtils.getString(R.string.filter_condition_new)),
    USED(UIUtils.getString(R.string.filter_condition_used)),
    MOST_LIKED(UIUtils.getString(R.string.categories_sort_by_most_liked_title));

    private String value;

    SortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SortBy getForValue(String value) {
        for (int i = 0; i < SortBy.values().length; i++) {
            if (SortBy.values()[i].getValue().equals(value)) {
                return SortBy.values()[i];
            }
        }
        return null;
    }
}
