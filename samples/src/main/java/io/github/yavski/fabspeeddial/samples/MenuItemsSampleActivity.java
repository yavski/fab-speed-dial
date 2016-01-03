/*
 * Copyright 2016 Yavor Ivanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.yavski.fabspeeddial.samples;

import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;

import io.github.yavski.fabmenu.samples.R;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MenuItemsSampleActivity extends BaseSampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_items_sample);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText inputView = (EditText) findViewById(R.id.puppy_name);

        FabSpeedDial fabSpeedDial = ((FabSpeedDial) findViewById(R.id.fab_speed_dial));
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                String input = inputView.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    Snackbar.make(findViewById(R.id.rootView), "Enter the name of your puppy",
                            Snackbar.LENGTH_SHORT).show();
                    return false;
                } else {
                    for (int i = 0; i < navigationMenu.size(); i++) {
                        MenuItem menuItem = navigationMenu.getItem(i);
                        String oldMenuItemTitle = menuItem.getTitle().toString();
                        menuItem.setTitle(oldMenuItemTitle + " " + input);
                    }
                    return true;
                }
            }
        });

    }

}
