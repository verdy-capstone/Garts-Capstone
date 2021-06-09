package garts.domain.com.garts.home.enums;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import garts.domain.com.garts.R;

public enum BottomNavigationTab {

    HOME(R.drawable.tab_home_ic, R.drawable.tab_home_selected_ic, R.string.home_tab_browse, false),
    LIKES(R.drawable.tab_like_ic, R.drawable.tab_like_selected_ic, R.string.home_tab_likes, false),
    SELL(R.drawable.tab_add_ic, R.drawable.tab_add_ic, 0, true),
    ACTIVITIES(R.drawable.tab_activity_ic, R.drawable.tab_activity_selected_ic, R.string.home_tab_activity, false),
    ACCOUNT(R.drawable.tab_user_ic, R.drawable.tab_user_selected_ic, R.string.home_tab_account, false);

    public static final float SPECIAL_ITEM_WIDTH_RATIO = 1.0f;

    @DrawableRes
    private int selectedResId;
    @DrawableRes
    private int unselectedResId;
    @StringRes
    private int titleResId;
    private boolean isSpecialItem;

    BottomNavigationTab(@DrawableRes int unselectedResId,
                        @DrawableRes int selectedResId,
                        @StringRes int titleResId,
                        boolean isSpecialItem) {
        this.unselectedResId = unselectedResId;
        this.selectedResId = selectedResId;
        this.titleResId = titleResId;
        this.isSpecialItem = isSpecialItem;
    }

    public static int getSpecialItemCount() {
        int counter = 0;
        for (BottomNavigationTab current : BottomNavigationTab.values()) {
            if (current.isSpecialItem()) {
                counter++;
            }
        }

        return counter;
    }

    public boolean isSpecialItem() {
        return isSpecialItem;
    }

    public int getUnselectedResId() {
        return unselectedResId;
    }

    public int getSelectedResId() {
        return selectedResId;
    }

    public int getTitleResId() {
        return titleResId;
    }
}
