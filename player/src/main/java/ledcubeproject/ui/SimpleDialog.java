package ledcubeproject.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 包裝AlertDialog，方便建立簡單的AlertDialog
 * Created by Jerry on 2017/8/5.
 */

public class SimpleDialog {

    private AlertDialog dialog;
    private Context context;
    private View view;

    public SimpleDialog() {
    }

    public SimpleDialog(Context c, View view, String title) {
        init(c, view, title);
    }

    public SimpleDialog(Context c, int viewId, String title) {
        view = LayoutInflater.from(c).inflate(viewId, null);
        init(c, view, title);
    }

    public void init(Context c, View view, String title) {
        context = c;
        this.view = view;
        dialog = (new AlertDialog.Builder(c)).setView(view).setTitle(title).create();
    }

    protected Context getContext() {
        return context;
    }

    /**
     * 顯示在螢幕上
     */
    public void show() {
        dialog.show();
    }

    /**
     * 讓AlertDialog從螢幕上消失
     */
    public void close() {
        dialog.dismiss();
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public View getView() {
        return view;
    }

}
