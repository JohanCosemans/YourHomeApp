package net.yourhome.app.canvas;

import java.util.List;

import net.yourhome.app.util.Configuration;
import net.yourhome.app.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseAdapter {
    
    private Context context;
    private List<NavDrawerItem> navDrawerItems;
     
    public NavDrawerListAdapter(Context context, List<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }
 
    @Override
    public int getCount() {
        return navDrawerItems.size();
    }
 
    @Override
    public Object getItem(int position) {       
        return navDrawerItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }
          
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        imgIcon.setImageBitmap(Configuration.getInstance().getAppIcon(context, navDrawerItems.get(position).getIconString(), 30, R.color.list_item_title));
        //txtTitle.setTypeface(Configuration.getInstance().getApplicationFont(context));
        txtTitle.setText(navDrawerItems.get(position).getTitle());
         
        return convertView;
    }
 
}
