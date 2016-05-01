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

package io.github.yavski.fabspeeddial;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.github.yavski.fabmenu.R;

/**
 * Created by yavorivanov on 01/01/2016.
 */
@CoordinatorLayout.DefaultBehavior(FabSpeedDialBehaviour.class)
public class FabSpeedDial extends LinearLayout implements View.OnClickListener {

    /**
     * Called to notify of close and selection changes.
     */
    public interface MenuListener {

        /**
         * Called just before the menu items are about to become visible.
         * Don't block as it's called on the main thread.
         *
         * @param navigationMenu The menu containing all menu items.
         * @return You must return true for the menu to be displayed;
         * if you return false it will not be shown.
         */
        boolean onPrepareMenu(NavigationMenu navigationMenu);

        /**
         * Called when a menu item is selected.
         *
         * @param menuItem The menu item that is selected
         * @return whether the menu item selection was handled
         */
        boolean onMenuItemSelected(MenuItem menuItem);

        void onMenuClosed();
    }

    private static final String TAG = FabSpeedDial.class.getSimpleName();

    private static final int VSYNC_RHYTHM = 16;

    public static final FastOutSlowInInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR =
            new FastOutSlowInInterpolator();

    public static final int BOTTOM_END = 0;
    public static final int BOTTOM_START = 1;
    public static final int TOP_END = 2;
    public static final int TOP_START = 3;
    private static final int DEFAULT_MENU_POSITION = BOTTOM_END;

    private MenuListener menuListener;
    private NavigationMenu navigationMenu;
    private Map<FloatingActionButton, MenuItem> fabMenuItemMap;
    private Map<CardView, MenuItem> cardViewMenuItemMap;

    private LinearLayout menuItemsLayout;
    FloatingActionButton fab;
    private View touchGuard = null;

    private int menuId;
    private int fabGravity;
    private Drawable fabDrawable;
    private ColorStateList fabDrawableTint;
    private ColorStateList fabBackgroundTint;
    private ColorStateList miniFabDrawableTint;
    private ColorStateList miniFabBackgroundTint;
    private ColorStateList miniFabTitleBackgroundTint;
    private boolean miniFabTitlesEnabled;
    private int miniFabTitleTextColor;
    private Drawable touchGuardDrawable;
    private boolean useTouchGuard;
    private int customMenuItemLayoutEnd;
    private int customMenuItemLayoutStart;

    private boolean isAnimating;

    private FabSpeedDial(Context context) {
        super(context);
    }

    public FabSpeedDial(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FabSpeedDial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FabSpeedDial, 0, 0);
        resolveCompulsoryAttributes(typedArray);
        resolveOptionalAttributes(typedArray);
        typedArray.recycle();

        if (isGravityBottom()) {
            LayoutInflater.from(context).inflate(R.layout.fab_speed_dial_bottom, this, true);
        } else {
            LayoutInflater.from(context).inflate(R.layout.fab_speed_dial_top, this, true);
        }

        if (isGravityEnd()) {
            setGravity(Gravity.END);
        }

        menuItemsLayout = (LinearLayout) findViewById(R.id.menu_items_layout);

        setOrientation(VERTICAL);

        newNavigationMenu();

        int menuItemCount = navigationMenu.size();
        fabMenuItemMap = new HashMap<>(menuItemCount);
        cardViewMenuItemMap = new HashMap<>(menuItemCount);
    }

    private void resolveCompulsoryAttributes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.FabSpeedDial_fabMenu)) {
            menuId = typedArray.getResourceId(R.styleable.FabSpeedDial_fabMenu, 0);
        } else {
            throw new AndroidRuntimeException("You must provide the id of the menu resource.");
        }

        if (typedArray.hasValue(R.styleable.FabSpeedDial_fabGravity)) {
            fabGravity = typedArray.getInt(R.styleable.FabSpeedDial_fabGravity, DEFAULT_MENU_POSITION);
        } else {
            throw new AndroidRuntimeException("You must specify the gravity of the Fab.");
        }
    }

    private void resolveOptionalAttributes(TypedArray typedArray) {
        fabDrawable = typedArray.getDrawable(R.styleable.FabSpeedDial_fabDrawable);
        if (fabDrawable == null) {
            fabDrawable = ContextCompat.getDrawable(getContext(), R.drawable.fab_add_clear_selector);
        }

        fabDrawableTint = typedArray.getColorStateList(R.styleable.FabSpeedDial_fabDrawableTint);
        if (fabDrawableTint == null) {
            fabDrawableTint = getColorStateList(R.color.fab_drawable_tint);
        }

        if (typedArray.hasValue(R.styleable.FabSpeedDial_fabBackgroundTint)) {
            fabBackgroundTint = typedArray.getColorStateList(R.styleable.FabSpeedDial_fabBackgroundTint);
        }

        miniFabBackgroundTint = typedArray.getColorStateList(R.styleable.FabSpeedDial_miniFabBackgroundTint);
        if (miniFabBackgroundTint == null) {
            miniFabBackgroundTint = getColorStateList(R.color.fab_background_tint);
        }

        miniFabDrawableTint = typedArray.getColorStateList(R.styleable.FabSpeedDial_miniFabDrawableTint);
        if (miniFabDrawableTint == null) {
            miniFabDrawableTint = getColorStateList(R.color.mini_fab_drawable_tint);
        }

        miniFabTitleBackgroundTint = typedArray.getColorStateList(R.styleable.FabSpeedDial_miniFabTitleBackgroundTint);
        if (miniFabTitleBackgroundTint == null) {
            miniFabTitleBackgroundTint = getColorStateList(R.color.mini_fab_title_background_tint);
        }

        miniFabTitlesEnabled = typedArray.getBoolean(R.styleable.FabSpeedDial_miniFabTitlesEnabled, true);


        miniFabTitleTextColor = typedArray.getColor(R.styleable.FabSpeedDial_miniFabTitleTextColor,
                ContextCompat.getColor(getContext(), R.color.title_text_color));

        touchGuardDrawable = typedArray.getDrawable(R.styleable.FabSpeedDial_touchGuardDrawable);

        useTouchGuard = typedArray.getBoolean(R.styleable.FabSpeedDial_touchGuard, true);

        customMenuItemLayoutEnd = typedArray.getResourceId(R.styleable.FabSpeedDial_menuItemLayoutEnd, 0);
        customMenuItemLayoutStart = typedArray.getResourceId(R.styleable.FabSpeedDial_menuItemLayoutStart, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        LayoutParams layoutParams =
                new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int coordinatorLayoutOffset = getResources().getDimensionPixelSize(R.dimen.coordinator_layout_offset);
        if (fabGravity == BOTTOM_END || fabGravity == TOP_END) {
            layoutParams.setMargins(0, 0, coordinatorLayoutOffset, 0);
        } else {
            layoutParams.setMargins(coordinatorLayoutOffset, 0, 0, 0);
        }
        menuItemsLayout.setLayoutParams(layoutParams);

        // Set up the client's FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(fabDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageTintList(fabDrawableTint);
        }
        if (fabBackgroundTint != null) {
            fab.setBackgroundTintList(fabBackgroundTint);
        }

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) return;

                if (isMenuOpen()) {
                    closeMenu();
                } else {
                    openMenu();
                }
            }
        });

        // Needed in order to intercept key events
        setFocusableInTouchMode(true);

        if (useTouchGuard) {
            ViewParent parent = getParent();

            touchGuard = new View(getContext());
            touchGuard.setOnClickListener(this);
            touchGuard.setWillNotDraw(true);
            touchGuard.setVisibility(GONE);

            if (touchGuardDrawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    touchGuard.setBackground(touchGuardDrawable);
                } else {
                    touchGuard.setBackgroundDrawable(touchGuardDrawable);
                }
            }

            if (parent instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) parent;
                frameLayout.addView(touchGuard, frameLayout.indexOfChild(this));
            } else if (parent instanceof CoordinatorLayout) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) parent;
                coordinatorLayout.addView(touchGuard, coordinatorLayout.indexOfChild(this));
            } else if (parent instanceof RelativeLayout) {
                ((RelativeLayout) parent).addView(
                        touchGuard, ((RelativeLayout) parent).indexOfChild(this),
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                Log.d(TAG, "touchGuard requires that the parent of this FabSpeedDialer be a FrameLayout or RelativeLayout");
            }
        }

        setOnClickListener(this);
    }

    private void newNavigationMenu() {
        navigationMenu = new NavigationMenu(getContext());
        new SupportMenuInflater(getContext()).inflate(menuId, navigationMenu);

        navigationMenu.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                return menuListener != null && menuListener.onMenuItemSelected(item);
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        fab.setSelected(false);
        removeFabMenuItems();

        if (menuListener != null) {
            if (v == this || v == touchGuard) {
                menuListener.onMenuClosed();
            } else if (v instanceof FloatingActionButton) {
                menuListener.onMenuItemSelected(fabMenuItemMap.get(v));
            } else if (v instanceof CardView) {
                menuListener.onMenuItemSelected(cardViewMenuItemMap.get(v));
            }
        } else {
            Log.d(TAG, "You haven't provided a MenuListener.");
        }
    }

    public void setMenuListener(MenuListener menuListener) {
        this.menuListener = menuListener;
    }

    public boolean isMenuOpen() {
        return menuItemsLayout.getChildCount() > 0;
    }

    public void openMenu() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;
        requestFocus();

        boolean showMenu = true;
        if (menuListener != null) {
            newNavigationMenu();
            showMenu = menuListener.onPrepareMenu(navigationMenu);
        }

        if (showMenu) {
            addMenuItems();
            fab.setSelected(true);
        } else {
            fab.setSelected(false);
        }
    }

    public void closeMenu() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;

        if (isMenuOpen()) {
            fab.setSelected(false);
            removeFabMenuItems();
            if (menuListener != null) {
                menuListener.onMenuClosed();
            }
        }
    }

    public void show() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;
        fab.show();
    }

    public void hide() {
        if (!ViewCompat.isAttachedToWindow(this))
            return;

        if (isMenuOpen()) {
            closeMenu();
        }
        fab.hide();
    }

    private void addMenuItems() {
        ViewCompat.setAlpha(menuItemsLayout, 1f);
        for (int i = 0; i < navigationMenu.size(); i++) {
            MenuItem menuItem = navigationMenu.getItem(i);
            if (menuItem.isVisible()) {
                menuItemsLayout.addView(createFabMenuItem(menuItem));
            }
        }
        animateFabMenuItemsIn();
    }

    private View createFabMenuItem(MenuItem menuItem) {
        ViewGroup fabMenuItem = (ViewGroup) LayoutInflater.from(getContext())
                .inflate(getMenuItemLayoutId(), this, false);

        FloatingActionButton miniFab = (FloatingActionButton) fabMenuItem.findViewById(R.id.mini_fab);
        CardView cardView = (CardView) fabMenuItem.findViewById(R.id.card_view);
        TextView titleView = (TextView) fabMenuItem.findViewById(R.id.title_view);

        fabMenuItemMap.put(miniFab, menuItem);
        cardViewMenuItemMap.put(cardView, menuItem);

        miniFab.setImageDrawable(menuItem.getIcon());
        miniFab.setOnClickListener(this);
        cardView.setOnClickListener(this);

        ViewCompat.setAlpha(miniFab, 0f);
        ViewCompat.setAlpha(cardView, 0f);

        final CharSequence title = menuItem.getTitle();
        if (!TextUtils.isEmpty(title) && miniFabTitlesEnabled) {
            cardView.setCardBackgroundColor(miniFabTitleBackgroundTint.getDefaultColor());
            titleView.setText(title);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setTextColor(miniFabTitleTextColor);
        } else {
            fabMenuItem.removeView(cardView);
        }

        miniFab.setBackgroundTintList(miniFabBackgroundTint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            miniFab.setImageTintList(miniFabDrawableTint);
        }

        return fabMenuItem;
    }

    private void removeFabMenuItems() {
        if (touchGuard != null) touchGuard.setVisibility(GONE);

        ViewCompat.animate(menuItemsLayout)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .alpha(0f)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        super.onAnimationStart(view);
                        isAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        menuItemsLayout.removeAllViews();
                        isAnimating = false;
                    }
                })
                .start();
    }

    private void animateFabMenuItemsIn() {
        if (touchGuard != null) touchGuard.setVisibility(VISIBLE);

        final int count = menuItemsLayout.getChildCount();

        if (isGravityBottom()) {
            for (int i = count - 1; i >= 0; i--) {
                final View fabMenuItem = menuItemsLayout.getChildAt(i);
                animateViewIn(fabMenuItem.findViewById(R.id.mini_fab), Math.abs(count - 1 - i));
                View cardView = fabMenuItem.findViewById(R.id.card_view);
                if (cardView != null) {
                    animateViewIn(cardView, Math.abs(count - 1 - i));
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                final View fabMenuItem = menuItemsLayout.getChildAt(i);
                animateViewIn(fabMenuItem.findViewById(R.id.mini_fab), i);
                View cardView = fabMenuItem.findViewById(R.id.card_view);
                if (cardView != null) {
                    animateViewIn(cardView, i);
                }
            }
        }
    }

    private void animateViewIn(final View view, int position) {
        final float offsetY = getResources().getDimensionPixelSize(R.dimen.keyline_1);

        ViewCompat.setScaleX(view, 0.25f);
        ViewCompat.setScaleY(view, 0.25f);
        ViewCompat.setY(view, ViewCompat.getY(view) + offsetY);

        ViewCompat.animate(view)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .scaleX(1f)
                .scaleY(1f)
                .translationYBy(-offsetY)
                .alpha(1f)
                .setStartDelay(4 * position * VSYNC_RHYTHM)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        super.onAnimationStart(view);
                        isAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        isAnimating = false;
                    }
                })
                .start();
    }

    private int getMenuItemLayoutId() {
        if (isGravityEnd()) {
            return (customMenuItemLayoutEnd != 0)? customMenuItemLayoutEnd : R.layout.fab_menu_item_end;
        } else {
            return (customMenuItemLayoutStart != 0)? customMenuItemLayoutStart : R.layout.fab_menu_item_start;
        }
    }

    private boolean isGravityBottom() {
        return fabGravity == BOTTOM_END || fabGravity == BOTTOM_START;
    }

    private boolean isGravityEnd() {
        return fabGravity == BOTTOM_END || fabGravity == TOP_END;
    }

    private ColorStateList getColorStateList(int colorRes) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int color = ContextCompat.getColor(getContext(), colorRes);

        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (isMenuOpen()
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP
                && event.getRepeatCount() == 0) {
            closeMenu();
            return true;
        }

        return super.dispatchKeyEventPreIme(event);
    }

}