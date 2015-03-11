package com.sports.iTrack.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;

import com.sports.iTrack.activity.MainActivity;
import com.sports.iTrack.R;
import com.sports.iTrack.activity.TroopsActivity;
import com.sports.iTrack.activity.AboutActivity;
import com.sports.iTrack.activity.HistoryActivity;
import com.sports.iTrack.activity.MeActivity;
import com.sports.iTrack.activity.RoutePlanActivity;
import com.sports.iTrack.utils.PackageUtils;
import com.sports.iTrack.utils.constant;

/**
 * Created by aaron_lu on 2/12/15.
 * <p/>
 * 实现 slideMenu  和 ActionBar全局化
 */
public class BaseActivity extends Activity {

    private static int LEFT_CURRENT_POSITION = -1;
    protected RelativeLayout fullLayout;
    protected LinearLayout frameLayout;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mLeftDrawer;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;

    private int[] leftIcon = { R.drawable.ic_leftdrawer_workout,
            R.drawable.ic_leftdrawer_route, R.drawable.ic_leftdrawer_history, R.drawable.ic_leftdrawer_music,
            R.drawable.ic_leftdrawer_troops,R.drawable.ic_leftdrawer_about, R.drawable.ic_leftdrawer_about };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {

        fullLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        frameLayout = (LinearLayout) fullLayout.findViewById(R.id.content_frame);

        getLayoutInflater().inflate(layoutResID, frameLayout, true);

        super.setContentView(fullLayout);

        //Your drawer content...
        mDrawerTitle = getString(R.string.app_name);
        mTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftDrawer = (LinearLayout) findViewById(R.id.ll_left_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new MenuAdapter(this));
        //        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerList.setItemsCanFocus(false);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(
                this.getBaseContext().getResources().getDrawable(R.drawable.BackBar));
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public LinearLayout getLeftDrawer() {
        return mLeftDrawer;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawer);
        //        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.more_flow).setVisible(drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.more_flow:
                //do something
                Toast.makeText(this, "More.", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LEFT_CURRENT_POSITION = position;
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment;

        // update the main content by replacing fragments
        switch (position) {
            case constant.LOGIN_POSITION:
                PackageUtils.startActivity(this, MeActivity.class);
                break;
            case constant.RECORD_POSITION:
                PackageUtils.startActivity(this, MainActivity.class);
                break;
            case constant.PLAN_POSITION:
                PackageUtils.startActivity(this, RoutePlanActivity.class);
                break;
            case constant.HISTORY_POSITION:
                PackageUtils.startActivity(this, HistoryActivity.class);
                break;
            case constant.MUSIC_POSITION:
                PackageUtils.openApp(this, "com.tencent.qqmusic");
                break;
            case constant.TROOPS_POSITION:
                PackageUtils.startActivity(this, TroopsActivity.class);
                break;
            case constant.ABOUT_POSITON:
                PackageUtils.startActivity(this, AboutActivity.class);
                break;
            default:
                break;
        }



        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    public final class ViewHolder {
        public ImageView titleIcon;
        public TextView titleText;
        public TextView contentText;
    }

    private class MenuAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public MenuAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override public int getCount() {
            return mPlanetTitles.length;
        }

        @Override public Object getItem(int position) {
            return mPlanetTitles[position];
        }

        @Override public long getItemId(int position) {
            return 0;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if (convertView == null || convertView.getTag() == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.drawer_list_item, null);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.titleIcon = (ImageView) convertView.findViewById(R.id.image_icon);
            viewHolder.titleText = (TextView) convertView.findViewById(R.id.test1);
            viewHolder.contentText = (TextView) convertView.findViewById(R.id.text2);

            viewHolder.titleIcon.setImageResource(leftIcon[position]);
            viewHolder.titleText.setText(mPlanetTitles[position]);

            if (LEFT_CURRENT_POSITION == position) {
                viewHolder.titleText.setBackgroundColor(Color.WHITE);
                //TODO 选中后设置成绿色
                //viewHolder.titleText.setTextColor(0xff71C671);
            }
            return convertView;
        }
    }
}
