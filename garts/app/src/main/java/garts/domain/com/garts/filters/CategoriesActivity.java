package garts.domain.com.garts.filters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import garts.domain.com.garts.R;
import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ToastUtils;

public class CategoriesActivity extends BaseActivity {

    public static final String SELECTED_CATEGORY_EXTRA_KEY = "SELECTED_CATEGORY_EXTRA_KEY";
    public static final String SELECTED_SUBCATEGORY_EXTRA_KEY = "SELECTED_SUBCATEGORY_EXTRA_KEY";
    public static final String HAS_FILTER_ROLE_EXTRA_KEY = "HAS_FILTER_ROLE_EXTRA_KEY";

    private static final int SUBCATEGORIES_REQ_CODE = 1;

    private ImageView backIV;
    private ImageView doneIV;
    private ProgressBar subcategoriesLoadingPB;
    private RecyclerView categoriesRV;

    private List<String> categories;
    private String selectedCategory = "";
    private String selectedSubcategory = "";

    private boolean hasFilterRole = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        selectedCategory = getIntent().getStringExtra(SELECTED_CATEGORY_EXTRA_KEY);
        hasFilterRole = getIntent().getBooleanExtra(HAS_FILTER_ROLE_EXTRA_KEY, true);
        if (selectedCategory == null) {
            selectedCategory = "";
        }

        initViews();

        // add "All" to categories array
        categories = new ArrayList<>();
        if (hasFilterRole) {
            categories.add(0, getString(R.string.categories_all_title));
        }

        // Call query
        queryCategories();
        setUpViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SUBCATEGORIES_REQ_CODE && resultCode == Activity.RESULT_OK) {
            selectedSubcategory = data.getStringExtra(SubcategoriesActivity.IN_OUT_SELECTED_SUBCATEGORY_EXTRA_KEY);
            setSelectionResult();
        }
    }

    private void initViews() {
        backIV = findViewById(R.id.ac_back_iv);
        doneIV = findViewById(R.id.ac_done_iv);
        subcategoriesLoadingPB = findViewById(R.id.ac_subcategories_loading_pb);
        categoriesRV = findViewById(R.id.ac_categories_rv);
    }

    private void setUpViews() {
        // MARK: - DONE BUTTON ------------------------------------
        doneIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(selectedCategory)) {
                    setSelectionResult();
                } else {
                    ToastUtils.showMessage(getString(R.string.categories_done_error));
                }
            }
        });

        // MARK: - CANCEL BUTTON ------------------------------------
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categories.clear();
                finish();
            }
        });
    }

    private void setSelectionResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(SELECTED_CATEGORY_EXTRA_KEY, selectedCategory);
        resultIntent.putExtra(SELECTED_SUBCATEGORY_EXTRA_KEY, selectedSubcategory);
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    // MARK: - QUERY CATEGORIES ---------------------------------------------------------------
    void queryCategories() {
        showLoading();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CATEGORIES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    hideLoading();

                    for (int i = 0; i < objects.size(); i++) {
                        // Get Parse object
                        ParseObject cObj = objects.get(i);
                        categories.add(cObj.getString(Configs.CATEGORIES_CATEGORY));
                    }

                    categoriesRV.setLayoutManager(new LinearLayoutManager(CategoriesActivity.this));
                    categoriesRV.setAdapter(new FilterAdapter(categories, selectedCategory,
                            new FilterAdapter.OnFilterSelectedListener() {
                                @Override
                                public void onFilterSelected(String filter) {
                                    selectedCategory = filter;
                                    Log.i("log-", "SELECTED CATEGORY: " + selectedCategory);

                                    if (hasFilterRole) {
                                        querySubcategories();
                                    }
                                }
                            }));
                    // Error in query
                } else {
                    hideLoading();
                    ToastUtils.showMessage(error.getMessage());
                }
            }
        });
    }

    private void querySubcategories() {
        doneIV.setVisibility(View.GONE);
        subcategoriesLoadingPB.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.SUBCATEGORIES_CLASS_NAME);
        query.whereEqualTo(Configs.SUBCATEGORIES_CATEGORY, selectedCategory);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                hideSubcategoriesLoading();

                if (e == null) {
                    startSubcategoriesScreen(objects);
                } else {
                    ToastUtils.showMessage(e.getMessage());
                }
            }
        });
    }

    private void hideSubcategoriesLoading() {
        doneIV.setVisibility(View.VISIBLE);
        subcategoriesLoadingPB.setVisibility(View.GONE);
    }

    private void startSubcategoriesScreen(List<ParseObject> subcategoriesObjects) {
        if (subcategoriesObjects == null || subcategoriesObjects.isEmpty()) {
            return;
        }

        ArrayList<String> subcategories = new ArrayList<>();
        for (int i = 0; i < subcategoriesObjects.size(); i++) {
            ParseObject cObj = subcategoriesObjects.get(i);
            subcategories.add(cObj.getString(Configs.SUBCATEGORIES_SUBCATEGORY));
        }

        Intent subcategoriesIntent = new Intent(this, SubcategoriesActivity.class);
        subcategoriesIntent.putStringArrayListExtra(SubcategoriesActivity.IN_SUBCATEGORIES_EXTRA_KEY, subcategories);
        subcategoriesIntent.putExtra(SubcategoriesActivity.IN_INCLUDE_ALL_SUBCATEGORY_EXTRA_KEY, true);
        startActivityForResult(subcategoriesIntent, SUBCATEGORIES_REQ_CODE);
    }
}
