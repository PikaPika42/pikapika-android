package com.pikapika.lite.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.webkit.CookieManager;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Flavio on 23/07/2016.
 */
public class Utils {
    private static String cryptoPass = "pEr1P33*pEr1P33*";



    public static String encryptIt(String valueToEnc) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(cryptoPass.toCharArray(), salt, 100, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

            byte[] ivBytes = new byte[16];
            random.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encValue = c.doFinal(valueToEnc.getBytes());

            byte[] finalCipherText = new byte[encValue.length+2*16];
            System.arraycopy(ivBytes, 0, finalCipherText, 0, 16);
            System.arraycopy(salt, 0, finalCipherText, 16, 16);
            System.arraycopy(encValue, 0, finalCipherText, 32, encValue.length);

            //return new String(finalCipherText);
            return Base64.encodeToString(finalCipherText, Base64.DEFAULT);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }  catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return valueToEnc;
    };

    public static String decryptIt(String valueToDec) {
        try {
            byte[] dataSrcByte = Base64.decode(valueToDec, Base64.DEFAULT);
            byte[] salt = new byte[16];
            byte[] ivBytes = new byte[16];
            byte[] encValue = new byte[dataSrcByte.length-2*16];

            System.arraycopy(dataSrcByte, 0, ivBytes, 0, 16);
            System.arraycopy(dataSrcByte, 16, salt, 0, 16);
            System.arraycopy(dataSrcByte, 32, encValue, 0, encValue.length);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(cryptoPass.toCharArray(), salt, 100, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedValueBytes = c.doFinal(encValue);

            return new String(decryptedValueBytes);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return valueToDec;
    }

    public static HashMap<String, String> getMapFromForm(String form){
        HashMap<String, String> map = new HashMap<>();
        String[] parts = form.split("\n");
        for(String part : parts){
            String[] keys = part.split("=");
            if(keys.length > 1){
                map.put(keys[0],keys[1]);
            }
        }
        return map;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static  void cleanCookies(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }else {
            CookieManager.getInstance().removeAllCookie();
        }
    }

    //return the distance between two locations in MILES
    public static double locationDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        return dist;
    }
}
