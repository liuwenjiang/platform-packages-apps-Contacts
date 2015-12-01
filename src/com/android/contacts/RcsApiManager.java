/*
 * Copyright (c) 2014 pci-suntektech Technologies, Inc.  All Rights Reserved.
 * pci-suntektech Technologies Proprietary and Confidential.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.android.contacts;

import android.content.Context;

import com.android.contacts.common.util.ContactsCommonRcsUtil;
import com.android.contacts.util.RcsUtils;
import com.suntek.mway.rcs.client.api.ClientApi;
import com.suntek.mway.rcs.client.api.ServiceListener;
import com.suntek.mway.rcs.client.api.basic.BasicApi;
import com.suntek.mway.rcs.client.api.capability.CapabilityApi;
import com.suntek.mway.rcs.client.api.contact.ContactApi;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatApi;
import com.suntek.mway.rcs.client.api.profile.ProfileApi;
import com.suntek.mway.rcs.client.api.richscreen.RichScreenApi;
import com.suntek.mway.rcs.client.api.support.SupportApi;

public class RcsApiManager {
    private static final String TAG = "RcsApiManager";
    private static Context mContext;
    private static SupportApi mSupportApi = SupportApi.getInstance();

    public static void init(Context context) {
        mContext = context;
        mSupportApi.initApi(context);
        new ClientApi().init(context, new ServiceListener() {

            @Override
            public void onServiceConnected() {
            }

            @Override
            public void onServiceDisconnected() {
            }
        }, new ServiceListener() {

            @Override
            public void onServiceConnected() {
            }

            @Override
            public void onServiceDisconnected() {
            }
        });

        boolean isNativeUIInstalled  = RcsUtils.isNativeUiInstalled(context);
        RcsUtils.setNativeUIInstalled(isNativeUIInstalled);
     }

    public static SupportApi getSupportApi() {
        if (mSupportApi == null) {
            mSupportApi = SupportApi.getInstance();
            mSupportApi.initApi(mContext);
        }
        return mSupportApi;
    }
}
