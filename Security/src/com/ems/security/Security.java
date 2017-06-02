package com.ems.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public final class Security
{
  public static Logger log = Logger.getLogger(Security.class.getName());

  private static final byte[] secureBytes = { 10, 22, 1, 30, 40, 
    7, 23, 37, 20, 40, 25, 0, 42, 29, 1, 28, 23, 28, 26, 32, 15, 0, 42 };

  private static Properties prop = new Properties();
  private DESKeySpec dks = null;
  private SecretKeyFactory skf = null;
  private SecretKey desKey = null;
  private Cipher cipher = null;
  private CipherInputStream cipherSource = null;

  static {
    try {
      InputStream inputStream = Security.class
        .getResourceAsStream("secure.properties");
      prop.load(inputStream);

      if (inputStream != null)
        inputStream.close();
    } catch (Exception e) {
      log.log(Level.SEVERE, 
        "Error : resouces could not be loaded " + 
        e.getLocalizedMessage());
    }
  }

  public static Security getInstance()
  {
    Security security = new Security();
    security.init();
    return security;
  }

  public static byte[] getSecurebytes() {
    byte[] systemSpecific = getEncryptionByte();
    return systemSpecific == null ? secureBytes : systemSpecific;
  }

  public static byte[] getEncryptionByte()
  {
    byte[] bytes = null;
    try {
      Process p = Runtime.getRuntime().exec("getmac /fo csv /nh");
      BufferedReader in = new BufferedReader(
        new InputStreamReader(p.getInputStream()));

      String line = in.readLine();
      String[] result = line.split(",");
      bytes = result[0].replace('"', ' ').trim().getBytes();
    } catch (Exception e) {
      log.log(Level.SEVERE, 
        "Secure Bytes : could not initialize System specific secure bytes" + 
        e.getLocalizedMessage());
    }
    return bytes;
  }

  private void init() {
    try {
      this.dks = new DESKeySpec(getSecurebytes());
      this.skf = SecretKeyFactory.getInstance(prop
        .getProperty("fileAlgorithm"));
      this.cipher = Cipher.getInstance(prop.getProperty("fileAlgorithm"));
      this.desKey = this.skf.generateSecret(this.dks);
      this.cipher.init(1, this.desKey);
      log.info(" initialized successfully " + (!prop.isEmpty()));
    } catch (Exception e) {
      log.log(Level.SEVERE, 
        "Cipher : could not be initialized " + 
        e.getLocalizedMessage());
    }
  }

  private Cipher getInstance(int cipherMode) {
    Cipher cipher = null;
    try
    {
      cipher = Cipher.getInstance(prop.getProperty("fileAlgorithm"));
      cipher.init(cipherMode, this.desKey);
    } catch (Exception e) {
      log.log(Level.SEVERE, 
        "Unable to return valid cipher " + e.getLocalizedMessage());
    }

    return cipher;
  }

  public long encrypt(InputStream source, OutputStream destination, int numberOfKb) throws InvalidKeyException, IOException
  {
    Cipher cipher = getInstance(1);
    if (cipher == null)
      throw new NullPointerException("Cipher cannot be NULL");
    this.cipherSource = new CipherInputStream(source, cipher);
    long totalBytesRead = doCopy(this.cipherSource, destination, numberOfKb);
    return totalBytesRead;
  }

  public long decrypt(InputStream source, OutputStream destination, int numberOfKb) throws InvalidKeyException, IOException
  {
    Cipher cipher = getInstance(2);
    if (cipher == null)
      throw new NullPointerException("Cipher cannot be NULL");
    this.cipherSource = new CipherInputStream(source, cipher);
    long totalBytesRead = doCopy(this.cipherSource, destination, numberOfKb);
    return totalBytesRead;
  }

  public InputStream getEncryptedStream(InputStream input) throws InvalidKeyException
  {
    Cipher cipher = getInstance(1);
    this.cipherSource = new CipherInputStream(input, cipher);
    return this.cipherSource;
  }

  public InputStream getDecryptedStream(InputStream input) throws InvalidKeyException
  {
    Cipher cipher = getInstance(2);
    this.cipherSource = new CipherInputStream(input, cipher);
    return this.cipherSource;
  }

  public String encrypt(String toBeEncrypted) throws Exception {
    Cipher cipher = getInstance(1);
    byte[] utf16 = toBeEncrypted.getBytes(prop.getProperty("encoding"));
    byte[] encrypted = cipher.doFinal(utf16);
    String value = new String(Base64.getEncoder().encode(encrypted));
    return value;
  }

  public String decrypt(String toBeDecrypted) throws Exception {
    Cipher cipher = getInstance(2);
    byte[] dec = Base64.getDecoder().decode(toBeDecrypted.getBytes());
    byte[] utf16 = cipher.doFinal(dec);
    String value = new String(utf16, prop.getProperty("encoding"));
    return value;
  }

  public long doCopy(InputStream source, OutputStream destination, int numberOfKb) throws IOException
  {
    byte[] bytes = new byte[1024 * numberOfKb];
    long totalBytesRead = 0L;

    if ((source != null) && (destination != null) && (numberOfKb != 0)) {
      try
      {
        int numBytes;
        while ((numBytes = source.read(bytes)) != -1)
        {
          destination.write(bytes, 0, numBytes);
          totalBytesRead += numBytes;
        }
        log.info("file copied successfully");
      } catch (Exception e) {
        log.log(Level.SEVERE, 
          "error in closing stream(s) " + e.getLocalizedMessage());
      } finally {
        source.close();
        destination.close();
      }
    }

    return totalBytesRead;
  }

  protected void finalize() throws Throwable
  {
    this.dks = null;
    this.skf = null;
    this.desKey = null;
    this.cipher = null;
    this.cipherSource = null;
    super.finalize();
  }

  public static void main(String[] args) {
    System.out.println("Secure me...");
  }
}