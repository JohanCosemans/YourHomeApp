package net.yourhome.app.bindings;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import net.yourhome.app.canvas.CanvasActivity;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.PinActivity;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.base.enums.MessageLevels;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.common.net.messagestructures.general.ClientMessageMessage;
import net.yourhome.common.net.messagestructures.general.ProtectedJSONMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Johan on 28-9-2016.
 */
public class PageNavigationBinding extends AbstractBinding {
    public static final String PAGE_ID = "pageId";

    public PageNavigationBinding(String stageItemId,JSONObject bindingProperties) throws JSONException {
        super(stageItemId,bindingProperties);
    }

    @Override
    public void handleMessage(JSONMessage message){}

    @Override
    public boolean viewPressed(DynamicView v, UIEvent event) {
        // Throw new event to main activity to start navigation
        if(this.viewListeners.size() > 0) {
            Intent intent = new Intent(CanvasActivity.CanvasEvents.PAGE_NAVIGATION.name());
            intent.putExtra(PAGE_ID,this.controlIdentifier.getValueIdentifier());
            DynamicView firstView = this.viewListeners.get(0);
            LocalBroadcastManager.getInstance(firstView.getContext()).sendBroadcast(intent);
        }
        return true;
    }

    @Override
    public boolean viewPressed(DynamicView v, final UIEvent event, JSONMessageCaller apiCaller) {
        final DynamicView finalV = v;
        if(apiCaller instanceof PinActivity.SyncJSONMessageCaller) {
            final PinActivity.SyncJSONMessageCaller syncApiCaller = (PinActivity.SyncJSONMessageCaller)apiCaller;
            syncApiCaller.setPostExecuteAction(new Runnable() {
                @Override
                public void run() {
                    JSONMessage resultMessage = syncApiCaller.getResultMessage();
                    if(resultMessage instanceof ClientMessageMessage
                            && ((ClientMessageMessage)resultMessage).messageLevel != MessageLevels.ERROR) {
                        syncApiCaller.setFinishActivity(true);
                        viewPressed(finalV,event);
                    }
                }
            });
            apiCaller.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buildMessage(v, event));
        }
        return true;
    }

    private JSONMessage buildMessage(DynamicView v,UIEvent event) {
        ProtectedJSONMessage activationMessage = new ProtectedJSONMessage();
        activationMessage.controlIdentifiers = this.getControlIdentifier();
        if(event != null
                && event.getProperty("protected") != null
                && (Boolean)event.getProperty("protected")) {
            activationMessage.isProtected = (Boolean)event.getProperty("protected");
            activationMessage.protectionCode = (String)event.getProperty("protectionCode");
        }
        return activationMessage;
    }

    @Override
    public void viewLongPressed(DynamicView v, UIEvent event) {

    }

    @Override
    protected void buildBinding() {

    }
}
