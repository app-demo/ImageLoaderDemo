package pull.cn.com.htmldemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cn.image.ImageLoader;

import java.util.List;

/**
 * Author: river
 * Date: 2015/12/30 14:56
 * Description:
 */
public class ListAdapter extends BaseAdapter {
    List<String> list;
    Context context;

    public ListAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder mHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);


            mHolder = new Holder();
            mHolder.image = (ImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(mHolder);

        } else {
            mHolder = (Holder) convertView.getTag();

        }

        ImageLoader.getInstance(context).loadImage(list.get(position), mHolder.image);

        return convertView;
    }

    class Holder {
        ImageView image;
    }
}
