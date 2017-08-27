package ledcubeproject.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 包裝<code>AlertDialog</code>，其內容物為一個字串<code>ListView</code>，透過幾個簡單的方法就可以使用
 * Created by Jerry on 2017/8/4.
 */

public class ListDialog extends SimpleDialog {
    private ListView listView;
    private ListAdapter ListAdapter = new ListAdapter();
    private AdapterView.OnItemClickListener onItemClickListener;

    /**
     * @param c     呼叫方的Context
     * @param o     會作為<code>ListView.setOnItemClickListener()</code>的參數
     * @param title AlertDiolog的標題
     */
    public ListDialog(Context c, AdapterView.OnItemClickListener o, String title) {
        listView = new ListView(c);
        listView.setBackgroundColor(0xFF000000);
        listView.setAdapter(ListAdapter);
        onItemClickListener = o;
        listView.setOnItemClickListener(o);
        init(c, listView, title);
    }

    /**
     * 更新內容。建議在呼叫 show()前先行呼叫，確保顯示的列表為最新狀態
     *
     * @param strArr 列表對應的字串陣列
     */
    public void update(String[] strArr) {
        ListAdapter.setContentStrings(strArr);
        ListAdapter.notifyDataSetChanged();
    }

    private class ListAdapter extends BaseAdapter {
        String[] contentStrings = null;

        public void setContentStrings(String[] names) {
            contentStrings = names;
        }

        @Override
        public int getCount() {
            if (contentStrings != null)
                return contentStrings.length;
            else return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) {      //如果沒有前一個版本則新增
                v = new TextView(getContext());
                ((TextView) v).setText(contentStrings[i]);
                ((TextView) v).setTextColor(Color.WHITE);
                ((TextView) v).setTextSize(20);
                v.setTag(contentStrings[i]);
            }
            return v;
        }
    }

}
