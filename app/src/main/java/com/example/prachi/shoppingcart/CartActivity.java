package com.example.prachi.shoppingcart;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CartActivity extends AppCompatActivity {

    private List<App.ItemData> mCardContent;

    private RecyclerView.LayoutManager mItemListLayoutManager;
    private ItemsListAdapter mAdapter;
    private FloatingActionButton mFloatingActionButton;

    private RecyclerView getItemsRecycleView() {
        return (RecyclerView) findViewById(R.id.items_list);
    }

    private TextView getCardMessage() {
        return (TextView) findViewById(R.id.card_message);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // ADD THESE LINES
                Bundle params = new Bundle();
                params.putLong( Param.VALUE, getCartBalance());
                FirebaseAnalytics.getInstance(CartActivity.this).logEvent(Event.ECOMMERCE_PURCHASE, params);
                Snackbar.make(view, "Checking out...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AsyncTask<Void, Void, Void> _ = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                App.clearCart();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFloatingActionButton.setVisibility(View.GONE);
                                        Snackbar.make(
                                                view, "Checking completed!", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                NavUtils.navigateUpFromSameTask(CartActivity.this);
                                            }
                                        }, TimeUnit.SECONDS.toMillis(1));
                                    }
                                });
                                return null;
                            }
                        }.execute();
                    }
                }, TimeUnit.SECONDS.toMillis(2)); // Delay for 2 seconds
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCardContent = App.getCardContent();

        RecyclerView recyclerView = getItemsRecycleView();
        recyclerView.setHasFixedSize(true);
        mItemListLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mItemListLayoutManager);
        mAdapter = new ItemsListAdapter(mCardContent, true);
        recyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateCardInfo();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateCardInfo();
            }
        });
        updateCardInfo();
    }
    private long getCartBalance() {
        long credits = 0;
        for (App.ItemData item : mAdapter.mDataSet) credits += item.mPrice;
        return credits;
    }

    private void updateCardInfo() {
        if (mAdapter.getItemCount() == 0) {
            getCardMessage().setText("Cart is empty");
            mFloatingActionButton.setVisibility(View.GONE);
        } else {
            getCardMessage().setText("Cart total " + getCartBalance() + " credits");
            mFloatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CartActivity.class);
        context.startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
