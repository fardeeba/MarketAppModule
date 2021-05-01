package com.skylarksit.module.libs.alertdialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.skylarksit.module.R;
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.utils.IClickListener;
import com.skylarksit.module.ui.utils.ItemClickListener;
import com.skylarksit.module.ui.utils.OnOneOffClickListener;

import java.util.List;

public class NokListAlertDialog<T> extends NokAlertDialogBuilder<NokListAlertDialog> {

    private int layout = R.layout.list_popup_item;
    private final RecyclerView listView;
    private SearchResultAdapter adapter;
    private final TextView cancelButton;
    private final Button actionButton;
    private IClickListener actionButtonHandler;

    {
        listView = findView(R.id.list);
        cancelButton = findView(R.id.cancelButton);
        actionButton = findView(R.id.actionButton);
        cancelButton.setOnClickListener(view -> hide());

        actionButton.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                actionButtonHandler();
            }
        });
    }

    private void actionButtonHandler() {
        if (actionButtonHandler != null) {
            actionButtonHandler.click(null);
            hide();
        }
    }

    public void setActionButton(@StringRes int text, IClickListener handler) {
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText(text);
        this.actionButtonHandler = handler;
    }

    public NokListAlertDialog(Context context, @LayoutRes int res) {
        super(context);

        setTitleColor(R.color.white);
        this.layout = res;
    }

    public NokListAlertDialog(Context context) {
        super(context);

        setTitleColor(R.color.white);
    }

    @Override
    protected int getLayout() {
        return R.layout.nok_list_alert_layout;
    }

    public void setItems(List<IListItem> items) {
        adapter = new SearchResultAdapter(getContext(), items, false, false, layout);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public IClickListener onHideListener;

    @Override
    public void hide() {
        super.hide();

        if (onHideListener != null) {
            onHideListener.click(null);
        }
    }

    public NokListAlertDialog setItemClickListener(final NokListClickListener<T> confirmClickListener) {
        if (confirmClickListener != null) {
            adapter.setClickListener(new ItemClickListener<T>(adapter) {
                @Override
                public void onClick(T item, int position) {
                    confirmClickListener.onClick(item);
                    hide();
                }
            });
        }

        return this;
    }

    public void setCancelText(String text) {
        cancelButton.setText(text);
    }

    public interface NokListClickListener<T> {
        void onClick(T object);
    }
}
