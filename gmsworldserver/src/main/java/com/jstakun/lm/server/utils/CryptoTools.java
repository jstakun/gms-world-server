/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils;

import com.jstakun.lm.server.config.Commons;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


/**
 *
 * @author jstakun
 */
public class CryptoTools {

    public static byte[] decrypt(byte[] enc) throws Exception {
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(Hex.decode(Commons.bc_salt), 128);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(Commons.bc_password.toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Commons.bc_algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(Commons.bc_algorithm);
        cipher.init(Cipher.DECRYPT_MODE,secretKey,pbeParamSpec);

        return cipher.doFinal(enc);

    }

    public static byte[] encrypt(byte[] plain) throws Exception {

        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(Hex.decode(Commons.bc_salt), 128);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(Commons.bc_password.toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Commons.bc_algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(Commons.bc_algorithm);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,pbeParamSpec);

        return cipher.doFinal(plain);

    }
}
