package io.github.yavski.fabspeeddial.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.github.yavski.fabmenu.samples.R;

/**
 * Created by yavorivanov on 03/01/2016.
 */
public abstract class BaseSampleActivity extends AppCompatActivity {

    public static final String TITLE = "title";

    private String mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getIntent().getStringExtra(TITLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.sample_activity_title, mTitle));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
