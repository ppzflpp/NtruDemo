package com.dragon.ntrudemo;

import android.text.TextUtils;
import android.util.Base64;

import net.sf.ntru.encrypt.EncryptionKeyPair;
import net.sf.ntru.encrypt.EncryptionParameters;
import net.sf.ntru.encrypt.NtruEncrypt;
import net.sf.ntru.sign.NtruSign;
import net.sf.ntru.sign.SignatureParameters;

/**
 * Created by Administrator on 2016/4/26 0026.
 */
public class NtruManager {

    private static final SignatureParameters SIGNATURE_PARAMS = SignatureParameters.APR2011_439_PROD;
    private static final EncryptionParameters ENCRYPTION_PARAMS = EncryptionParameters.APR2011_439_FAST;

    private byte[] mEncryptedBytes;
    private NtruEncrypt mNtruEncrypt;
    private EncryptionKeyPair mEncryptionKeyPair;

    public NtruManager(){
        mNtruEncrypt = new NtruEncrypt(ENCRYPTION_PARAMS);
        mEncryptionKeyPair = mNtruEncrypt.generateKeyPair();
    }

    public String encrypt(String content){
        if(TextUtils.isEmpty(content)){
            return null;
        }

        String result = null;
        mEncryptedBytes = mNtruEncrypt.encrypt(content.getBytes(), mEncryptionKeyPair.getPublic());
        result = Base64.encodeToString(mEncryptedBytes,Base64.DEFAULT);
        return result;
    }

    public String decrypt(){
        String result = null;
        if(mEncryptedBytes != null){
            byte[] bytes = mNtruEncrypt.decrypt(mEncryptedBytes,mEncryptionKeyPair);
            result = new String(bytes);
        }
        return result;
    }

}
