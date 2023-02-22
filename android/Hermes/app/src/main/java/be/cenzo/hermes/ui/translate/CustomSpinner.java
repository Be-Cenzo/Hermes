package be.cenzo.hermes.ui.translate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import be.cenzo.hermes.R;

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    private AlertDialog mPopup;

    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
            mPopup = null;
        }
    }


    //when clicked alertDialog is made
    @Override
    public boolean performClick() {
        Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence prompt = getPrompt();
        if (prompt != null) {
            builder.setTitle(prompt);
        }


        mPopup = builder.setSingleChoiceItems(
                new DropDownAdapter(getAdapter()),
                getSelectedItemPosition(), this).show();

        WindowManager.LayoutParams WMLP = mPopup.getWindow().getAttributes();

        WMLP.x = 0; // x position
        WMLP.y = 0; // y position
        WMLP.height = WindowManager.LayoutParams.WRAP_CONTENT ;
        WMLP.width = WindowManager.LayoutParams.WRAP_CONTENT;
        WMLP.horizontalMargin = 0;
        WMLP.verticalMargin = 0;

        mPopup.getWindow().setAttributes(WMLP);



        ListView listView = mPopup.getListView();
        // Remove divider between rows
        listView.setDivider(null);

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        setSelection(which);
        dialog.dismiss();
        mPopup = null;
    }


    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;

        /**
         * <p>Creates a new ListAddapter wrapper for the specified adapter.</p>
         *
         * @param adapter the Adapter to transform into a ListAdapter
         */
        public DropDownAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
        }

        public int getCount() {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        public Object getItem(int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return mAdapter == null ? null :
                    mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        /**
         * <p>Always returns false.</p>
         *
         * @return false
         */
        public boolean areAllItemsEnabled() {
            return true;
        }

        /**
         * <p>Always returns false.</p>
         *
         * @return false
         */
        public boolean isEnabled(int position) {
            return true;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }

    }
}
