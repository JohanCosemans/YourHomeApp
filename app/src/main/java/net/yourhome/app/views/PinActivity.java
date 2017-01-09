package net.yourhome.app.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import net.yourhome.app.R;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.MessageLevels;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.common.net.messagestructures.general.ClientMessageMessage;
import net.yourhome.common.net.messagestructures.ipcamera.SnapshotRequestMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

import java.util.ArrayList;
import java.util.List;

public class PinActivity extends Activity implements View.OnClickListener {

    PinActivity me = this;
    List<Button> buttonList = new ArrayList<>();
    EditText userInput;
    Button cancelButton;
    Button okButton;
    ImageButton deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        buttonList.add((Button)findViewById(R.id.pin_button_0));
        buttonList.add((Button)findViewById(R.id.pin_button_1));
        buttonList.add((Button)findViewById(R.id.pin_button_2));
        buttonList.add((Button)findViewById(R.id.pin_button_3));
        buttonList.add((Button)findViewById(R.id.pin_button_4));
        buttonList.add((Button)findViewById(R.id.pin_button_5));
        buttonList.add((Button)findViewById(R.id.pin_button_6));
        buttonList.add((Button)findViewById(R.id.pin_button_7));
        buttonList.add((Button)findViewById(R.id.pin_button_8));
        buttonList.add((Button)findViewById(R.id.pin_button_9));

        for(Button button : buttonList){
            button.setOnClickListener(this);
        }
        deleteButton = (ImageButton)findViewById(R.id.pin_button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentInput = userInput.getText().toString();
                userInput.setText(currentInput.length()>0?currentInput.substring(0,currentInput.length()-1):"");
                userInput.setSelection(currentInput.length()>0?currentInput.length()-1:0);
            }
        });

        userInput = (EditText)findViewById(R.id.pin_entered_number);
        userInput.setRawInputType(InputType.TYPE_CLASS_TEXT);
        userInput.setTextIsSelectable(true);

        cancelButton = (Button)findViewById(R.id.pin_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        okButton = (Button)findViewById(R.id.pin_ok);

        // Read origin
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        String controllerIdentifier = extras.getString("controllerIdentifier");
        String nodeIdentifier = extras.getString("nodeIdentifier");
        String valueIdentifier = extras.getString("valueIdentifier");
        final String stageElementId = extras.getString("stageElementId");
        ControlIdentifiers identifiers = new ControlIdentifiers(ControllerTypes.convert(controllerIdentifier),nodeIdentifier,valueIdentifier);
        final List<AbstractBinding> bindingList = BindingController.getInstance().getBindingsFor(identifiers);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AbstractBinding binding : bindingList) {
                    if(binding.getStageElementId().equals(stageElementId)) {
                        UIEvent event = new UIEvent(UIEvent.Types.EMPTY);
                        event.setProperty("protected",true);
                        event.setProperty("protectionCode",userInput.getText().toString());

                        JSONMessageCaller loader = new SyncJSONMessageCaller(me.getBaseContext());
                        boolean result = binding.viewPressed(null, event,loader);
                    }
                }
            }
        });
    }
    public void onClick(View v){
        String currentInput = userInput.getText().toString();
        int buttonIndex = buttonList.indexOf(v) +1;
        switch(v.getId()){
            case R.id.pin_button_0:
                userInput.append(0+"");
                break;
            case R.id.pin_button_1:
                userInput.append(1+"");
                break;
            case R.id.pin_button_2:
                userInput.append(2+"");
                break;
            case R.id.pin_button_3:
                userInput.append(3+"");
                break;
            case R.id.pin_button_4:
                userInput.append(4+"");
                break;
            case R.id.pin_button_5:
                userInput.append(5+"");
                break;
            case R.id.pin_button_6:
                userInput.append(6+"");
                break;
            case R.id.pin_button_7:
                userInput.append(7+"");
                break;
            case R.id.pin_button_8:
                userInput.append(8+"");
                break;
            case R.id.pin_button_9:
                userInput.append(9+"");
                break;
        }
    }
    public class SyncJSONMessageCaller extends JSONMessageCaller {

        private Runnable postExecuteAction;
        private JSONMessage resultMessage;
        private boolean finishActivity = true;

        public SyncJSONMessageCaller(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONMessage resultMessage) {
            this.resultMessage = resultMessage;
            BindingController.getInstance().handleCommand(resultMessage);

            if(resultMessage instanceof ClientMessageMessage) {
                ClientMessageMessage clientMessage = (ClientMessageMessage) resultMessage;

                if(postExecuteAction != null) {
                    postExecuteAction.run();
                }

                if(finishActivity && clientMessage.messageLevel != MessageLevels.ERROR) {
                    me.finish();
                }
            }else if(finishActivity) {
                me.finish();
            }
        }
        public JSONMessage getResultMessage() {
            return resultMessage;
        }
        public void setPostExecuteAction(Runnable runnable) {
            this.postExecuteAction=runnable;
        }
        public void setFinishActivity(boolean finishActivity) {
            this.finishActivity = finishActivity;
        }
    }
}
