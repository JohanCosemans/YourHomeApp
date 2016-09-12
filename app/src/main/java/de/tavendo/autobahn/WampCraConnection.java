/******************************************************************************
 *
 *  Copyright 2012 Alejandro Hernandez
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package de.tavendo.autobahn;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class WampCraConnection extends WampConnection implements WampCra {

	@Override
	public void authenticate(final AuthHandler authHandler, final String authKey, final String authSecret) {
		this.authenticate(authHandler, authKey, authSecret, null);
	}

	@Override
	public void authenticate(final AuthHandler authHandler, final String authKey, final String authSecret, Object authExtra) {
		CallHandler callHandler = new CallHandler() {

			@Override
			public void onResult(Object challenge) {

				String sig = null;
				try {
					sig = WampCraConnection.this.authSignature((String) challenge, authSecret);
				} catch (SignatureException e) {
					Log.e("WampCraConnection:authenicate", e.toString());
				}

				WampCraConnection.this.call(Wamp.URI_WAMP_PROCEDURE + "auth", WampCraPermissions.class, new CallHandler() {

					@Override
					public void onResult(Object result) {
						authHandler.onAuthSuccess(result);
					}

					@Override
					public void onError(String errorUri, String errorDesc) {
						authHandler.onAuthError(errorUri, errorDesc);
					}

				}, sig);

			}

			@Override
			public void onError(String errorUri, String errorDesc) {
				authHandler.onAuthError(errorUri, errorDesc);
			}

		};
		if (authExtra != null) {
			this.call(Wamp.URI_WAMP_PROCEDURE + "authreq", String.class, callHandler, authKey, authExtra);
		} else {
			this.call(Wamp.URI_WAMP_PROCEDURE + "authreq", String.class, callHandler, authKey);
		}
	}

	public String authSignature(String authChallenge, String authSecret) throws SignatureException {
		try {
			Key sk = new SecretKeySpec(authSecret.getBytes(), WampCraConnection.HASH_ALGORITHM);
			Mac mac = Mac.getInstance(sk.getAlgorithm());
			mac.init(sk);
			final byte[] hmac = mac.doFinal(authChallenge.getBytes());
			return Base64.encodeToString(hmac, Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e1) {
			throw new SignatureException("error building signature, no such algorithm in device " + WampCraConnection.HASH_ALGORITHM);
		} catch (InvalidKeyException e) {
			throw new SignatureException("error building signature, invalid key " + WampCraConnection.HASH_ALGORITHM);
		}
	}

	private static final String HASH_ALGORITHM = "HmacSHA256";

}
