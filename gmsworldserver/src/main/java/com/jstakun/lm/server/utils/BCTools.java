/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;

public class BCTools {

    private static byte[] cipherData(BufferedBlockCipher cipher, byte[] data)
            throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    public static byte[] decrypt(byte[] cipher) throws Exception {

        PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA1Digest());
        pGen.init(PBEParametersGenerator.PKCS12PasswordToBytes(Commons.getProperty(Property.bc_password).toCharArray()), Hex.decode(Commons.getProperty(Property.bc_salt)), 128);
        ParametersWithIV paramsWithIV = (ParametersWithIV) pGen.generateDerivedParameters(192, 64);

        DESedeEngine des = new DESedeEngine();
        CBCBlockCipher des_CBC = new CBCBlockCipher(des);

        PaddedBufferedBlockCipher cipherAES = new PaddedBufferedBlockCipher(des_CBC);

        cipherAES.init(false, paramsWithIV);
        return cipherData(cipherAES, cipher);
    }

    public static byte[] encrypt(byte[] plain) throws Exception {

        PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA1Digest());
        pGen.init(PBEParametersGenerator.PKCS12PasswordToBytes(Commons.getProperty(Property.bc_password).toCharArray()), Hex.decode(Commons.getProperty(Property.bc_salt)), 128);
        ParametersWithIV paramsWithIV = (ParametersWithIV) pGen.generateDerivedParameters(192, 64);

        DESedeEngine des = new DESedeEngine();
        CBCBlockCipher des_CBC = new CBCBlockCipher(des);

        PaddedBufferedBlockCipher cipherAES = new PaddedBufferedBlockCipher(des_CBC);

        cipherAES.init(true, paramsWithIV);
        return cipherData(cipherAES, plain);
    }
}
