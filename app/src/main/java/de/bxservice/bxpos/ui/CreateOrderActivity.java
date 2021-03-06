/**********************************************************************
 * This file is part of FreiBier POS                                   *
 *                                                                     *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - Bx Service GmbH                                      *
 **********************************************************************/
package de.bxservice.bxpos.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import de.bxservice.bxpos.R;
import de.bxservice.bxpos.logic.model.idempiere.DefaultPosData;
import de.bxservice.bxpos.logic.model.pos.NewOrderGridItem;
import de.bxservice.bxpos.logic.model.pos.POSOrder;
import de.bxservice.bxpos.logic.model.idempiere.MProduct;
import de.bxservice.bxpos.logic.model.idempiere.Table;
import de.bxservice.bxpos.logic.model.pos.PosProperties;
import de.bxservice.bxpos.ui.adapter.CreateOrderPagerAdapter;
import de.bxservice.bxpos.ui.adapter.SearchItemAdapter;
import de.bxservice.bxpos.ui.decorator.DividerItemDecoration;
import de.bxservice.bxpos.ui.dialog.MultipleItemsDialogFragment;
import de.bxservice.bxpos.ui.dialog.OverridePriceDialogFragment;
import de.bxservice.bxpos.ui.dialog.SwitchTableDialogFragment;
import de.bxservice.bxpos.ui.dialog.GuestNumberDialogFragment;
import de.bxservice.bxpos.ui.dialog.RemarkDialogFragment;
import de.bxservice.bxpos.ui.fragment.CreateOrderDetailFragment;
import de.bxservice.bxpos.ui.utilities.PreferenceActivityHelper;

public class CreateOrderActivity extends AppCompatActivity implements GuestNumberDialogFragment.GuestNumberDialogListener,
        RemarkDialogFragment.RemarkDialogListener, SwitchTableDialogFragment.SwitchTableDialogListener,
        MultipleItemsDialogFragment.MultipleItemsDialogListener, OverridePriceDialogFragment.OverridePriceDialogListener,
        SearchView.OnQueryTextListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private CreateOrderPagerAdapter mCreateOrderPagerAdapter;
    static final int PICK_CONFIRMATION_REQUEST = 1;  // The request code
    private static final String ORDER          = "createdOrder";
    public final static String CALLER_ACTIVITY = "CreateOrderActivity.CALLER";

    private PagerSlidingTabStrip tabs;

    //RecyclerView attributes for search functionality
    private RecyclerView recyclerView;
    private SearchItemAdapter mAdapter;
    private ArrayList<NewOrderGridItem> items = new ArrayList<>();
    private HashMap<NewOrderGridItem, MProduct> itemProductHashMap;
    private SearchView mSearchView;

    //order attributes
    private POSOrder posOrder = null;
    private int numberOfGuests = 0;
    private Table selectedTable = null;
    private String remarkNote = "";
    private FloatingActionButton sendActionButton;

    private String caller;
    private boolean itemsAdded = false;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    //If the app is running on a large-screen device
    private boolean mTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_order_toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Table # and # of guests
        getExtras();

        if (savedInstanceState != null) {
            posOrder = (POSOrder) savedInstanceState.getSerializable(ORDER);
        }

        if("EditOrderActivity".equals(caller))
            setTitle(R.string.title_activity_add_items);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mCreateOrderPagerAdapter = new CreateOrderPagerAdapter(getSupportFragmentManager(), getBaseContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.createOrderContainer);
        mViewPager.setAdapter(mCreateOrderPagerAdapter);


        recyclerView = (RecyclerView) findViewById(R.id.search_item_view);

        initSearchListItems();

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SearchItemAdapter(items);
        mAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = recyclerView.getChildAdapterPosition(v);

                NewOrderGridItem selectedItem = mAdapter.getSelectedItem(position);
                addOrderItem(itemProductHashMap.get(selectedItem));

                mCreateOrderPagerAdapter.updateQty(selectedItem, getProductQtyOrdered(itemProductHashMap.get(selectedItem)));
                onBackPressed();
            }
        });

        recyclerView.setAdapter(mAdapter);
        recyclerView.setVisibility(View.GONE);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));


        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);

        sendActionButton = (FloatingActionButton) findViewById(R.id.fab);
        sendActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (posOrder == null)
                    Snackbar.make(view, R.string.empty_order, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else
                    openConfirmationActivity();
            }
        });

        ViewGroup fragmentContainer = (ViewGroup) findViewById(R.id.detail_order_fragment);
        mTablet = (fragmentContainer != null);

        if (posOrder != null) {
            remarkNote = posOrder.getOrderRemark();
            if (mTablet) {
                updateCreateOrderDetailFragment();
            }
        }

    }

    /**
     * Creates the list view that will be used when click on search
     */
    private void initSearchListItems() {

        NumberFormat currencyFormat = PosProperties.getInstance().getCurrencyFormat();

        NewOrderGridItem gridItem;
        itemProductHashMap = new HashMap<>();

        for (MProduct product : MProduct.getSoldProducts(getBaseContext())) {
            gridItem = new NewOrderGridItem();

            //If the key and the name are the same, don't repeat
            if (product.getProductName().equalsIgnoreCase(product.getProductKey()))
                gridItem.setName(product.getProductName());
            else
                gridItem.setName(product.getProductKey() + " " + product.getProductName());

            if (!product.askForPrice())
                gridItem.setPrice(currencyFormat.format(product.getProductPriceValue()));
            else
                gridItem.setPrice("");

            gridItem.setKey(product.getProductKey());

            items.add(gridItem);
            itemProductHashMap.put(gridItem, product);
        }
    }

    /**
     * Method that allows to capture the text that is being
     * introduced in the search field
     * @param newText
     * @return
     */
    public boolean onQueryTextChange(String newText) {
        SearchItemAdapter adapter = (SearchItemAdapter) recyclerView.getAdapter();

        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
        } else {
            showSearchList(true);
            adapter.getFilter().filter(newText);
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * When the search button is pressed - the listview is shown and the tabs are hidden
     * When the search button loses focuses - listview hidden and tabs are shown
     * @param show
     */
    private void showSearchList(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewPager.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            tabs.setVisibility(show ? View.GONE : View.VISIBLE);
            tabs.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tabs.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            if (sendActionButton.getVisibility() == View.VISIBLE)
                sendActionButton.hide();
            else if (!show) //Show only when the search list is not shown
                sendActionButton.show();

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            tabs.setVisibility(show ? View.GONE : View.VISIBLE);
            if (sendActionButton.getVisibility() == View.VISIBLE)
                sendActionButton.hide();
            else if (!show) //Show only when the search list is not shown
                sendActionButton.show();
        }
    }//showSearchList

    /**
     * Get extras from the previous activity
     * - Table number
     * - Number of guests
     */
    private void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            caller = getIntent().getStringExtra(CALLER_ACTIVITY);

            if ("MainActivity".equals(caller)) {
                if(getIntent().getSerializableExtra(MainActivity.EXTRA_ASSIGNED_TABLE) != null)
                    selectedTable = (Table) getIntent().getSerializableExtra(MainActivity.EXTRA_ASSIGNED_TABLE);

                numberOfGuests = extras.getInt(MainActivity.EXTRA_NUMBER_OF_GUESTS);
            } else if ("EditOrderActivity".equals(caller)) {
                posOrder = (POSOrder) getIntent().getSerializableExtra(EditOrderActivity.EXTRA_ORDER);
                selectedTable = posOrder.getTable();
                numberOfGuests = posOrder.getGuestNumber();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_order_activity, menu);

        MenuItem guestItem = menu.findItem(R.id.set_guests);

        if (guestItem != null) {
            if (!DefaultPosData.get(getBaseContext()).isShowGuestDialog())
                guestItem.setVisible(false);
        }

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView = (SearchView) menu.findItem(R.id.items_search).getActionView();

        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        setupSearchView();

        return true;
    }

    /**
     * Add the needed configuration to the search view
     */
    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSearchList(true);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.set_guests) {
            showGuestNumberDialog();
            return true;
        }
        if (id == R.id.add_note) {
            showRemarkDialog();
            return true;
        }
        if (id == R.id.change_table) {
            showTransferTableDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRemarkDialog() {
        RemarkDialogFragment remarkDialog = new RemarkDialogFragment();
        remarkDialog.setNote(remarkNote);
        remarkDialog.show(getFragmentManager(), "RemarkDialogFragment");
    }

    private void showGuestNumberDialog() {
        // Create an instance of the dialog fragment and show it
        GuestNumberDialogFragment guestDialog = new GuestNumberDialogFragment();
        guestDialog.setNumberOfGuests(numberOfGuests);
        guestDialog.show(getFragmentManager(), "NumberOfGuestDialogFragment");
    }

    private void showTransferTableDialog() {
        SwitchTableDialogFragment changeTableDialog = new SwitchTableDialogFragment();
        changeTableDialog.show(getFragmentManager(), "SwitchTableDialogFragment");
    }

    private void showAddMultipleItemsDialog(MProduct product) {
        MultipleItemsDialogFragment multipleItemsDialog = new MultipleItemsDialogFragment();
        multipleItemsDialog.setProduct(product);
        multipleItemsDialog.setNoItems(getProductQtyOrdered(product));
        multipleItemsDialog.show(getFragmentManager(), "MultipleItemsDialogFragment");
    }

    private void showSetPriceDialog(MProduct product) {
        OverridePriceDialogFragment priceDialogFragment = new OverridePriceDialogFragment();
        priceDialogFragment.setProduct(product);
        priceDialogFragment.show(getFragmentManager(), "OverridePriceDialogFragment");
    }

    /**
     * Click set on guest number dialog
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(GuestNumberDialogFragment dialog) {
        // User touched the dialog's positive button
        numberOfGuests = dialog.getNumberOfGuests();
        updateDraftOrder();
    }

    /**
     * Click add on add remark dialog
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(RemarkDialogFragment dialog) {
        // User touched the dialog's positive button
        remarkNote = dialog.getNote();
        updateDraftOrder();
    }

    /**
     * Click add on Change table dialog
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(SwitchTableDialogFragment dialog) {
        selectedTable = dialog.getTable();
        updateDraftOrder();
    }

    @Override
    public void onDialogPositiveClick(MultipleItemsDialogFragment dialog) {

        int orderedQty = dialog.getNoItems();

        //Substract the lines that were previously added by single click
        int additionalItems = orderedQty - getProductQtyOrdered(dialog.getProduct());

        boolean itemsAdded = additionalItems > 0;

        while (additionalItems > 0) {
            addOrderItem(dialog.getProduct());
            additionalItems = additionalItems -1;
        }

        if (itemsAdded) {
            updateOrderLinesQuantity();
        }

    }

    @Override
    public void onDialogPositiveClick(OverridePriceDialogFragment dialog) {
        if (dialog.getOverridePrice() != null) {
            posOrder.addItem(dialog.getProduct(), dialog.getOverridePrice(), getBaseContext());
            updateOrderLinesQuantity();
        }
        dialog.dismiss();
    }

    public void addOrderItem(MProduct product) {

        if(posOrder == null)
            createOrder();

        //If the product price is zero and the price limit > 0
        if (product.askForPrice()) {
            showSetPriceDialog(product);
        } else {
            posOrder.addItem(product, getBaseContext());
            if (mTablet)
                updateCreateOrderDetailFragment();
        }
    }

    public void addMultipleItems(MProduct product) {

        if(posOrder == null)
            createOrder();

        showAddMultipleItemsDialog(product);
    }

    private void createOrder() {
        posOrder = new POSOrder();
        posOrder.setGuestNumber(numberOfGuests);
        posOrder.setOrderRemark(remarkNote);
        posOrder.setTable(selectedTable);
        posOrder.setStatus(POSOrder.DRAFT_STATUS);
        posOrder.createOrder(getBaseContext());
    }

    private void updateDraftOrder() {

        if(posOrder != null) {

            if(!remarkNote.equals(posOrder.getOrderRemark())) {
                posOrder.setOrderRemark(remarkNote);
            }
            if(numberOfGuests != posOrder.getGuestNumber()) {
                posOrder.setGuestNumber(numberOfGuests);
            }
            if(selectedTable != null && !selectedTable.equals(posOrder.getTable())) {
                //If a table was selected before
                if(posOrder.getTable() != null)
                    posOrder.getTable().freeTable(getBaseContext(), true);
                posOrder.setTable(selectedTable);
                selectedTable.setServerName(posOrder.getServerName(getBaseContext()));
                selectedTable.occupyTable(getBaseContext(), true);
            }

            posOrder.updateOrder(getBaseContext());
        }

    }

    public int getProductQtyOrdered(MProduct product) {
        if(posOrder == null)
            return 0;
        return posOrder.getProductQtyOrdered(product);
    }


    @Override
    /**
     * When the back button is pressed and something
     * was ordered show a confirmation dialog
     */
    public void onBackPressed() {

        //If the search view mode is displayed, only go back to the tab
        if (recyclerView.isShown()) {
            mSearchView.onActionViewCollapsed();
            showSearchList(false);
        }
        else if (posOrder != null && "MainActivity".equals(caller)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.discard_draft_order)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            if(posOrder.remove(getBaseContext())) {
                                //If the order is discarded -> the preference value goes back to what it was
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(PreferenceActivityHelper.KEY_ORDER_NUMBER, posOrder.getOrderNo());
                                editor.apply();

                                CreateOrderActivity.super.onBackPressed();
                            }
                        }
                    }).create().show();
        }
        else
            super .onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Opens the confirmation activity passing the draft order
     * as parameter
     */
    private void openConfirmationActivity() {
        if("MainActivity".equals(caller)) {
            Intent intent = new Intent(this, EditOrderActivity.class);
            intent.putExtra(EditOrderActivity.DRAFT_ORDER, posOrder);
            intent.putExtra(EditOrderActivity.CALLER_ACTIVITY, "CreateOrderActivity");
            startActivityForResult(intent, PICK_CONFIRMATION_REQUEST);
        }
        else if ("EditOrderActivity".equals(caller)) {
            itemsAdded = true;
            onBackPressed();
        }
    }

    /**
     * Refresh the quantity ordered on the product items buttons
     */
    private void updateOrderLinesQuantity() {
        mCreateOrderPagerAdapter.refreshAllQty();
        if (mTablet)
            updateCreateOrderDetailFragment();
    }

    private void updateCreateOrderDetailFragment() {
        CreateOrderDetailFragment fragment =
                CreateOrderDetailFragment.newInstance(posOrder);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_order_fragment, fragment)
                .commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == PICK_CONFIRMATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                posOrder = (POSOrder)data.getExtras().get(EditOrderActivity.EXTRA_ORDER);
                updateOrderLinesQuantity();
            }
            //Everything ok - the order was created and the create Activity should be closed
            if (resultCode == 2) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void finish() {
        if ("EditOrderActivity".equals(caller) && itemsAdded) {
            Intent data = new Intent();
            data.putExtra(EditOrderActivity.EXTRA_ORDER, posOrder);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(ORDER, posOrder);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


}
