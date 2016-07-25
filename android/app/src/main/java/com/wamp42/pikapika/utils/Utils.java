package com.wamp42.pikapika.utils;

import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
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
}
