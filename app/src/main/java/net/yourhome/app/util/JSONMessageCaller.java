/*-
 * Copyright (c) 2016 Coteq, Johan Cosemans
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.yourhome.app.util;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class JSONMessageCaller extends AsyncTask<JSONMessage, Void, JSONMessage> {

    protected Configuration configuration;
	protected Context context;

    public JSONMessageCaller(Context context) {
		this.context = context;
	}

	@Override
	protected JSONMessage doInBackground(JSONMessage... message) {
		this.configuration = Configuration.getInstance();
		String result = null;
        try {
            try {
                result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
            } catch (IOException | URISyntaxException e) {
                this.configuration.toggleConnectionInternalExternal(this.context);
                try {
                    result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
                } catch (Exception ex) {
                }
            }
            try {
                if(result != null) {
                    JSONObject returnObject = new JSONObject(result);
                    JSONMessage returnMessage = MessageTypes.getMessage(returnObject);
                    return returnMessage;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
		return null;
	}

}
