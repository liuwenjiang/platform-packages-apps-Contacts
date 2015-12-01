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
package com.android.contacts.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SqliteWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Identity;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.Telephony.Threads;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.RawContact;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.EmailDataItem;
import com.android.contacts.common.model.dataitem.EventDataItem;
import com.android.contacts.common.model.dataitem.OrganizationDataItem;
import com.android.contacts.common.model.dataitem.PhoneDataItem;
import com.android.contacts.common.model.dataitem.StructuredNameDataItem;
import com.android.contacts.common.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.util.ContactsCommonRcsUtil;
import com.android.contacts.group.GroupBrowseListAdapter;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupListItem;
import com.android.contacts.quickcontact.MyQrcodeActivity;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.RcsApiManager;
import com.android.contacts.R;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar.IMAGE_TYPE;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.TelephoneModel;
import com.suntek.mway.rcs.client.aidl.plugin.entity.contact.Auth;
import com.suntek.mway.rcs.client.aidl.plugin.entity.contact.SyncAction;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardImg;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardInfo;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IContactSyncListener;
import com.suntek.mway.rcs.client.api.profile.ProfileListener;
import com.suntek.mway.rcs.client.api.capability.CapabiltyListener;
import com.suntek.mway.rcs.client.aidl.service.entity.GroupChat;
import com.suntek.mway.rcs.client.aidl.service.entity.GroupChatMember;
import com.suntek.mway.rcs.client.aidl.service.entity.RCSCapabilities;
import com.suntek.mway.rcs.client.api.basic.BasicApi;
import com.suntek.mway.rcs.client.api.capability.CapabilityApi;
import com.suntek.mway.rcs.client.api.capability.CapabiltyListener;
import com.suntek.mway.rcs.client.api.contact.ContactApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatApi;
import com.suntek.mway.rcs.client.api.profile.ProfileApi;
import com.suntek.mway.rcs.client.api.richscreen.RichScreenApi;
import com.suntek.rcs.ui.common.RcsLog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RcsUtils {

    public static final String TAG = "ContactsRcsUtils";

    public static final String LOCAL_PHOTO_SETTED = "local_photo_setted";

    public static final String KEY_IS_INSERT = "isInsert";

    public static final String IS_VIEWING_CONTACT_DETAIL = "is_viewing_contact_detail";

    public static final String PREF_UPDATE_WHILE_FIRST_CONNECT = "pref_update_while_first_connect";

    public static final String PREF_TIME_OF_LAST_WIFI_CONNECT= "pref_time_of_last_wifi_connect";

    public static final String PREF_RCS_FILE_NAME = "RcsSharepreferences";

    public static final String PREF_RCS_PROFILE_TEXT_ETAG = "ProfilePotoEtag";

    public static final String PREF_RCS_PROFILE_PHOTO_ETAG = "ProfilePotoEtag";

    // User requst to update enhance screen
    public static final String UPDATE_ENHANCE_SCREEN_PHONE_EVENT = "933 10 12000";

    public static final String PREF_DAY_OF_WEEK_LAST_CONNECT = "pref_day_of_week_last_connect";

    public static final String PREF_MY_TEMINAL = "pref_my_terminal";

    public static final long SERVEN_DAYS = (60 * 60 * 24 * 7 * 1000);

    public static final long ONE_DAY = (60 * 60 * 24 * 1 * 1000);

    public static final String INSERT_LOCAL_PROFILE = "insert_local_profile";

    public static final String MSISDN = "13800138324";

    private static String imagePath = "image";

    private static final Uri PROFILE_URI = Uri.parse("content://com.android.contacts/profile");

    private static final Uri PROFILE_DATA_URI = Uri.withAppendedPath(PROFILE_URI, "data");

    private static final Uri PROFILE_RAW_CONTACTS_URI = Uri.withAppendedPath(PROFILE_URI,
            "raw_contacts");

    public static int RCS_TYPE_FIXED = 21;

    public static final String KEY_IS_SOMETHING_CHANGED_EXCEPT_PHOTO
                               = "isSomethingChangedButExceptPhoto";

    public static final String ACTION_PUBLIC_ACCOUNT_ACTIVITY
                               = "com.suntek.mway.rcs.nativeui.ui.PUBLIC_ACCOUNT_ACTIVITY";

    public static final String ACTION_BACKUP_RESTORE_ACTIVITY
                               = "com.suntek.mway.rcs.nativeui.ui.BACKUP_RESTORE_ACTIVITY";

    public static final String QUICK_CONTACTS_ACTIVITY
                               = "com.android.contacts.quickcontact.QuickContactActivity";

    public static final String RCS_CAPABILITY_CHANGED = "rcs_capability_changed";

    public static final String RCS_CAPABILITY_CHANGED_CONTACT_ID
                                = "rcs_capability_changed_contact_id";

    public static final String RCS_CAPABILITY_CHANGED_VALUE = "rcs_capability_changed_value";

    private static final int LOADER_GROUPS = 1;

    // RCS capability: sucess.
    public static final int RCS_SUCESS = 200;

    // RCS capability: offline.
    public static final int RCS_OFFLINE = 408;

    // RCS capability: not RCS.
    public static final int NOT_RCS = 404;

    public static final int RESTORE_CONTACTS = 0;

    public static final int DOWNLOAD_PROFILE = 1;

    private static int DEFAULT_NUMBER_LENGTH = 11;

    private static final String NATIVE_UI_PACKAGE = "com.suntek.mway.rcs.nativeui";

    private static final String PLUGIN_PACKAGE = "com.suntek.mway.rcs.app.plugin";

    private static final String KEY_BACKUP_ONCE_CHANGED = "key_backup_once_changed";

    private static final String KEY_AUTO_BACKUP = "key_auto_backup";

    private static final String PREF_BACKUP_RESTORE_NAME = "pref_backup_restore_name";

    private static final String KEY_ONLY_WIFI_BACKUP_RESOTORE = "key_only_wifi_backup_restore";

    private static final String ENHANCE_SCREEN_APK_NAME = "com.cmdm.rcs";

    private static final String ONLINE_BUSINESS_HALL = "cn.com.onlinebusiness";

    private static final String PLUNGIN_CENTER = "com.cmri.rcs.plugincenter";

    public static boolean isNativeUIInstalled;

    // add firewall menu
    private static final Uri WHITELIST_CONTENT_URI = Uri
            .parse("content://com.android.firewall/whitelistitems");

    private static final Uri BLACKLIST_CONTENT_URI = Uri
            .parse("content://com.android.firewall/blacklistitems");

    private static boolean isPackageInstalled(Context context, String packageName) {
        boolean installed = false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_PROVIDERS);
            installed = (info != null);
        } catch (NameNotFoundException e) {
        }
        RcsLog.i("Is " + packageName + "installed ? " + installed);
        return installed;
    }

    public static boolean isNativeUiInstalled(Context context) {
        return isPackageInstalled(context, NATIVE_UI_PACKAGE);
    }

    public static boolean isPluginInstalled(Context context) {
        return isPackageInstalled(context, PLUGIN_PACKAGE);
    }

    public static boolean isEnhanceScreenInstalled(Context context) {
        return isPackageInstalled(context, ENHANCE_SCREEN_APK_NAME);
    }

    public static boolean isOnlineBusinessHallInstalled(Context context) {
        return isPackageInstalled(context, ONLINE_BUSINESS_HALL);
    }

    public static boolean isPlunginCenterInstalled(Context context) {
        return isPackageInstalled(context, PLUNGIN_CENTER);
    }

    public static void resotreIfTerminalChanged(final Context context, final int mode,
            final Contact contactData, final RestoreFinishedListener listener) {
        final Handler handler = new Handler();
        Thread t = new Thread() {
            @Override
            public void run() {
                RcsUtils.sleep(500);
                String myAccountNumber = "";
                try {
                    RcsLog.d("Calling  BasicApi.getInstance().getAccount()");
                    myAccountNumber = BasicApi.getInstance().getAccount();
                } catch (ServiceDisconnectedException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            makeToast(context, R.string.rcs_service_is_not_available);
                        }
                    });
                    RcsLog.w(e);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                RcsLog.d("The account is " + myAccountNumber);
                final int message;
                SharedPreferences prefs;
                if (mode == RESTORE_CONTACTS) {
                    prefs = context.getSharedPreferences("RestoreContactsPreference",
                            Context.MODE_PRIVATE);
                    message = R.string.rcs_resotre_contacts_if_terminal_changed;
                } else if (mode == DOWNLOAD_PROFILE){
                    prefs = context.getSharedPreferences("DownloadProfilePreferences",
                            Context.MODE_PRIVATE);
                    message = R.string.rcs_download_profile_if_data_changed;
                } else {
                    return;
                }
                String latestTerminal = prefs.getString(RcsUtils.PREF_MY_TEMINAL, "");
                if (!TextUtils.isEmpty(myAccountNumber)
                        && !TextUtils.equals(myAccountNumber, latestTerminal)) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Dialog dialog = new AlertDialog.Builder(context)
                                    .setMessage(context.getResources().getString(message))
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                                    if (mode == RESTORE_CONTACTS) {
                                                        try {
                                                             context.startActivity(
                                                             new Intent(RcsUtils
                                                                .ACTION_BACKUP_RESTORE_ACTIVITY));
                                                        } catch (ActivityNotFoundException ex) {
                                                            Toast.makeText(context,
                                                                    R.string.missing_app,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    if (mode == DOWNLOAD_PROFILE) {
                                                        RcsUtils.restoreLocalProfileInfo(
                                                                context.getApplicationContext(),
                                                                contactData, listener);
                                                    }
                                                }
                                            }).create();
                            dialog.show();
                        }
                    });
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(RcsUtils.PREF_MY_TEMINAL, myAccountNumber);
                    editor.apply();
                }
            }
        };
        t.start();
    }

    public static class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!RcsApiManager.getSupportApi().isRcsSupported())
                return;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean(
                    RcsUtils.PREF_UPDATE_WHILE_FIRST_CONNECT, false)) {
                return;
            }
            if (RcsUtils.isWifiEnabled(context.getApplicationContext())) {
                long nowTime = System.currentTimeMillis();
                long theTimeOfLastConnection = prefs.getLong(
                        RcsUtils.PREF_TIME_OF_LAST_WIFI_CONNECT, nowTime);
                int nowDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                int theDayOfWeekOfLastConnection = prefs.getInt(
                        RcsUtils.PREF_DAY_OF_WEEK_LAST_CONNECT, nowDayOfWeek);
                // Over 7 days.
                boolean flag1 = nowTime - theTimeOfLastConnection >= RcsUtils.SERVEN_DAYS;
                // Less than 7 days but in diferent week.
                boolean flag2 = (nowTime - theTimeOfLastConnection < RcsUtils.SERVEN_DAYS)
                        && (nowDayOfWeek < theDayOfWeekOfLastConnection);
                // The day of week is same, but in deferent week.
                boolean flag3 = (nowDayOfWeek == theDayOfWeekOfLastConnection)
                        && (nowTime - theTimeOfLastConnection > RcsUtils.ONE_DAY);

                if (flag1 || flag2 || flag3) {
                    RcsUtils.updateContactsPhotos(context);
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(RcsUtils.PREF_TIME_OF_LAST_WIFI_CONNECT, nowTime);
                editor.putInt(RcsUtils.PREF_DAY_OF_WEEK_LAST_CONNECT, nowDayOfWeek);
                editor.apply();
            }
        }
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float)width / w);
        float scaleHeight = ((float)height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static final String MIMETYPE_RCS = "vnd.android.cursor.item/rcs";

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static void updateRCSCapability(final Activity activity, final Contact contactData) {
        final Handler handler = new Handler();
        try {
            if (!BasicApi.getInstance().isOnline()) {
                RcsLog.d("Calling updateRCSCapability Rcs is offline!");
                return;
            }
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
            return;
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(1000);
                if (activity == null || activity.isFinishing() || !activity.isResumed()) {
                    return;
                }
                RcsLog.d("Calling updateRCSCapability!");
                queryRCSCapability(activity.getApplicationContext(), contactData, handler);
            }
        });
        t.start();
    }

    private static void insertRcsCapa(final Context context, long contactId, long rawContactId,
            int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Data.DATA1, contactId);
        contentValues.put(ContactsContract.Data.DATA2, value);
        contentValues.put(ContactsContract.Data.MIMETYPE,
                 ContactsCommonRcsUtil.RCS_CAPABILITY_MIMETYPE);
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    private static void deleteRcsCapa(final Context context, long contactId) {
        context.getContentResolver().delete(
                ContactsContract.Data.CONTENT_URI,
                ContactsContract.Data.MIMETYPE + " = ?  and " + ContactsContract.Data.DATA1
                        + " = ?", new String[] {
                         ContactsCommonRcsUtil.RCS_CAPABILITY_MIMETYPE, String.valueOf(contactId)
                });
    }

    private static void updateRcsCapa(final Context context, long contactId, int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Data.DATA2, value);
        context.getContentResolver().update(
                ContactsContract.Data.CONTENT_URI,
                contentValues,
                ContactsContract.Data.MIMETYPE + " = ?  and " + ContactsContract.Data.DATA1
                        + " = ?", new String[] {
                        ContactsCommonRcsUtil.RCS_CAPABILITY_MIMETYPE, String.valueOf(contactId)
                });
    }

    private static void queryRCSCapability(final Context context, final Contact contactData,
            final Handler handler) {
        final long contactId = contactData.getRawContacts().get(0).getContactId();
        deleteRcsCapa(context, contactId);
        boolean hasPhoneNumber = false;
        ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.clear();
        for (RawContact rawContact : contactData.getRawContacts()) {
            final long rawContactId = rawContact.getId();
            for (DataItem dataItem : rawContact.getDataItems()) {
                if (dataItem instanceof PhoneDataItem) {
                    String phoneNumber = ((PhoneDataItem)dataItem).getNumber();
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        phoneNumber = phoneNumber.trim();
                        hasPhoneNumber = true;
                    } else {
                        continue;
                    }
                    RcsLog.d("Phone number is: " + phoneNumber);
                    findContactsCapacity(context, contactId, rawContactId, phoneNumber, handler);
                }
            }
        }
        if (!hasPhoneNumber) {
            ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, false);
            RcsLog.d(contactData.getDisplayName() + ": " + " is not RCS user!");
            insertRcsCapa(context, contactId, -1, 0);
        }
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo.isConnected();
        } else {
            return false;
        }
    }

    public interface RestoreFinishedListener {
        void onRestoreFinished();
    }

    private static byte[] processPhoto(byte[] photo) {
        Bitmap tempBitmap = Bytes2Bitmap(photo);
        int height = tempBitmap.getHeight();
        int width = tempBitmap.getWidth();
        if (height <= 120 || width <= 120) {
            return Bitmap2Bytes(zoomBitmap(tempBitmap, 720, 720));
        }
        if (height >= 1024 || width >= 1024) {
            return Bitmap2Bytes(zoomBitmap(tempBitmap, 720, 720));
        }
        if (height != width) {
            int len = (width > height) ? height : width;
            return Bitmap2Bytes(zoomBitmap(tempBitmap, len, len));
        }
        return photo;
    }

    public static int whichBtn = 0;

    public static Dialog createLocalProfileBackupRestoreDialog(final Context context,
            final Contact contactData, final RestoreFinishedListener listener) {
        String[] items = new String[] {
                context.getString(R.string.upload_profile),
                context.getString(R.string.download_profile)
        };
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.upload_download_profile))
                .setSingleChoiceItems(items, whichBtn, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        whichBtn = which;
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        okToRestoreLocalProfile(context.getApplicationContext(), whichBtn,
                                contactData, listener);
                    }
                }).create();
        return alertDialog;
    }

    private static class UpdatePhotosTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        private Handler mHandler = new Handler();

        UpdatePhotosTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver resolver = mContext.getContentResolver();
            Cursor c = resolver.query(Contacts.CONTENT_URI, new String[] {
                Contacts._ID
            }, null, null, null);
            ArrayList<Long> contactIdList = new ArrayList<Long>();
            try {
                if (c != null && c.moveToFirst()) {
                    do {
                        Long contactId = c.getLong(0);
                        contactIdList.add(contactId);
                    } while (c.moveToNext());
                }
            } finally {
                if (null != c) {
                    c.close();
                }
            }
            for (long aContactId : contactIdList) {
                c = resolver.query(RawContacts.CONTENT_URI, new String[] {
                    RawContacts._ID
                }, RawContacts.CONTACT_ID + "=" + String.valueOf(aContactId), null, null);
                final ArrayList<Long> rawContactIdList = new ArrayList<Long>();
                try {
                    if (c != null && c.moveToFirst()) {
                        do {
                            long rawContactId = c.getLong(0);
                            if (!hasLocalSetted(resolver, rawContactId)) {
                                rawContactIdList.add(rawContactId);
                            }
                        } while (c.moveToNext());
                    }
                } finally {
                    if (null != c) {
                        c.close();
                    }
                }
                if (rawContactIdList.size() > 0) {
                    try {
                        ProfileApi.getInstance().getHeadPicByContact(aContactId,
                                new ProfileListener() {
                                    @Override
                                    public void onAvatarGet(final Avatar photo,
                                            final int resultCode, final String resultDesc)
                                            throws RemoteException {
                                        saveProfilePhoto(mContext, mHandler, photo, resultCode,
                                                resultDesc, rawContactIdList);
                                    }

                                    @Override
                                    public void onAvatarUpdated(int arg0, String arg1)
                                            throws RemoteException {
                                    }

                                    @Override
                                    public void onProfileGet(Profile arg0, int arg1, String arg2)
                                            throws RemoteException {
                                    }

                                    @Override
                                    public void onProfileUpdated(int arg0, String arg1)
                                            throws RemoteException {
                                    }

                                    @Override
                                    public void onQRImgDecode(QRCardInfo imgObj, int resultCode,
                                            String arg2) throws RemoteException {

                                    }
                                });
                    } catch (ServiceDisconnectedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

    }

    public static void updateContactsPhotos(Context context) {
        new UpdatePhotosTask(context).execute();
    }

    private static final String TEMP_PHOTO_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/temp_photo";

    public static boolean hasLocalSetted(ContentResolver resolver, long rawContactId) {
        Cursor c = resolver.query(RawContacts.CONTENT_URI, new String[] {
            LOCAL_PHOTO_SETTED
        }, RawContacts._ID + "=" + String.valueOf(rawContactId), null, null);
        long localSetted = 0;
        try {
            if (c != null && c.moveToFirst()) {
                localSetted = c.getLong(0);
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
        return (localSetted == 1) ? true : false;
    }

    public static boolean isNativeUIInstalled() {
        return isNativeUIInstalled;
    }

    public static void setNativeUIInstalled(boolean isNativeUIInstalled) {
        RcsUtils.isNativeUIInstalled = isNativeUIInstalled;
    }

    public static void setLocalSetted(ContentResolver resolver, boolean isLocalSetted,
            long rawContactId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCAL_PHOTO_SETTED, isLocalSetted ? 1 : 0);
        resolver.update(RawContacts.CONTENT_URI, contentValues,
                RawContacts._ID + "=" + String.valueOf(rawContactId), null);
    }

    public static void newAndEditContactsUpdateEnhanceScreen(Context context,
            ContentResolver resolver, long rawContactId) {
        RcsLog.d("new and edit contact rawContactId: " + rawContactId);
        if (RcsApiManager.getSupportApi().isRcsSupported() && isEnhanceScreenInstalled(context)) {
            Cursor phone = null;
            try {
                phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + rawContactId,
                        null, null);
                if (null != phone) {
                    while (phone.moveToNext()) {
                        String Number = phone.getString(phone
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (!TextUtils.isEmpty(Number)) {
                            Number = getFormatNumber(Number);
                            RcsLog.d("new and edit contact downloadRichScrnObj" + Number);
                            RichScreenApi.getInstance().downloadRichScrnObj(Number,
                                    UPDATE_ENHANCE_SCREEN_PHONE_EVENT);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (phone != null) {
                    phone.close();
                    phone = null;
                }
            }
        }
    }

    public static void importContactUpdateEnhanceScreen(HashSet<String> phoneNumberSet) {
        try {
            RcsLog.d("import contact downloadRichScrnObj" + phoneNumberSet.toString());
            Iterator<String> iterator = phoneNumberSet.iterator();
            while(iterator.hasNext()){
                String phoneNumber = getFormatNumber(iterator.next().replaceAll(",", ""));
                RichScreenApi.getInstance().downloadRichScrnObj(phoneNumber,
                        UPDATE_ENHANCE_SCREEN_PHONE_EVENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setContactPhoto(Context context, byte[] input, Uri outputUri) {
        FileOutputStream outputStream = null;

        try {
            outputStream = context.getContentResolver().openAssetFileDescriptor(outputUri, "rw")
                    .createOutputStream();
            outputStream.write(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(outputStream);
        }
    }

    public static byte[] readFromPhoto(Context context) {
        AssetManager am = context.getAssets();
        String[] images = null;
        byte[] content = null;

        try {
            images = am.list(imagePath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedInputStream in = null;
        ByteArrayOutputStream out = null;
        for (int i = 0; i < images.length; i++) {
            try {
                String image = images[i];

                in = new BufferedInputStream(am.open(imagePath + "/" + image));

                out = new ByteArrayOutputStream(1024);
                byte[] temp = new byte[1024];
                int size = 0;
                while ((size = in.read(temp)) != -1) {
                    out.write(temp, 0, size);
                }
                content = out.toByteArray();
                if (content != null) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return content;
    }

    private static String getRawContactId(Context context) {
        String rawContactId = null;
        Uri uri = Uri.parse("content://com.android.contacts/profile/raw_contacts/");
        Cursor cursor = context.getContentResolver().query(uri, null,
                "account_id = 1 AND contact_id != '' ", null, null);
        try {
            if (cursor != null && cursor.moveToFirst() && !cursor.isAfterLast()) {
                rawContactId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                cursor.moveToNext();
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        if (rawContactId == null) {
            ContentValues values = new ContentValues();
            Uri rawContactUri = context.getContentResolver().insert(uri, values);
            rawContactId = String.valueOf(ContentUris.parseId(rawContactUri));
        }
        return rawContactId;
    }

    public static int getLocalGroupsCount(Context context) {
        StringBuilder where = new StringBuilder();
        where.append(Groups.DELETED + "!=1");
        where.append(" AND (" + Groups.SOURCE_ID + "!='RCS'" + " OR " + Groups.SOURCE_ID
                + " IS NULL)");
        Cursor c = context.getContentResolver().query(Groups.CONTENT_URI, null, where.toString(),
                null, null);
        if (c != null) {
            int groupsCount = c.getCount();
            c.close();
            return groupsCount;
        } else {
            return 0;
        }

    }

    public static void saveQrCode(Context context, String imgBase64, String etag) {
        String rawContactId = getRawContactId(context);
        Cursor cursor = context.getContentResolver().query(PROFILE_DATA_URI, new String[] {
                "_id", "mimetype", "data15"
        }, " raw_contact_id = ?  AND mimetype = ? ", new String[] {
                rawContactId, MIMETYPE_RCS
        }, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String dataId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                ContentValues values = new ContentValues();
                values.put("data15", imgBase64);
                values.put("data14", etag);
                context.getContentResolver().update(PROFILE_DATA_URI, values, " _id = ? ",
                        new String[] {
                            dataId
                        });
            } else {
                ContentValues values = new ContentValues();
                values.put("data15", imgBase64);
                values.put("raw_contact_id", rawContactId);
                values.put("mimetype", MIMETYPE_RCS);
                context.getContentResolver().insert(
                        Uri.parse("content://com.android.contacts/profile/data/"), values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String GetQrCode(Context context, String rawContactId) {

        Uri uri = Uri.parse("content://com.android.contacts/profile/data/");
        String imgBase64 = null;
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                "_id", "mimetype", "data15"
        }, " raw_contact_id = ?  AND mimetype = ? ", new String[] {
                rawContactId, MIMETYPE_RCS
        }, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                imgBase64 = cursor.getString(cursor.getColumnIndexOrThrow("data15"));
            } else {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imgBase64;
    }

    // Get presence from data table
    public static Bitmap getMyProfilePhotoOnData(Context context, String rawContactId) {
        Bitmap bitmap = null;
        Uri uri = Uri.parse("content://com.android.contacts/profile/data/");
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                "_id", "mimetype", "data15"
        }, " raw_contact_id = ?  AND mimetype = ? ", new String[] {
                rawContactId, "vnd.android.cursor.item/photo"
        }, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow("data15"));
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bitmap;
    }

    public static Profile getMyProfileOnDB(Context context, String rawContactId) {
        Profile profile = null;
        ArrayList<TelephoneModel> teleList = null;
        Uri uri = Uri.parse("content://com.android.contacts/profile/data/");
        StringBuilder where = new StringBuilder();
        where.append("raw_contact_id=");
        where.append(rawContactId);
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                "_id", "mimetype", "data1", "data2", "data3", "data4", "data15"
        }, where.toString(), null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                profile = new Profile();
                teleList = new ArrayList<TelephoneModel>();
                while (!cursor.isAfterLast()) {
                    String mimetype = cursor.getString(cursor.getColumnIndexOrThrow("mimetype"));
                    String data1 = cursor.getString(cursor.getColumnIndexOrThrow("data1"));
                    if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                        String numberType = cursor.getString(cursor.getColumnIndexOrThrow("data2"));
                        if (TextUtils.isEmpty(numberType)) {
                            numberType = "1";
                        }
                        if ("4".equals(numberType)) {
                            profile.setCompanyFax(data1);
                        } else if ("17".equals(numberType)) {
                            profile.setCompanyTel(data1);
                            // Add account
                        } else if ("2".equals(numberType)) {
                            profile.setAccount(data1);
                        } else {
                            TelephoneModel model = new TelephoneModel();
                            model.setTelephone(data1);
                            model.setType(Integer.parseInt(numberType));
                            teleList.add(model);
                        }
                    } else if ("vnd.android.cursor.item/postal-address_v2".equals(mimetype)) {
                        String data2 = cursor.getString(cursor.getColumnIndexOrThrow("data2"));

                        if ("1".equals(data2)) {
                            profile.setHomeAddress(data1);
                        } else if ("2".equals(data2)) {
                            profile.setCompanyAddress(data1);
                        }
                    } else if ("vnd.android.cursor.item/name".equals(mimetype)) {
                        String fristName = cursor.getString(cursor.getColumnIndexOrThrow("data2"));
                        String lastName = cursor.getString(cursor.getColumnIndexOrThrow("data3"));
                        profile.setFirstName(fristName);
                        profile.setLastName(lastName);
                    } else if ("vnd.android.cursor.item/email_v2".equals(mimetype)) {
                        profile.setEmail(data1);
                    } else if ("vnd.android.cursor.item/organization".equals(mimetype)) {
                        String data4 = cursor.getString(cursor.getColumnIndexOrThrow("data4"));
                        profile.setCompanyName(data1);
                        profile.setCompanyDuty(data4);
                    } else if (MIMETYPE_RCS.equals(mimetype)) {
                        String data2 = cursor.getString(cursor.getColumnIndexOrThrow("data2"));
                        profile.setEtag(data1);
                        profile.setBirthday(data2);
                    }
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (profile != null) {
            profile.setOtherTels(teleList);
        }
        return profile;
    }

    public static String dealPhoneNumberString(String number) {
        if (number == null) {
            return "";
        }
        number = number.replaceAll(" ", "");
        return number;
    }

    public static void insertQrcodeContact(Context context, Intent data) {
        // QRcode Vcard only include 7 fields.
        String name = data.getStringExtra("name");
        String tel = data.getStringExtra("tel");
        String companyTel = data.getStringExtra("companyTel");
        String companyFax = data.getStringExtra("companyFax");
        String companyName = data.getStringExtra("companyName");
        String companyDuty = data.getStringExtra("companyDuty");
        String companyEmail = data.getStringExtra("companyEmail");
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, tel);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, companyEmail);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, companyTel);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, companyFax);
        intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, companyName);
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, companyDuty);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        context.startActivity(intent);
    }

    public static String getProfileAccountNumber() {
        String myAccountNumber = "";
        try {
            myAccountNumber = BasicApi.getInstance().getAccount();
        } catch (ServiceDisconnectedException e) {
            RcsLog.w(e);
            e.printStackTrace();
        } catch (RemoteException e) {
            RcsLog.w(e);
            e.printStackTrace();
        }
        return myAccountNumber;
    }

    public static Profile createLocalProfile(RawContact rawContact) {

        if (rawContact == null)
            return null;

        Profile profile = new Profile();
        String myAccountNumber = RcsUtils.getMyPhoneNumber();
        profile.setAccount(myAccountNumber);
        profile.setOtherTels(new ArrayList<TelephoneModel>());
        String firstName = "";
        String lastName = "";
        for (DataItem dataItem : rawContact.getDataItems()) {
            if (dataItem instanceof StructuredNameDataItem) {
                firstName = ((StructuredNameDataItem)dataItem).getGivenName();
                lastName = ((StructuredNameDataItem)dataItem).getFamilyName();
                RcsLog.d("The first name is " + firstName);
                RcsLog.d("The last name is " + lastName);
                if (TextUtils.isEmpty(firstName)) {
                    return null;
                }
                profile.setFirstName(firstName);
                profile.setLastName(lastName);
            } else if (dataItem instanceof PhoneDataItem) {
                int phoneType = ((PhoneDataItem)dataItem).getContentValues().getAsInteger(
                        Phone.TYPE);
                if ((Phone.TYPE_WORK == phoneType) && profile.getCompanyTel() == null) {
                    profile.setCompanyTel(((PhoneDataItem)dataItem).getNumber());
                } else if ((Phone.TYPE_FAX_WORK == phoneType) && profile.getCompanyFax() == null) {
                    profile.setCompanyFax(((PhoneDataItem)dataItem).getNumber());
                } else if (profile.getOtherTels().size() < 6) {
                    TelephoneModel tele;
                    switch (phoneType) {
                        case Phone.TYPE_HOME: {
                            tele = new TelephoneModel();
                            tele.setType(TelephoneModel.TYPE_HOME);
                            tele.setTelephone(((PhoneDataItem)dataItem).getNumber());
                            profile.getOtherTels().add(tele);

                            tele = new TelephoneModel();
                            tele.setType(TelephoneModel.TYPE_FIXED);
                            tele.setTelephone(((PhoneDataItem)dataItem).getNumber());
                            profile.getOtherTels().add(tele);
                            break;
                        }

                        case Phone.TYPE_WORK: {
                            tele = new TelephoneModel();
                            tele.setType(TelephoneModel.TYPE_WORK);
                            tele.setTelephone(((PhoneDataItem)dataItem).getNumber());
                            profile.getOtherTels().add(tele);
                            break;
                        }

                        case Phone.TYPE_MOBILE: {
                            tele = new TelephoneModel();
                            tele.setType(TelephoneModel.TYPE_MOBILE);
                            tele.setTelephone(((PhoneDataItem)dataItem).getNumber());
                            profile.getOtherTels().add(tele);
                            break;
                        }

                        default: {
                            tele = new TelephoneModel();
                            tele.setType(TelephoneModel.TYPE_OTHER);
                            tele.setTelephone(((PhoneDataItem)dataItem).getNumber());
                            profile.getOtherTels().add(tele);
                            break;
                        }

                    }
                }

            } else if (dataItem instanceof OrganizationDataItem) {
                profile.setCompanyName(((OrganizationDataItem)dataItem).getCompany());
                profile.setCompanyDuty(((OrganizationDataItem)dataItem).getTitle());
            } else if (dataItem instanceof StructuredPostalDataItem) {
                int type = ((StructuredPostalDataItem)dataItem).getContentValues().getAsInteger(
                        StructuredPostal.TYPE);
                if (type == StructuredPostal.TYPE_HOME) {
                    profile.setHomeAddress(((StructuredPostalDataItem)dataItem)
                            .getFormattedAddress());
                } else if (type == StructuredPostal.TYPE_WORK) {
                    profile.setCompanyAddress(((StructuredPostalDataItem)dataItem)
                            .getFormattedAddress());
                }
            } else if (dataItem instanceof EmailDataItem) {
                profile.setEmail(((EmailDataItem)dataItem).getAddress());
            } else if (dataItem instanceof EventDataItem) {
                int type = ((EventDataItem)dataItem).getContentValues().getAsInteger(Event.TYPE);
                if (type == Event.TYPE_BIRTHDAY) {
                    profile.setBirthday(((EventDataItem)dataItem).getStartDate());
                }
            }
        }
        if (TextUtils.isEmpty(firstName)) {
            return null;
        }
        return profile;
    }

    // birthday, fixed phone number, eTag need to be extend.
    public static Profile createLocalProfile(Contact contact) {
        if (!contact.isUserProfile() || !(contact.getDirectoryAccountName() == null)) {
            return null;
        }
        RawContact rawContact = contact.getRawContacts().get(0);
        return createLocalProfile(rawContact);
    }

    private static void updateOneContactPhoto(Context context, Contact contactData,
            byte[] contactPhoto) {
        if (contactPhoto == null || contactData == null)
            return;
        for (RawContact rawContact : contactData.getRawContacts()) {
            long rawContactId = rawContact.getId();
            if (!RcsUtils.hasLocalSetted(context.getContentResolver(), rawContactId)) {
                final Uri outputUri = Uri.withAppendedPath(
                        ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                        RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
                RcsUtils.setContactPhoto(context, contactPhoto, outputUri);
            }
        }
    }

    private static class SaveLocalProfilePhotoTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        private long mRawContactId;

        private byte[] mLocalProfilePhoto;

        private RestoreFinishedListener mListener;

        SaveLocalProfilePhotoTask(Context context, long rawContactId, byte[] localProfilePhoto,
                RestoreFinishedListener listener) {
            mContext = context;
            mRawContactId = rawContactId;
            mLocalProfilePhoto = localProfilePhoto;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mLocalProfilePhoto == null)
                return null;

            Cursor c = mContext.getContentResolver().query(PROFILE_DATA_URI, new String[] {
                "data15"
            }, "raw_contact_id = ? and mimetype = ?", new String[] {
                    String.valueOf(mRawContactId), Photo.CONTENT_ITEM_TYPE
            }, null);
            if (c != null && c.getCount() > 0) {
                try {
                    c.moveToFirst();
                    byte[] contactPhoto = c.getBlob(0);
                    if (contactPhoto == null) {
                        mContext.getContentResolver().delete(PROFILE_DATA_URI,
                                "raw_contact_id = ? and mimetype = ?", new String[] {
                                        String.valueOf(mRawContactId), Photo.CONTENT_ITEM_TYPE
                                });
                    } else if (!Arrays.equals(mLocalProfilePhoto, contactPhoto)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Photo.PHOTO, mLocalProfilePhoto);
                        mContext.getContentResolver().update(PROFILE_DATA_URI, contentValues,
                                Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ?",
                                new String[] {
                                        String.valueOf(mRawContactId), Photo.CONTENT_ITEM_TYPE
                                });
                        return null;
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, mRawContactId);
            contentValues.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
            contentValues.put(Photo.PHOTO, mLocalProfilePhoto);
            mContext.getContentResolver().insert(PROFILE_DATA_URI, contentValues);
            if (mListener != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (mListener != null) {
                mListener.onRestoreFinished();
            }
        }
    }

    private static void saveMyLocalProfileText(ContentResolver resolver, long rawContactId,
            Profile profile) {

        if (profile == null)
            return;

        Cursor c = resolver.query(PROFILE_DATA_URI, new String[] {
                "mimetype", "data1", "data2", "data3", "data4", "data5"
        }, " raw_contact_id = ?  ", new String[] {
            String.valueOf(rawContactId)
        }, null);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        boolean insertCompanyTel = true;
        boolean insertCompanyFax = true;
        boolean insertHomeAddress = true;
        boolean insertCompanyAddress = true;
        boolean insertEmail = true;
        boolean updateOrganization = true;
        boolean insertOrganization = true;
        boolean updateNameInfo = true;
        boolean insertNameInfo = true;
        boolean updateBirthday = true;
        boolean insertBirthday = true;
        String middleName = "";

        ArrayList<TelephoneModel> otherTeleList = new ArrayList<TelephoneModel>(
                profile.getOtherTels());
        try {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                do {
                    String mimeType = c.getString(0);
                    if (TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimeType)) {
                        int phoneType = c.getInt(2);
                        String phoneNumber = c.getString(1);
                        if (phoneType == Phone.TYPE_WORK
                                && TextUtils.equals(phoneNumber, profile.getCompanyTel())) {
                            insertCompanyTel = false;
                        }
                        if (phoneType == Phone.TYPE_FAX_WORK
                                && TextUtils.equals(phoneNumber, profile.getCompanyFax())) {
                            insertCompanyFax = false;
                        }
                        for (TelephoneModel tel : profile.getOtherTels()) {
                            if (phoneType == Phone.TYPE_WORK
                                    && tel.getType() == TelephoneModel.TYPE_WORK
                                    && TextUtils.equals(phoneNumber, tel.getTelephone())) {
                                otherTeleList.remove(tel);
                            } else if (phoneType == Phone.TYPE_HOME
                                    && tel.getType() == TelephoneModel.TYPE_HOME
                                    && TextUtils.equals(phoneNumber, tel.getTelephone())) {
                                otherTeleList.remove(tel);
                            } else if (phoneType == Phone.TYPE_MOBILE
                                    && tel.getType() == TelephoneModel.TYPE_MOBILE
                                    && TextUtils.equals(phoneNumber, tel.getTelephone())) {
                                otherTeleList.remove(tel);
                            } else if (phoneType == Phone.TYPE_HOME
                                    && tel.getType() == TelephoneModel.TYPE_FIXED
                                    && TextUtils.equals(phoneNumber, tel.getTelephone())) {
                                otherTeleList.remove(tel);
                            } else if (phoneType == Phone.TYPE_OTHER
                                    && tel.getType() == TelephoneModel.TYPE_OTHER
                                    && TextUtils.equals(phoneNumber, tel.getTelephone())) {
                                otherTeleList.remove(tel);
                            }
                        }

                    } else if (TextUtils.equals(StructuredPostal.CONTENT_ITEM_TYPE, mimeType)) {
                        int addressType = c.getInt(2);
                        String addressInfo = c.getString(1);
                        if (addressType == StructuredPostal.TYPE_HOME
                                && TextUtils.equals(addressInfo, profile.getHomeAddress())) {
                            insertHomeAddress = false;
                        } else if (addressType == StructuredPostal.TYPE_WORK
                                && TextUtils.equals(addressInfo, profile.getCompanyAddress())) {
                            insertCompanyAddress = false;
                        }
                    } else if (TextUtils.equals(Email.CONTENT_ITEM_TYPE, mimeType)) {
                        String emailAddress = c.getString(1);
                        if (TextUtils.equals(emailAddress, profile.getEmail())) {
                            insertEmail = false;
                        }
                    } else if (TextUtils.equals(Organization.CONTENT_ITEM_TYPE, mimeType)) {
                        String companyName = c.getString(1);
                        String companyTitle = c.getString(4);
                        insertOrganization = false;
                        if (TextUtils.equals(companyName, profile.getCompanyName())
                                && TextUtils.equals(companyTitle, profile.getCompanyDuty())) {
                            updateOrganization = false;
                        }
                    } else if (TextUtils.equals(StructuredName.CONTENT_ITEM_TYPE, mimeType)) {
                        insertNameInfo = false;
                        String firstName = c.getString(2);
                        middleName = c.getString(5);
                        if (middleName == null) {
                            middleName = "";
                        }
                        String lastName = c.getString(3);
                        if (TextUtils.equals(firstName, profile.getFirstName())
                                && TextUtils.equals(lastName, profile.getLastName())) {
                            updateNameInfo = false;
                        }
                    } else if (TextUtils.equals(Event.CONTENT_ITEM_TYPE, mimeType)) {
                        int eventType = c.getInt(2);
                        if (eventType == Event.TYPE_BIRTHDAY) {
                            String startDate = c.getString(1);
                            insertBirthday = false;
                            if (TextUtils.equals(startDate, profile.getBirthday())) {
                                updateBirthday = false;
                            }
                        }
                    }
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (insertCompanyTel && !TextUtils.isEmpty(profile.getCompanyTel())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            contentValues.put(Phone.TYPE, Phone.TYPE_WORK);
            contentValues.put(Phone.NUMBER, profile.getCompanyTel());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }
        if (insertCompanyFax && !TextUtils.isEmpty(profile.getCompanyFax())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            contentValues.put(Phone.TYPE, Phone.TYPE_FAX_WORK);
            contentValues.put(Phone.NUMBER, profile.getCompanyFax());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }
        if (insertHomeAddress && !TextUtils.isEmpty(profile.getHomeAddress())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
            contentValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME);
            contentValues.put(StructuredPostal.FORMATTED_ADDRESS, profile.getHomeAddress());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }
        if (insertCompanyAddress && !TextUtils.isEmpty(profile.getCompanyAddress())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
            contentValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
            contentValues.put(StructuredPostal.FORMATTED_ADDRESS, profile.getCompanyAddress());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }
        if (insertEmail && !TextUtils.isEmpty(profile.getEmail())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            contentValues.put(Email.TYPE, Email.TYPE_HOME);
            contentValues.put(Email.ADDRESS, profile.getEmail());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }

        if (insertOrganization) {
            if (!TextUtils.isEmpty(profile.getCompanyName())
                    || !TextUtils.isEmpty(profile.getCompanyDuty())) {
                RcsLog.d("insert organization");
                ContentValues contentValues = new ContentValues();
                contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
                contentValues.put(Organization.COMPANY, profile.getCompanyName());
                contentValues.put(Organization.TITLE, profile.getCompanyDuty());
                contentValues.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
                ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI)
                        .withValues(contentValues).build());
            }
        } else if (updateOrganization) {
            if (TextUtils.isEmpty(profile.getCompanyName())
                    && TextUtils.isEmpty(profile.getCompanyDuty())) {
                ops.add(ContentProviderOperation
                        .newDelete(PROFILE_DATA_URI)
                        .withSelection(
                                Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ",
                                new String[] {
                                        String.valueOf(rawContactId),
                                        Organization.CONTENT_ITEM_TYPE
                                }).build());
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Organization.COMPANY, profile.getCompanyName());
                contentValues.put(Organization.TITLE, profile.getCompanyDuty());
                ops.add(ContentProviderOperation
                        .newUpdate(PROFILE_DATA_URI)
                        .withValues(contentValues)
                        .withSelection(
                                Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ",
                                new String[] {
                                        String.valueOf(rawContactId),
                                        Organization.CONTENT_ITEM_TYPE
                                }).build());
            }
        }

        if (insertNameInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(StructuredName.GIVEN_NAME, profile.getFirstName());
            contentValues.put(StructuredName.FAMILY_NAME, profile.getLastName());
            StringBuilder displayName = new StringBuilder();
            displayName.append(profile.getFirstName());
            displayName.append(" ");
            displayName.append(middleName);
            displayName.append(" ");
            displayName.append(profile.getLastName());
            contentValues.put(StructuredName.DISPLAY_NAME, displayName.toString());
            contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        } else if (updateNameInfo) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(StructuredName.GIVEN_NAME, profile.getFirstName());
            contentValues.put(StructuredName.FAMILY_NAME, profile.getLastName());
            StringBuilder displayName = new StringBuilder();
            displayName.append(profile.getFirstName());
            displayName.append(" ");
            displayName.append(middleName);
            displayName.append(" ");
            displayName.append(profile.getLastName());
            contentValues.put(StructuredName.DISPLAY_NAME, displayName.toString());
            ops.add(ContentProviderOperation
                    .newUpdate(PROFILE_DATA_URI)
                    .withValues(contentValues)
                    .withSelection(Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ",
                            new String[] {
                                    String.valueOf(rawContactId), StructuredName.CONTENT_ITEM_TYPE
                            }).build());
        }

        if (insertBirthday && !TextUtils.isEmpty(profile.getBirthday())) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Event.TYPE, Event.TYPE_BIRTHDAY);
            contentValues.put(Event.START_DATE, profile.getBirthday());
            contentValues.put(Data.MIMETYPE, Event.CONTENT_ITEM_TYPE);
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        } else if (updateBirthday) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Event.START_DATE, profile.getBirthday());
            ops.add(ContentProviderOperation
                    .newUpdate(PROFILE_DATA_URI)
                    .withValues(contentValues)
                    .withSelection(Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ? ",
                            new String[] {
                                    String.valueOf(rawContactId), Event.CONTENT_ITEM_TYPE
                            }).build());
        }

        for (TelephoneModel tel : otherTeleList) {
            int phoneType = Phone.TYPE_HOME;
            if (tel.getType() == TelephoneModel.TYPE_HOME
                    || tel.getType() == TelephoneModel.TYPE_FIXED) {
                phoneType = Phone.TYPE_HOME;
            } else if (tel.getType() == TelephoneModel.TYPE_MOBILE) {
                phoneType = Phone.TYPE_MOBILE;
            } else if (tel.getType() == TelephoneModel.TYPE_WORK) {
                phoneType = Phone.TYPE_WORK;
            } else {
                phoneType = Phone.TYPE_OTHER;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
            contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            contentValues.put(Phone.TYPE, phoneType);
            contentValues.put(Phone.NUMBER, tel.getTelephone());
            ops.add(ContentProviderOperation.newInsert(PROFILE_DATA_URI).withValues(contentValues)
                    .build());
        }
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static String getMyPhoneNumber() {
        String myAccountNumber = "";
        try {
            RcsLog.d("Calling  BasicApi.getInstance().getAccount()");
            myAccountNumber = BasicApi.getInstance().getAccount();
        } catch (ServiceDisconnectedException e) {
            RcsLog.w(e);
            e.printStackTrace();
        } catch (RemoteException e) {
            RcsLog.w(e);
            e.printStackTrace();
        }
        return myAccountNumber;
    }

    public static void updateOnlyOneContactPhoto(Context context, Contact contact,
            byte[] contactPhoto, final RestoreFinishedListener listener, Handler handler) {
        if (contactPhoto == null)
            return;
        ImmutableList<RawContact> rawContacts = contact.getRawContacts();
        for (RawContact rawContact : rawContacts) {
            long rawContactId = rawContact.getId();
            if (!RcsUtils.hasLocalSetted(context.getContentResolver(), rawContactId)) {
                final Uri outputUri = Uri.withAppendedPath(
                        ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                        RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
                RcsUtils.setContactPhoto(context, contactPhoto, outputUri);
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onRestoreFinished();
                }
            }
        });
    }

    public static void saveMyLocalProfilePhoto(Context context, long rawContactId,
            byte[] localProfilePhoto, final RestoreFinishedListener listener, Handler handler) {
        if (localProfilePhoto == null)
            return;
        Cursor c = context.getContentResolver().query(PROFILE_DATA_URI, new String[] {
            "data15"
        }, "raw_contact_id = ? and mimetype = ?", new String[] {
                String.valueOf(rawContactId), Photo.CONTENT_ITEM_TYPE
        }, null);
        if (c != null && c.getCount() > 0) {
            try {
                c.moveToFirst();
                byte[] contactPhoto = c.getBlob(0);
                if (contactPhoto == null) {
                    context.getContentResolver().delete(PROFILE_DATA_URI,
                            "raw_contact_id = ? and mimetype = ?", new String[] {
                                    String.valueOf(rawContactId), Photo.CONTENT_ITEM_TYPE
                            });
                } else if (!Arrays.equals(localProfilePhoto, contactPhoto)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Photo.PHOTO, localProfilePhoto);
                    context.getContentResolver().update(PROFILE_DATA_URI, contentValues,
                            Data.RAW_CONTACT_ID + " = ? and " + Data.MIMETYPE + " = ?",
                            new String[] {
                                    String.valueOf(rawContactId), Photo.CONTENT_ITEM_TYPE
                            });
                    return;
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(Data.RAW_CONTACT_ID, rawContactId);
        contentValues.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        contentValues.put(Photo.PHOTO, localProfilePhoto);
        context.getContentResolver().insert(PROFILE_DATA_URI, contentValues);
        if (listener != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRestoreFinished();
                }
            });
        }
    }

    private static void restoreLocalProfileInfo(final Context context, final Contact contactData,
            final RestoreFinishedListener listener) {
        final Handler handler = new Handler();
        try {
            ProfileApi.getInstance().getMyProfile(new ProfileListener() {
                @Override
                public void onAvatarGet(Avatar arg0, int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onAvatarUpdated(int arg0, String arg1) throws RemoteException {
                }

                @Override
                public void onProfileGet(final Profile profile, final int resultCode,
                        final String resultDesc) throws RemoteException {
                    if (resultCode == 0) {
                        SharedPreferences myProfileSharedPreferences = context
                                .getSharedPreferences("RcsSharepreferences", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myProfileSharedPreferences.edit();
                        editor.putString(PREF_RCS_PROFILE_TEXT_ETAG, profile.getEtag());
                        editor.commit();
                        RcsLog.d("download ProfileTextEtag: " + profile.getEtag());
                        for (RawContact rawContact : contactData.getRawContacts()) {
                            saveMyLocalProfileText(context.getContentResolver(),
                                    rawContact.getId(), profile);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                makeToast(context, R.string.get_text_profile_successfully);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                makeToast(context, R.string.get_text_profile_failed);
                            }
                        });
                    }
                }

                @Override
                public void onProfileUpdated(int arg0, String arg1) throws RemoteException {
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {
                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        RcsUtils.sleep(1000);
        try {
            ProfileApi.getInstance().getMyHeadPic(new ProfileListener() {

                @Override
                public void onAvatarGet(final Avatar photo, final int resultCode,
                        final String resultDesc) throws RemoteException {
                    if (resultCode == 0) {
                        if (photo != null) {
                            SharedPreferences myProfileSharedPreferences = context
                                    .getSharedPreferences("RcsSharepreferences",
                                            Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = myProfileSharedPreferences.edit();
                            editor.putString(PREF_RCS_PROFILE_PHOTO_ETAG, photo.getEtag());
                            editor.commit();
                            RcsLog.d("download ProfilePotoEtag: " + photo.getEtag());
                            byte[] localProfilePhoto = Base64.decode(photo.getImgBase64Str(),
                                    android.util.Base64.DEFAULT);
                            for (RawContact rawContact : contactData.getRawContacts()) {
                                saveMyLocalProfilePhoto(context, rawContact.getId(),
                                        localProfilePhoto, listener, handler);

                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    makeToast(context, R.string.get_photo_profile_successfully);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    makeToast(context, R.string.get_photo_profile_failed);
                                }
                            });
                        }
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                makeToast(context, R.string.get_photo_profile_failed);
                            }
                        });
                    }
                }

                @Override
                public void onAvatarUpdated(int arg0, String arg1) throws RemoteException {
                }

                @Override
                public void onProfileGet(Profile arg0, int arg1, String arg2)
                        throws RemoteException {
                }

                @Override
                public void onProfileUpdated(int arg0, String arg1) throws RemoteException {
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {
                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void backupLocalProfileInfo(final Context context, final Profile profile,
            Avatar photoInfo) {
        final Handler handler = new Handler();
        try {
            ProfileApi.getInstance().setMyProfile(profile, new ProfileListener() {

                @Override
                public void onAvatarGet(Avatar arg0, int arg1, String arg2)
                        throws RemoteException {
                }

                @Override
                public void onAvatarUpdated(int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onProfileGet(Profile arg0, int arg1, String arg2)
                        throws RemoteException {
                }

                @Override
                public void onProfileUpdated(final int resultCode, final String resultDesc)
                        throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (resultCode == 0) {
                                getQRcodeFromService(profile, context);
                                makeToast(context, R.string.upload_text_profile_successfully);
                            } else {
                                makeToast(context, R.string.upload_text_profile_failed);
                            }
                        }
                    });
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {
                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            ProfileApi.getInstance().setMyHeadPic(photoInfo, new ProfileListener() {

                @Override
                public void onAvatarGet(Avatar arg0, int arg1, String arg2) throws RemoteException {

                }

                @Override
                public void onAvatarUpdated(final int resultCode, final String resultDesc)
                        throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (resultCode == 0) {
                                makeToast(context, R.string.upload_photo_profile_successfully);
                            } else {
                                makeToast(context, R.string.upload_photo_profile_failed);
                            }
                        }
                    });

                }

                @Override
                public void onProfileGet(Profile arg0, int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onProfileUpdated(int arg0, String arg1) throws RemoteException {
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {

                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void getOneContactPhotoFromServer(
            final WeakReference<QuickContactActivity> activityRef,
            final WeakReference<Contact> contactRef, final ProfileApi profileApi,
            final RestoreFinishedListener listener) {
        QuickContactActivity activity = activityRef.get();
        Contact ContactData = contactRef.get();
        if (activity == null || ContactData == null || profileApi == null) {
            return;
        }
        final long contactId = ContactData.getRawContacts().get(0).getContactId();
        final Handler handler = new Handler();
        final Context appContext = activity.getApplicationContext();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    QuickContactActivity activity = activityRef.get();
                    if (activity == null || activity.isFinishing()) {
                        return;
                    }
                    profileApi.getHeadPicByContact(contactId, new ProfileListener() {
                        @Override
                        public void onAvatarGet(final Avatar photo, final int resultCode,
                                final String resultDesc) throws RemoteException {
                            QuickContactActivity activity = activityRef.get();
                            Contact contactData = contactRef.get();
                            if (resultCode == 0) {
                                final byte[] contactPhoto = Base64.decode(photo.getImgBase64Str(),
                                        android.util.Base64.DEFAULT);
                                if (activity == null || activity.isFinishing()
                                        || contactData == null) {
                                    return;
                                }
                                updateOneContactPhoto(appContext, contactData, contactPhoto);
                                sleep(1000);
                                if (activity == null || activity.isFinishing()) {
                                    return;
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null) {
                                            listener.onRestoreFinished();

                                        }
                                    }
                                });
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeToast(appContext,
                                                R.string.get_photo_profile_successfully);
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeToast(appContext, R.string.get_photo_profile_failed);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onAvatarUpdated(int arg0, String arg1) throws RemoteException {
                        }

                        @Override
                        public void onProfileGet(Profile arg0, int arg1, String arg2)
                                throws RemoteException {
                        }

                        @Override
                        public void onProfileUpdated(int arg0, String arg1) throws RemoteException {
                        }

                        @Override
                        public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                                throws RemoteException {
                        }
                    });
                } catch (ServiceDisconnectedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public static String getAddressesStringByGroupId(long groupId) {
        String address = "";
        try {
            List<GroupChatMember> groupChatMembers = GroupChatApi.getInstance()
                    .getMembers(groupId);
            if (groupChatMembers != null) {
                for (int i = 0, size = groupChatMembers.size(); i < size; i++) {
                    GroupChatMember number = groupChatMembers.get(i);
                    address += number.getNumber();
                    if (i + 1 < size) {
                        address += ";";
                    }
                }
            }
        } catch (ServiceDisconnectedException e) {
            RcsLog.w(e);
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return address;
    }

    public static long getThreadIdByGroupId(Context context, long groupId) {
        long threadId = 0;

        if (groupId == -1) {
            return threadId;
        }
        GroupChat groupChat = null;
        try {
            groupChat = GroupChatApi.getInstance().getGroupChatById(groupId);
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (groupChat == null) {
            return threadId;
        }

        threadId = groupChat.getThreadId();

        return threadId;
    }

    public static boolean isActivityIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean getCompanyFromProfile(Profile profile) {
        if (!TextUtils.isEmpty(profile.getCompanyName())
                || !TextUtils.isEmpty(profile.getCompanyDuty())
                        || !TextUtils.isEmpty(profile.getCompanyTel())
                        || !TextUtils.isEmpty(profile.getCompanyAddress())
                        || !TextUtils.isEmpty(profile.getCompanyFax())) {
            return true;
        }
        return false;
    }

    public static void getQRcodeFromService(Profile profile, final Context context) {
        RcsLog.d("getQRcodeFromService");
        SharedPreferences myQrcodeSharedPreferences = context.getSharedPreferences(
                "QrcodePersonalCheckState", Activity.MODE_PRIVATE);
        boolean isBInfo = myQrcodeSharedPreferences.getBoolean("isHasBusiness", false);
        try {
            ProfileApi.getInstance().refreshMyQRImg(profile, isBInfo, new ProfileListener() {
                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {

                }

                public void onQRImgGet(QRCardImg imgObj, int resultCode, String arg2) {
                    RcsLog.d("get qrcode resultCode= " + resultCode);
                    if (resultCode == 0) {
                        if (imgObj != null && !TextUtils.isEmpty(imgObj.getImgBase64Str())) {
                            byte[] imageByte = Base64.decode(imgObj.getImgBase64Str(),
                                    Base64.DEFAULT);
                            final Bitmap qrcodeBitmap = BitmapFactory.decodeByteArray(imageByte, 0,
                                    imageByte.length);
                            if (qrcodeBitmap != null) {
                                saveQrCode(context, imgObj.getImgBase64Str(), imgObj.getEtag());
                            }
                        }
                    }
                }

                @Override
                public void onProfileUpdated(int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onAvatarUpdated(int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onAvatarGet(Avatar avatar, int resultCode, String resultDesc)
                        throws RemoteException {
                }

                @Override
                public void onProfileGet(Profile profile, int resultCode, String resultDesc)
                        throws RemoteException {
                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }

    private static boolean needGetPhotoFromServer(Context context, Contact contactData) {
        for (RawContact rawContact : contactData.getRawContacts()) {
            if (!RcsUtils.hasLocalSetted(context.getContentResolver(), rawContact.getId())) {
                return true;
            }
        }
        return false;
    }

    public static void updateContactPhotoViaServer(
            final WeakReference<QuickContactActivity> activityRef,
            final WeakReference<Contact> contactRef) {
        QuickContactActivity activity = activityRef.get();
        Contact contactData = contactRef.get();
        if (activity == null || contactData == null) {
            return;
        }
        if (RcsUtils.needGetPhotoFromServer(activity.getApplicationContext(), contactData)) {
            RcsUtils.getOneContactPhotoFromServer(activityRef, contactRef,
                    ProfileApi.getInstance(), new RestoreFinishedListener() {
                        public void onRestoreFinished() {
                            QuickContactActivity activity = activityRef.get();
                            Contact contactData = contactRef.get();
                            if (activity != null && !activity.isFinishing() && activity.isResumed()
                                    && contactData != null) {
                                Intent resultIntent = QuickContact.composeQuickContactsIntent(
                                        activity.getBaseContext(), (Rect)null,
                                        contactData.getLookupUri(),
                                        QuickContactActivity.MODE_FULLY_EXPANDED, null);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                activity.startActivity(resultIntent);
                            }
                        }
                    });
        }
    }

    public static void initRcsMenu(Context context, Menu menu, Contact contactData) {
        boolean isRcsSupport = RcsApiManager.getSupportApi().isRcsSupported();
        boolean isUserProfile = contactData != null && contactData.isUserProfile();

        final MenuItem optionsQrcode = menu.findItem(R.id.menu_qrcode);
        if (optionsQrcode != null) {
            optionsQrcode.setVisible(isRcsSupport && isUserProfile);
        }

        final MenuItem optionsPluginCenter = menu.findItem(R.id.menu_plugin_center);
        if (optionsPluginCenter != null) {
            optionsPluginCenter.setVisible(isRcsSupport && isPlunginCenterInstalled(context)
                    && isUserProfile);
        }
        final MenuItem optionsUpdateEnhanceScreen = menu.findItem(R.id.menu_updateenhancedscreen);
        if (optionsUpdateEnhanceScreen != null) {
            optionsUpdateEnhanceScreen.setVisible(isRcsSupport && isEnhanceScreenInstalled(context)
                    && !isUserProfile);
        }
        final MenuItem optionsEnhancedscreen = menu.findItem(R.id.menu_enhancedscreen);
        if (optionsEnhancedscreen != null) {
            optionsEnhancedscreen.setVisible(isRcsSupport && isEnhanceScreenInstalled(context));
        }
        // Display/Hide the online business hall menu item.
        MenuItem onlineBusinessHall = menu.findItem(R.id.menu_online_business_hall);
        if (onlineBusinessHall != null) {
            onlineBusinessHall.setVisible(isRcsSupport && isUserProfile
                    && isOnlineBusinessHallInstalled(context));
        }
    }

    public static void startQrCodeActivity(Context context, Contact contactData) {
        Intent intent = new Intent(context, MyQrcodeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("contact_name", contactData.getDisplayName());
        RawContact rawContact = contactData.getRawContacts().get(0);
        bundle.putParcelable("raw_contact", rawContact);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private static ArrayList<String> getcontactPhoneList(Contact contactData) {
        String contactPhone = null;
        ArrayList<String> contactsPhoneList = new ArrayList<String>();
        if (contactData == null) {
            return contactsPhoneList;
        }
        for (RawContact raw : contactData.getRawContacts()) {
            for (DataItem dataItem : raw.getDataItems()) {
                final ContentValues entryValues = dataItem.getContentValues();
                final String mimeType = dataItem.getMimeType();
                if (mimeType == null)
                    continue;
                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    contactPhone = entryValues.getAsString(Phone.NUMBER);
                    if (contactPhone == null)
                        continue;
                    contactsPhoneList.add(getFormatNumber(contactPhone));
                }
            }
        }
        return contactsPhoneList;
    }

    public static void setEnhanceScreen(Context context, Contact contactData) {
        try {
            if (getcontactPhoneList(contactData).size() < 1) {
                makeToast(context, R.string.Unformatted_profile_phone_number);
            } else {
                    if (contactData.isUserProfile()) {
                        RichScreenApi.getInstance()
                                .startRichScreenApp(new ArrayList<String>());
                    } else {
                        RichScreenApi.getInstance().startRichScreenApp(
                                getcontactPhoneList(contactData));
                    }
            }
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void updateEnhanceScreeenFunction(Context context, Contact contactData) {
        try {
            if (getcontactPhoneList(contactData).size() < 1) {
                makeToast(context, R.string.Unformatted_profile_phone_number);
            } else {
                if (!RcsUtils.isNetworkConnected(context)) {
                    makeToast(context, R.string.rcs_network_uanvailable);
                    return;
                }
                ArrayList<String> phoneNumberList = getcontactPhoneList(contactData);
                for (int i = 0; i < phoneNumberList.size(); i++) {
                    if (!TextUtils.isEmpty(phoneNumberList.get(i))) {
                        RcsLog.d(phoneNumberList.get(i));
                        RichScreenApi.getInstance().downloadRichScrnObj(phoneNumberList.get(i),
                                    RcsUtils.UPDATE_ENHANCE_SCREEN_PHONE_EVENT);
                    }
                }
                makeToast(context, R.string.rcs_updateting_enhance_screen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isLocalProfile(Contact contactData) {
        return TextUtils.isEmpty(contactData.getDirectoryAccountName())
                && contactData.isUserProfile();
    }

    public static void startCreateGroupChatActivity(Context context, String number, String message) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", message);
        if (!TextUtils.isEmpty(number)) {
            sendIntent.putExtra("address", number);
        }
        sendIntent.putExtra("isGroupChat", true);
        sendIntent.setComponent(new ComponentName("com.android.mms",
                "com.android.mms.ui.ComposeMessageActivity"));
        if (RcsUtils.isActivityIntentAvailable(context, sendIntent)) {
            context.startActivity(sendIntent);
        }
    }

    public static Cursor queryPhoneNumbers(Context context, long contactId) {
        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

        Cursor c = context.getContentResolver().query(dataUri, new String[] {
                Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY
        }, Data.MIMETYPE + "=?", new String[] {
            Phone.CONTENT_ITEM_TYPE
        }, null);
        if (c != null && c.moveToFirst()) {
            return c;
        }
        return null;
    }

    public static String getPhoneforContactId(Context context, long contactId) {

        String phone = "";
        Cursor phonesCursor = null;
        phonesCursor = RcsUtils.queryPhoneNumbers(context, contactId);
        try {
            if (null != phonesCursor) {
                if (phonesCursor.getCount() == 0) {
                    // No valid number
                    if (!phonesCursor.isClosed()) {
                        phonesCursor.close();
                    }
                    return phone;
                } else if (phonesCursor.getCount() == 1) {
                    // only one number, call it.
                    phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                } else {
                    phonesCursor.moveToPosition(-1);
                    boolean first = true;
                    while (phonesCursor.moveToNext()) {
                        if (first) {
                            phone = phonesCursor.getString(phonesCursor
                                    .getColumnIndex(Phone.NUMBER));
                            first = false;
                        }
                        if (phonesCursor.getInt(phonesCursor.getColumnIndex

                        (Phone.IS_SUPER_PRIMARY)) != 0) {
                            // Found super primary, call it.
                            phone = phonesCursor.getString(phonesCursor
                                    .getColumnIndex(Phone.NUMBER));
                            break;
                        }
                    }
                }
            }
        } finally {
            if (null != phonesCursor) {
                phonesCursor.close();
            }
        }
        return phone;
    }

    public static String getAllPhoneNumberFromContactId(Context context, long contactId) {

        String phone = "";
        Cursor phonesCursor = null;
        StringBuilder sb = new StringBuilder();
        phonesCursor = RcsUtils.queryPhoneNumbers(context, contactId);

        try {
            if (phonesCursor != null) {
                if (phonesCursor.getCount() == 0) {
                    phone = "";
                } else if (phonesCursor.getCount() == 1) {
                    // only one number, call it.
                    phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                } else {
                    while (phonesCursor.moveToNext()) {
                        phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                        sb.append(phone).append(";");
                    }
                    phone = sb.toString();
                }
            }
        } finally {
            if (null != phonesCursor) {
                phonesCursor.close();
            }

        }
        return phone;
    }

    public static void startChatGroupManagementActivity(Context context, GroupListItem entry) {
        long groupId = entry.getGroupId();
        long threadId = RcsUtils.getThreadIdByGroupId(context, groupId);
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (threadId > 0) {
            intent.setData(uri);
        }
        intent.putExtra("groupId", groupId);
        intent.putExtra("isGroupChat", true);
        String address = RcsUtils.getAddressesStringByGroupId(groupId);
        if (!TextUtils.isEmpty(address)) {
            intent.putExtra("address", address);
        }
        intent.setComponent(new ComponentName("com.android.mms",
                "com.android.mms.ui.ComposeMessageActivity"));
        context.startActivity(intent);
    }

    public static int getMessageChatCount(int position) {
        int size = 0;
        try {
            List<GroupChatMember> groupChatMembers = GroupChatApi.getInstance()
                    .getMembers(position);
            if (null != groupChatMembers) {
                size = groupChatMembers.size();
            }
        } catch (ServiceDisconnectedException e) {
            RcsLog.w("Exception initRcsComponents()" + e);
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getFormatNumber(String number) {
        if (null == number) {
            return "";
        }
        number = number.replaceAll("-", "");
        number = number.replaceAll(" ", "");
        number = number.replaceAll(",", "");
        int numberLen = number.length();
        if (numberLen > DEFAULT_NUMBER_LENGTH) {
            number = number.substring(numberLen - DEFAULT_NUMBER_LENGTH, numberLen);
        }
        return number;
    }

    public static void autoBackupOnceChanged(final Context context) {
        final Handler handler = new Handler();
        Thread t = new Thread() {
            @Override
            public void run() {
                RcsUtils.sleep(2000);
                backupContacts(context, handler);
            }
        };
        t.start();
    }

    private static void backupContacts(final Context context, final Handler handler) {
        Context nativeUiContext = null;
        try {
            nativeUiContext = context.createPackageContext(NATIVE_UI_PACKAGE,
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (nativeUiContext == null)
            return;
        SharedPreferences backupRestorePref = nativeUiContext.getSharedPreferences(
                PREF_BACKUP_RESTORE_NAME, Activity.MODE_WORLD_READABLE
                        | Activity.MODE_MULTI_PROCESS);
        boolean isBackup = backupRestorePref.getBoolean(KEY_BACKUP_ONCE_CHANGED, false);
        boolean isAutoBackup = backupRestorePref.getBoolean(KEY_AUTO_BACKUP, false);
        boolean isOnlySyncViaWifi = backupRestorePref.getBoolean(KEY_ONLY_WIFI_BACKUP_RESOTORE,
                false);
        RcsLog.d("Calling autoBackupOnceChanged!");
        try {
            if (isBackup && isAutoBackup
                 && (isOnlySyncViaWifi && RcsUtils.isWifiEnabled(context) || !isOnlySyncViaWifi)) {
                RcsLog.d("Auto backup started!");
                ContactApi.getInstance().doSync(SyncAction.CONTACT_UPLOAD,
                        new IContactSyncListener.Stub() {

                            @Override
                            public void onAuthSession(Auth arg0, boolean arg1)
                                    throws RemoteException {

                            }

                            @Override
                            public void onExecuting(Auth arg0, int arg1) throws RemoteException {

                            }

                            @Override
                            public void onHttpResponeText(String arg0, String arg1)
                                    throws RemoteException {

                            }

                            @Override
                            public void onPreExecuteAuthSession(Auth arg0) throws RemoteException {

                            }

                            @Override
                            public void onProgress(Auth arg0, int arg1, int arg2, int arg3)
                                    throws RemoteException {

                            }

                            @Override
                            public void onRunning() throws RemoteException {

                            }

                            @Override
                            public void onThrowException(Auth auth, int syncAction,
                                    String exceptionMessage) {
                                RcsLog.e("Exception: " + exceptionMessage);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeToast(context, R.string.contact_backup_fail);
                                    }
                                });
                            }

                            @Override
                            public void onSync(Auth auto, final int action, final boolean isSuccess)
                                    throws RemoteException {
                                if (isSuccess) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            makeToast(context, R.string.contact_backup_success);
                                        }
                                    });
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            makeToast(context, R.string.contact_backup_fail);
                                        }
                                    });
                                }
                            }
                        });
            }
        } catch (ServiceDisconnectedException e) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    makeToast(context, R.string.rcs_service_is_not_available);
                }
            });
            RcsLog.w(e);
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void startOnlineBusinessHallActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(ONLINE_BUSINESS_HALL);
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    private static void makeToast(Context context, int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }

    public static void findContactsCapacity(final Context context, final long contactId,
            final long rawContactId, final String phoneNumber, final Handler handler) {
        try {
            CapabilityApi.getInstance().getCapability(phoneNumber, true, new CapabiltyListener() {
                @Override
                public void onCallback(RCSCapabilities arg0, int resultCode, String resultDesc,
                        String respPhoneNumber) throws RemoteException {
                    if (resultCode == RCS_SUCESS || resultCode == RCS_OFFLINE) {
                         ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, true);
                        insertRcsCapa(context, contactId, rawContactId, 1);
                    } else if (resultCode == NOT_RCS) {
                        if (! ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.containsKey(contactId)) {
                             ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, false);
                        }
                        insertRcsCapa(context, contactId, rawContactId, 0);
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                makeToast(context, R.string.rcs_capability_query_failed);
                            }
                        });
                        if (! ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.containsKey(contactId)) {
                             ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, false);
                        }
                        insertRcsCapa(context, contactId, rawContactId, 0);
                    }
                }
            });
        } catch (ServiceDisconnectedException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    makeToast(context, R.string.rcs_service_is_not_available);
                }
            });
            if (! ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.containsKey(contactId)) {
                 ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, false);
            }
            insertRcsCapa(context, contactId, rawContactId, 0);
            e.printStackTrace();
        } catch (RemoteException e) {
            if (! ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.containsKey(contactId)) {
                 ContactsCommonRcsUtil.RCS_CAPABILITY_MAP_CACHE.put(contactId, false);
            }
            insertRcsCapa(context, contactId, rawContactId, 0);
            e.printStackTrace();
        }
    }

    private static void doBackupLocalProfileInfo(Context context, Contact contactData) {

        Profile profile = createLocalProfile(contactData);

        if (profile == null) {
            makeToast(context, R.string.first_last_name_empty);
            return;
        }
        SharedPreferences myProfileSharedPreferences = context.getSharedPreferences(
                PREF_RCS_FILE_NAME, Activity.MODE_WORLD_READABLE);
        String TextEtag = myProfileSharedPreferences.getString(PREF_RCS_PROFILE_TEXT_ETAG, null);
        profile.setEtag(TextEtag);

        String myAccountNumber = RcsUtils.getMyPhoneNumber();
        if (TextUtils.isEmpty(myAccountNumber)) {
            makeToast(context, R.string.account_empty);
            return;
        }
        RcsLog.d("The account is " + myAccountNumber);
        Avatar photoInfo = new Avatar();
        photoInfo.setAccount(myAccountNumber);
        photoInfo.setAvatarImgType(IMAGE_TYPE.PNG);
        byte[] contactPhoto = contactData.getPhotoBinaryData();
        if (contactPhoto == null) {
            makeToast(context, R.string.photo_empty);
            return;
        }
        String PhotoEtag = myProfileSharedPreferences.getString(PREF_RCS_PROFILE_PHOTO_ETAG, null);
        photoInfo.setEtag(PhotoEtag);
        photoInfo
                .setImgBase64Str(Base64.encodeToString(processPhoto(contactPhoto), Base64.DEFAULT));
        backupLocalProfileInfo(context, profile, photoInfo);
    }

    private static void okToRestoreLocalProfile(Context context, int whichButton,
            Contact contactData, RestoreFinishedListener listener) {
        if (contactData == null)
            return;
        int BACKUP = 0, RESTORE = 1;
        if (whichButton == BACKUP) {
            doBackupLocalProfileInfo(context, contactData);
        }
        if (whichButton == RESTORE) {
            restoreLocalProfileInfo(context, contactData, listener);
        }
    }

    public static void saveProfilePhoto(final Context context, final Handler handler,
            final Avatar photo, final int resultCode, final String resultDesc,
            final ArrayList<Long> rawContactIdList) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (resultCode == 0) {
                    if (photo != null) {
                        byte[] contactPhoto = Base64.decode(photo.getImgBase64Str(),
                                android.util.Base64.DEFAULT);
                        for (long rawContactId : rawContactIdList) {
                            final Uri outputUri = Uri.withAppendedPath(ContentUris.withAppendedId(
                                    RawContacts.CONTENT_URI, rawContactId),
                                    RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
                            setContactPhoto(context, contactPhoto, outputUri);
                        }
                    }
                    makeToast(context, R.string.get_photo_profile_successfully);
                } else {
                    makeToast(context, R.string.get_photo_profile_failed);
                }
            }
        });
    }

    private static final int MAX_LENGTH = 20;

    public static boolean judgeUserNameLength(Context context,
            RawContactDeltaList rawContactDeltaList, boolean isExpand) {
        if (!RcsApiManager.getSupportApi().isRcsSupported()) {
            return true;
        }
        RawContactDelta rawContactDelta = rawContactDeltaList.get(0);
        ArrayList<ValuesDelta> names = rawContactDelta
                .getMimeEntries(StructuredName.CONTENT_ITEM_TYPE);
        ValuesDelta nameValuesDelta = names.get(0);
        String firstName = nameValuesDelta.getAsString(StructuredName.FAMILY_NAME);
        String lastName = nameValuesDelta.getAsString(StructuredName.GIVEN_NAME);
        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && !isExpand) {
            String displayName = nameValuesDelta.getAsString(StructuredName.DISPLAY_NAME);
            if (TextUtils.isEmpty(displayName)) {
                displayName = "";
            }
            if (displayName.getBytes().length > MAX_LENGTH * 2) {
                Toast.makeText(context, R.string.full_name_max_length, Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
        if (TextUtils.isEmpty(firstName)) {
            firstName = "";
        }
        if (TextUtils.isEmpty(lastName)) {
            lastName = "";
        }
        if (!isExpand) {
            if ((firstName + lastName).getBytes().length > MAX_LENGTH * 2) {
                Toast.makeText(context, R.string.full_name_max_length, Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            if (firstName.getBytes().length > MAX_LENGTH) {
                Toast.makeText(context, R.string.first_name_max_length, Toast.LENGTH_LONG).show();
                return false;
            }
            if (lastName.getBytes().length > MAX_LENGTH) {
                Toast.makeText(context, R.string.last_name_max_length, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    public static boolean isGroupNameValid(Context context,
               String groupName) {
       if (!RcsApiManager.getSupportApi().isRcsSupported()) {
           return true;
       }
       if (!TextUtils.isEmpty(groupName)) {
           if (groupName.getBytes().length > MAX_LENGTH) {
               Toast.makeText(context, R.string.group_name_max_length, Toast.LENGTH_LONG).show();
               return false;
           }
       }
       return true;
    }

    public static boolean isRegularEmail(String emailString) {
        String patten = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]"
                + "\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        Pattern p = Pattern.compile(patten);
        Matcher m = p.matcher(emailString);
        return m.matches();
    }

    public static boolean checkNumberInFirewall(ContentResolver resolver, boolean isBlacklist,
            String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        String queryNumber = number.replaceAll("[\\-\\/ ]", "");
        int len = queryNumber.length();
        if (len > 11) {
            queryNumber = number.substring(len - 11, len);
        }
        Uri firewallUri = isBlacklist ? BLACKLIST_CONTENT_URI : WHITELIST_CONTENT_URI;
        Cursor fiewallCursor = resolver.query(firewallUri, new String[] {
                "_id", "number", "person_id", "name"
        }, "number" + " LIKE '%" + queryNumber + "'", null, null);
        try {
            if (fiewallCursor != null && fiewallCursor.getCount() > 0) {
                return false;
            }
        } finally {
            if (fiewallCursor != null) {
                fiewallCursor.close();
                fiewallCursor = null;
            }
        }
        return true;
    }
}
