package com.nearcompany.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nearcompany.R;
import com.nearcompany.model.BasicCompany;
import com.nearcompany.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CompaniesAdapter adapter;

    private List<BasicCompany> mCompanyPages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setmCompanyPages();

        initCollapsingToolbar();

        recyclerView = findViewById(R.id.recycler_view);

        adapter = new CompaniesAdapter(this, mCompanyPages);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        Utility utility = Utility.getInstance(this);
        utility.generateHashkey();
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("NEAR COMPANY");
                    collapsingToolbar.setCollapsedTitleGravity(Gravity.CENTER);
                    collapsingToolbar.setBackgroundColor(Color.BLACK);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void setmCompanyPages() {
        mCompanyPages = new ArrayList<>();
        mCompanyPages.add(new BasicCompany("Teknopark İstanbul", "Teknopark İStanbul Firmaları", "https://www.teknoparkistanbul.com.tr/images/teknoparklogo.png"));
        mCompanyPages.add(new BasicCompany("KOU Teknopark", "KOU Teknopark Firmaları", "https://kouteknopark.com//images/logo.png"));
        mCompanyPages.add(new BasicCompany("ARI Teknokent", "ARI Teknokent Firmaları", "https://www.ariteknokent.com.tr/assets/img/itu-ari-logo.png"));
        mCompanyPages.add(new BasicCompany("Ulutek", "Ulutek Firmaları", "https://www.ulutek.com.tr/images/logos/ulutek_v2_logo.png"));
        mCompanyPages.add(new BasicCompany("Teknopark Ankara", "Teknopark Ankara Firmaları", "http://www.teknoparkankara.com.tr/images/yenilogo.png"));
        mCompanyPages.add(new BasicCompany("OSTİM Teknopark", "OSTİM Teknopark Firmaları", "https://www.ostimteknopark.com.tr/content/upload/settings/ekologo-20180623093920.png"));
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private class CompaniesAdapter extends RecyclerView.Adapter<CompaniesAdapter.MyViewHolder> {

        private Context mContext;
        private List<BasicCompany> companies;

        public CompaniesAdapter(Context mContext, List<BasicCompany> companies) {
            this.mContext = mContext;
            this.companies = companies;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.company_card, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            BasicCompany company = companies.get(position);
            holder.title.setText(company.name);
            holder.desc.setText(company.desc);

            // loading album cover using Glide library
            Glide.with(mContext).load(company.logo).into(holder.logo);

            holder.logo.setOnClickListener(view -> {
                showPopupMenu(holder);
            });
        }

        @Override
        public int getItemCount() {
            return companies.size();
        }

        private void showPopupMenu(MyViewHolder holder) {
            // inflate menu
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Are You Sure?");
            builder.setIcon(R.drawable.logo);
            builder.setMessage("Your company database is refreshing with " + holder.title.getText() + ".");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Clicked Positive Button", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Clicked Negative Button", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, desc;
            public ImageView logo;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                desc = (TextView) view.findViewById(R.id.desc);
                logo = (ImageView) view.findViewById(R.id.logo);
            }
        }
    }
}
