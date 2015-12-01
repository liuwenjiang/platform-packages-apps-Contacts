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
package com.android.contacts.quickcontact;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import com.android.contacts.common.model.RawContact;
import android.content.SharedPreferences;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;
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
import com.android.contacts.common.model.dataitem.PhoneDataItem;
import com.android.contacts.common.model.dataitem.RelationDataItem;
import com.android.contacts.common.model.dataitem.SipAddressDataItem;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.R;
import com.android.contacts.util.RcsUtils;
import com.suntek.mway.rcs.client.api.profile.ProfileApi;
import com.suntek.mway.rcs.client.api.profile.ProfileListener;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardImg;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardInfo;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.api.basic.BasicApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.rcs.ui.common.RcsLog;

/**
 *
 * UI to show my profile QRcode
 *
 */

public class MyQrcodeActivity extends Activity {

    private static final String TAG = "MyQrcodeActivity";
    private static final int QRCODE_SETTING_REQUST = 11;
    private static final int DIALOG_EDIT_CONTACT = 12;
    private static final int QRCODE_INIT_GET_QRCODE_BITMAP = 13;
    private static final int QRCODE_RESULT_BACK_GET_QRCODE_BITMAP = 14;
    private static final int DIALOG_UPLOAD_PROFILE = 15;
    private Context mContext;
    private TextView name, phone_number, introContent;
    private ImageView qrcode_img, photo;
    private ProgressDialog mProgressDialog;
    private RawContact mRawContact;
    private Bitmap mContactPhoto;
    private String mCurrContactName = "";
    private int mGgetBitmapAction = QRCODE_INIT_GET_QRCODE_BITMAP;
    private ProfileListener mQrcodeListener;
    private Profile myProfile;
    boolean isHasBusiness = false;
    private final Handler mhandler = new Handler();

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        processIntent(getIntent());
        setContentView(R.layout.show_my_qrcode);
        // We want the UP affordance but no app icon.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME);
        }
        photo = (ImageView) findViewById(R.id.photo);
        name = (TextView) findViewById(R.id.name);
        phone_number = (TextView) findViewById(R.id.phone_number);
        qrcode_img = (ImageView) findViewById(R.id.qrcode_img);
        introContent = (TextView) findViewById(R.id.intro);

        name.setText(mCurrContactName);
        long contactId = mRawContact.getContactId();
        mContactPhoto = loadContactPhoto(contactId, null);
        if (mContactPhoto != null) {
            photo.setImageBitmap(mContactPhoto);
        } else {
            photo.setImageDrawable(ContactPhotoManager.getDefaultAvatarDrawableForContact(
                    this, null, false, null, null));
        }

        mQrcodeListener = new ProfileListener() {

            @Override
            public void onQRImgDecode(QRCardInfo imgObj, int resultCode,
                    String arg2) throws RemoteException {

            }

            public void onQRImgGet(QRCardImg imgObj, int resultCode, String arg2) {
                RcsLog.d("resultCode= " + resultCode);
                dismissProgressDialog();
                if (resultCode == 0) {
                    if (imgObj != null
                            && !TextUtils.isEmpty(imgObj.getImgBase64Str())) {
                        byte[] imageByte = Base64.decode(
                                imgObj.getImgBase64Str(), Base64.DEFAULT);
                        final Bitmap qrcodeBitmap = BitmapFactory
                                .decodeByteArray(imageByte, 0, imageByte.length);
                        if (qrcodeBitmap != null) {
                            RcsUtils.saveQrCode(MyQrcodeActivity.this,
                                    imgObj.getImgBase64Str(), imgObj.getEtag());
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    qrcode_img.setImageBitmap(qrcodeBitmap);
                                    RcsLog.d("set qrcode successfull ");
                                }
                            });
                            SharedPreferences prefs;
                            prefs = mContext.getSharedPreferences("QrCodeUpdatePreference",
                                    Context.MODE_PRIVATE);
                            String latestTerminal = prefs.getString(RcsUtils.PREF_MY_TEMINAL, "");
                            String myPhoneNumber = RcsUtils.getMyPhoneNumber();
                            if (!TextUtils.isEmpty(myPhoneNumber)
                                    && !TextUtils.equals(myPhoneNumber, latestTerminal)) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(RcsUtils.PREF_MY_TEMINAL, myPhoneNumber);
                                editor.apply();
                            }
                        }
                    }
                } else {

                    mhandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (mGgetBitmapAction == QRCODE_INIT_GET_QRCODE_BITMAP) {
                                Toast.makeText( mContext,
                                        getString(R.string
                                        .rcs_create_qrcode_fail_upload_profile_first),
                                        Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(mContext, getString(R.string.refresh_qrcode_fail),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onProfileUpdated(int resultCode, String resultDesc) throws RemoteException {
            }

            @Override
            public void onAvatarUpdated(int resultCode, String resultDesc) throws RemoteException {
            }

            @Override
            public void onAvatarGet(Avatar avatar, int resultCode, String resultDesc)
                    throws RemoteException {
            }

            @Override
            public void onProfileGet(Profile profile, int resultCode, String resultDesc)
                    throws RemoteException {
            }
        };
        if (mRawContact != null) {
            String rawContactId = String.valueOf(mRawContact.getId());
            RcsLog.i("rawContactId:" + rawContactId);
            String imgString = RcsUtils.GetQrCode(mContext, rawContactId);
            myProfile = RcsUtils.createLocalProfile(mRawContact);
            updateDisplayNumber(myProfile);

            SharedPreferences prefs;
            prefs = mContext.getSharedPreferences("QrCodeUpdatePreference", Context.MODE_PRIVATE);
            String latestTerminal = prefs.getString(RcsUtils.PREF_MY_TEMINAL, "");
            String myPhoneNumber = RcsUtils.getMyPhoneNumber();
            if (!decodeStringAndSetBitmap(imgString)
                    || (!TextUtils.isEmpty(myPhoneNumber) && !TextUtils.equals(myPhoneNumber,
                            latestTerminal))) {
                if ((null != myProfile) && !TextUtils.isEmpty(myProfile.getFirstName())) {
                    createProgressDialog();
                    getQRcodeFromService(myProfile);
                    mGgetBitmapAction = QRCODE_INIT_GET_QRCODE_BITMAP;
                } else {
                    showDialog(DIALOG_EDIT_CONTACT);
                }
            }
        }
    }

    private void updateDisplayNumber(Profile profile) {
        if (null == profile) {
            return;
        }
        StringBuilder currContactPhone = new StringBuilder();
        String myAccountNumber = "";
        myAccountNumber = RcsUtils.getMyPhoneNumber();
        currContactPhone.append(myAccountNumber);
        SharedPreferences myQrcodeSharedPreferences = getSharedPreferences(
                "QrcodePersonalCheckState", Activity.MODE_PRIVATE);
        String value = myQrcodeSharedPreferences.getString("value", "");
        String[] initChecked = value.split(",");
        int total = myQrcodeSharedPreferences.getInt("total", 0);
        for (int i = 0; i < total; i++) {
            if (initChecked[i].equals(getString(R.string.rcs_company_number))) {
                if (!TextUtils.isEmpty(profile.getCompanyTel())) {
                    currContactPhone.append(",").append(profile.getCompanyTel());
                }
            } else if (initChecked[i]
                    .equals(getString(R.string.rcs_company_fax))) {
                if (!TextUtils.isEmpty(profile.getCompanyFax())) {
                    currContactPhone.append(",").append(profile.getCompanyFax());
                }
            }
        }
        phone_number.setText(currContactPhone);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = new Dialog(this);
        if (id == DIALOG_EDIT_CONTACT) {
            dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.qrcode_setting_private_message)
                    .setPositiveButton(R.string.btn_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    startActivity(new Intent(
                                            Intent.ACTION_EDIT,
                                            Uri.withAppendedPath(
                                                    RawContacts.CONTENT_URI,
                                                    String.valueOf(mRawContact
                                                    .getId()))));
                                    finish();
                            }
                    })
            .setNegativeButton(R.string.btn_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            finish();
                        }
                    }).create();
        } else if (id == DIALOG_UPLOAD_PROFILE){
            dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.qrcode_upload_profile)
                    .setPositiveButton(R.string.btn_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                }
                            })
                    .setNegativeButton(R.string.btn_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    finish();
                                }
                    }).create();
        }
        return dialog;
    }

    private boolean decodeStringAndSetBitmap(String imgString) {
        if (TextUtils.isEmpty(imgString)) {
            return false;
        }
        byte[] imageByte = Base64.decode(imgString, Base64.DEFAULT);
        final Bitmap qrcodeBitmap = BitmapFactory.decodeByteArray(imageByte, 0,
                imageByte.length);
        if (null != qrcode_img) {
            qrcode_img.setImageBitmap(qrcodeBitmap);
            RcsLog.d("set qrcode successfull ");
        }
        return true;
    }

    public void getQRcodeFromService(Profile profile) {
        RcsLog.d("getQRcodeFromService");
        try {
            ProfileApi.getInstance().refreshMyQRImg(profile, isHasBusiness, mQrcodeListener);
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void processIntent(Intent intent) {

        String contactPhone = null;
        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        mRawContact = (RawContact) bundle.getParcelable("raw_contact");
        for (DataItem dataItem : mRawContact.getDataItems()) {
            final ContentValues entryValues = dataItem.getContentValues();
            final String mimeType = dataItem.getMimeType();
            if (mimeType == null)
                continue;
        }
        mCurrContactName = bundle.getString("contact_name");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add(0, 1, 0, R.string.qrcode_setting).setIcon(
                R.drawable.qrcode_setting);
        item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;

            case 1:
                // setting QR code datasource
                Intent intent = new Intent(this,QrcodeInfoSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("raw_contact", mRawContact);
                bundle.putString("contact_name", mCurrContactName);
                intent.putExtras(bundle);
                startActivityForResult(intent,QRCODE_SETTING_REQUST);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        // requestCode >= 0 means the activity in question is a sub-activity.
        if (requestCode >= 0) {
            // mWaitingForSubActivity = true;
        }

        super.startActivityForResult(intent, requestCode);
    }

    private Bitmap loadContactPhoto(long contactId,
            BitmapFactory.Options options) {
        Cursor cursor = null;
        Bitmap bm = null;

        try {
            Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                    contactId);
            Uri photoUri = Uri.withAppendedPath(contactUri,
                    Contacts.Photo.CONTENT_DIRECTORY);
            cursor = getContentResolver().query(photoUri,
                    new String[] { Photo.PHOTO }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {

                byte[] data = cursor.getBlob(0);
                bm = BitmapFactory.decodeByteArray(data, 0, data.length,
                        options);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bm;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RcsLog.d("resultCode= " + resultCode);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == QRCODE_SETTING_REQUST && null != data) {
            Bundle bundle = data.getExtras();
            Profile profile = (Profile) bundle.getParcelable("Profile");
            isHasBusiness = data.getBooleanExtra("isHasBusiness", false);
            myProfile = profile;
            refreshInterface(profile, isHasBusiness);
        }
    }

    private void refreshInterface(Profile profile, boolean isHasBusiness) {
        TextView name = (TextView) findViewById(R.id.name);
        TextView phone_number = (TextView) findViewById(R.id.phone_number);
        ImageView qrcode_img = (ImageView) findViewById(R.id.qrcode_img);
        TextView introContent = (TextView) findViewById(R.id.intro);
        createProgressDialog();
        downloadProfile(profile);
        // getQRcodeFromService(profile,isHasBusiness);
        mGgetBitmapAction = QRCODE_RESULT_BACK_GET_QRCODE_BITMAP;
        name.setText(mCurrContactName);
        updateDisplayNumber(myProfile);
    }

    /*
     * actually we do not need to download the profile first ,because the
     * network issuess, i add download profile first to make sure we can upload
     * the profile first,why we need to upload the profile first ,because
     * service need to update profile first ,then it can create the qrcode what
     * we modify from qrcode setting
     */
    private void downloadProfile(Profile prfile) {
        try {
            ProfileApi.getInstance().getMyProfile(new ProfileListener() {
                @Override
                public void onAvatarGet(Avatar arg0, int resultCode,
                        String resultDesc) throws RemoteException {
                }

                @Override
                public void onAvatarUpdated(int arg0, String arg1)
                        throws RemoteException {
                }

                @Override
                public void onProfileGet(final Profile profile,
                        final int resultCode, final String resultDesc)
                        throws RemoteException {
                    RcsLog.d("getMyProfile resultCode= " + resultCode);
                    if (resultCode == 0) {
                        SharedPreferences myProfileSharedPreferences = getSharedPreferences(
                                "RcsSharepreferences", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myProfileSharedPreferences
                                .edit();
                        editor.putString("ProfileTextEtag", profile.getEtag());
                        editor.commit();
                        uploadProfile(myProfile);
                    } else {
                        dismissProgressDialog();
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, getString(R.string.refresh_qrcode_fail),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onProfileUpdated(int arg0, String arg1)
                        throws RemoteException {
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode,
                        String arg2) throws RemoteException {

                }

                @Override
                public void onQRImgGet(QRCardImg qrImgObj, int resultCode, String resultDesc) {
                }

            });
        } catch (ServiceDisconnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void uploadProfile(Profile profile) {
        // final Handler handler = new Handler();
        SharedPreferences myProfileSharedPreferences = getSharedPreferences(
                "RcsSharepreferences", Activity.MODE_WORLD_READABLE);
        String etag = myProfileSharedPreferences.getString("ProfileTextEtag",
                null);
        profile.setEtag(etag);
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
                    RcsLog.d("setMyProfile resultCode= " + resultCode);
                    if (resultCode == 0) {
                        getQRcodeFromService(myProfile);
                    } else {
                        dismissProgressDialog();
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, getString(R.string.refresh_qrcode_fail),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2)
                        throws RemoteException {
                }

                @Override
                public void onQRImgGet(QRCardImg qrImgObj, int resultCode, String resultDesc) {
                }
            });
        } catch (ServiceDisconnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(mContext.getResources().getString(
                R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
