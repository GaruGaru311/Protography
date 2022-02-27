package com.example.protography.ui.Fragments.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.protography.MainActivity;
import com.example.protography.R;
import com.example.protography.databinding.FragmentProfileBinding;
import com.example.protography.ui.Activities.EditProfileActivity;
import com.example.protography.ui.Activities.Log.LoginActivity;
import com.example.protography.ui.Adapters.ProfileAdapter;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;


public class ProfileFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FragmentProfileBinding binding;
    private Button menu;
    private ShapeableImageView image;
    private ShapeableImageView ProfileImage;
    private DrawerLayout mDrawer;
    private TextView userName;
    private TextView userMail;
    SharedPreferences sharedPref;
    private NavigationView navigationView;
    Intent intent;
    private TextView textViewUsername;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        mDrawer = binding.drawerLayout;
        navigationView = binding.nvView;

        View header = navigationView.getHeaderView(0);

        // Controllo l'orientamento e in base a quello cambio il layout per immagine e username
        LinearLayout placeHolder = binding.layoutImage;
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getLayoutInflater().inflate(R.layout.image_name_landscape, placeHolder);
        } else {
            getLayoutInflater().inflate(R.layout.image_name_portrait, placeHolder);
        }

        // Questi elementi li posso inizializzare solo dopo che ho caricato il layout precedente
        textViewUsername = placeHolder.findViewById(R.id.text_view_username);
        textViewUsername.setText(sharedPref.getString("FULLNAME", null));
        image = placeHolder.findViewById(R.id.image);
        menu = placeHolder.findViewById(R.id.menu);
        menu.setOnClickListener(v -> mDrawer.openDrawer(GravityCompat.START));


        //header drawer
        ProfileImage = header.findViewById(R.id.imageProfile);
        userName = header.findViewById(R.id.userName);
        userMail = header.findViewById(R.id.userMail);

        Picasso.get().load(sharedPref.getString("PROFILEIMG", null)).into(image);
        Picasso.get().load(sharedPref.getString("PROFILEIMG", null)).into(ProfileImage);
        userName.setText(sharedPref.getString("FULLNAME", null));
        userMail.setText(sharedPref.getString("EMAIL", null));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        intent = new Intent(getActivity(), EditProfileActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.item2:
                        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                            case Configuration.UI_MODE_NIGHT_YES:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                Toast.makeText(getActivity(), getString(R.string.switched_to_light_theme), Toast.LENGTH_SHORT).show();
                                sharedPref.edit().putString("THEME", "LIGHT").apply();
                                break;
                            case Configuration.UI_MODE_NIGHT_NO:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                Toast.makeText(getActivity(), getString(R.string.switched_to_dark_theme), Toast.LENGTH_SHORT).show();
                                sharedPref.edit().putString("THEME", "DARK").apply();
                                break;
                        }

                        break;

                    case R.id.item3:
                        FirebaseAuth.getInstance().signOut();
                        sharedPref.edit().clear().apply();
                        getActivity().finish();
                        intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        mTabLayout = binding.tabLayout;
        // Si aggiungono i tab con il loro titolo che viene mostrato
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_upload));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_bookmarks));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager = binding.viewPager;

        ProfileAdapter adapter = new ProfileAdapter(getParentFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        Picasso.get().load(sharedPref.getString("PROFILEIMG", null)).into(image);
        Picasso.get().load(sharedPref.getString("PROFILEIMG", null)).into(ProfileImage);
        userName.setText(sharedPref.getString("FULLNAME", null));
        userMail.setText(sharedPref.getString("EMAIL", null));
        textViewUsername.setText(sharedPref.getString("FULLNAME", null));
    }
}