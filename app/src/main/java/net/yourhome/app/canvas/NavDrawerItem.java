package net.yourhome.app.canvas;

public class NavDrawerItem {
    
    private String title;
    private int iconString;
    // boolean to set visiblity of the counter
    private boolean isCounterVisible = false;
     
    public NavDrawerItem(){}
 
    public NavDrawerItem(String title, int iconString){
        this.title = title;
        this.iconString = iconString;
    }
     
    public NavDrawerItem(String title, int iconString, boolean isCounterVisible){
        this.title = title;
        this.iconString = iconString;
        this.isCounterVisible = isCounterVisible;
    }
     
    public String getTitle(){
        return this.title;
    }
     
    public int getIconString(){
        return this.iconString;
    }
     
    public boolean getCounterVisibility(){
        return this.isCounterVisible;
    }
     
    public void setTitle(String title){
        this.title = title;
    }
     
    public void setIconString(int iconString){
        this.iconString = iconString;
    }
     
    public void setCounterVisibility(boolean isCounterVisible){
        this.isCounterVisible = isCounterVisible;
    }
}
