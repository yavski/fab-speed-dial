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
import android.support.v7.widget.SwitchCompat;
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
                }

                setMenuItemTitle(navigationMenu, R.id.action_call, input);
                setMenuItemTitle(navigationMenu, R.id.action_text, input);
                setMenuItemTitle(navigationMenu, R.id.action_email, input);

                setMenuItemVisibility(navigationMenu, R.id.action_call, R.id.menu_item_call_switch);
                setMenuItemVisibility(navigationMenu, R.id.action_text, R.id.menu_item_text_switch);
                setMenuItemVisibility(navigationMenu, R.id.action_email, R.id.menu_item_email_switch);

                return true;
            }
        });

    }

    private void setMenuItemTitle(NavigationMenu menu, int menuItemId, CharSequence input) {
        MenuItem menuItem = menu.findItem(menuItemId);
        String oldMenuItemTitle = menuItem.getTitle().toString();
        menuItem.setTitle(oldMenuItemTitle + " " + input);
    }

    private void setMenuItemVisibility(NavigationMenu menu, int menuItemId, int switchItem) {
        SwitchCompat switchView = (SwitchCompat) findViewById(switchItem);
        menu.findItem(menuItemId).setVisible(switchView.isChecked());
    }

}
