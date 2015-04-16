package net.gmsworld.server.utils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;

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
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(Hex.decode(Commons.getProperty(Property.bc_salt)), 128);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(Commons.getProperty(Property.bc_password).toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Commons.getProperty(Property.bc_algorithm));
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(Commons.getProperty(Property.bc_algorithm));
        cipher.init(Cipher.DECRYPT_MODE,secretKey,pbeParamSpec);

        return cipher.doFinal(enc);

    }

    public static byte[] encrypt(byte[] plain) throws Exception {

        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(Hex.decode(Commons.getProperty(Property.bc_salt)), 128);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(Commons.getProperty(Property.bc_password).toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Commons.getProperty(Property.bc_algorithm));
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(Commons.getProperty(Property.bc_algorithm));
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,pbeParamSpec);

        return cipher.doFinal(plain);

    }
}
