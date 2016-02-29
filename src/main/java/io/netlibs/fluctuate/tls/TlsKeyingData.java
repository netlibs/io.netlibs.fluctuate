package io.netlibs.fluctuate.tls;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TlsKeyingData
{

  private final String serverName;
  private final PrivateKey privateKey;
  private final X509Certificate[] publicKey;

  public static TlsKeyingData loadFrom(File keyCertChainFile, File keyFile)
  {
    return loadFrom(keyCertChainFile, keyFile, null);
  }

  public static TlsKeyingData loadFrom(File keyCertChainFile, File keyFile, String keyPassword)
  {

    X509Certificate[] keyCertChain;
    PrivateKey key;
    try
    {
      keyCertChain = toX509Certificates(keyCertChainFile);
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("File does not contain valid certificates: " + keyCertChainFile, e);
    }
    try
    {
      key = toPrivateKey(keyFile, keyPassword);
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("File does not contain valid private key: " + keyFile, e);
    }

    return new TlsKeyingData(null, key, keyCertChain);

  }

  static X509Certificate[] toX509Certificates(File file) throws CertificateException
  {
    if (file == null)
    {
      return null;
    }
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    ByteBuf[] certs = PemReader.readCertificates(file);
    X509Certificate[] x509Certs = new X509Certificate[certs.length];

    try
    {
      for (int i = 0; i < certs.length; i++)
      {
        x509Certs[i] = (X509Certificate) cf.generateCertificate(new ByteBufInputStream(certs[i]));
      }
    }
    finally
    {
      for (ByteBuf buf : certs)
      {
        buf.release();
      }
    }
    return x509Certs;
  }

  static PrivateKey toPrivateKey(File keyFile, String keyPassword) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeySpecException,
      InvalidAlgorithmParameterException,
      KeyException, IOException
  {
    if (keyFile == null)
    {
      return null;
    }
    ByteBuf encodedKeyBuf = PemReader.readPrivateKey(keyFile);
    byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
    encodedKeyBuf.readBytes(encodedKey).release();

    PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(keyPassword == null ? null : keyPassword.toCharArray(),
        encodedKey);

    PrivateKey key;
    try
    {
      key = KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
    }
    catch (InvalidKeySpecException ignore)
    {
      try
      {
        key = KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
      }
      catch (InvalidKeySpecException ignore2)
      {
        try
        {
          key = KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
        }
        catch (InvalidKeySpecException e)
        {
          throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
        }
      }
    }
    return key;
  }

  /**
   * Generates a key specification for an (encrypted) private key.
   *
   * @param password
   *          characters, if {@code null} or empty an unencrypted key is assumed
   * @param key
   *          bytes of the DER encoded private key
   *
   * @return a key specification
   *
   * @throws IOException
   *           if parsing {@code key} fails
   * @throws NoSuchAlgorithmException
   *           if the algorithm used to encrypt {@code key} is unkown
   * @throws NoSuchPaddingException
   *           if the padding scheme specified in the decryption algorithm is unkown
   * @throws InvalidKeySpecException
   *           if the decryption key based on {@code password} cannot be generated
   * @throws InvalidKeyException
   *           if the decryption key based on {@code password} cannot be used to decrypt {@code key}
   * @throws InvalidAlgorithmParameterException
   *           if decryption algorithm parameters are somehow faulty
   */
  protected static PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key)
      throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
      InvalidKeyException, InvalidAlgorithmParameterException
  {

    if (password == null || password.length == 0)
    {
      return new PKCS8EncodedKeySpec(key);
    }

    EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
    PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
    SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);

    Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
    cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());

    return encryptedPrivateKeyInfo.getKeySpec(cipher);
  }

}
