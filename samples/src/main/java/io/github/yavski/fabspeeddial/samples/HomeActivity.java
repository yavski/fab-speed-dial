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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.github.yavski.fabmenu.samples.R;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String[] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titles = getResources().getStringArray(R.array.title_array);
        final String[] descriptions = getResources().getStringArray(R.array.description_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item_2,
                R.id.text1,
                titles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

                text1.setText(titles[position]);
                text2.setText(descriptions[position]);
                return view;
            }
        };
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Class<? extends BaseSampleActivity> newActivity;
        switch (position) {
            default:
            case 0 :
                newActivity = PositionSampleActivity.class;
                break;
            case 1:
                newActivity = GroupStyleSampleActivity.class;
                break;
            case 2:
                newActivity = IndividualStyleSampleActivity.class;
                break;
            case 3:
                newActivity = EventsSampleActivity.class;
                break;
            case 4:
                newActivity = MenuItemsSampleActivity.class;
                break;
        }
        Intent i = new Intent(HomeActivity.this, newActivity);
        i.putExtra(BaseSampleActivity.TITLE, titles[position]);
        startActivity(i);
    }
}
