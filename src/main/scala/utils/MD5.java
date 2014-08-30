package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Shaiy
 * @since 02/05/11 11:34
 */
public abstract class MD5
{

    public static String fromString(String text)
    {
        try
        {
            return fromBytes(text.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            //Do nothing
        }

        return null;
    }

    public static String fromBytes(byte[] data)
    {
        MessageDigest md;
        byte[] md5hash = new byte[32];

        try
        {
            md = MessageDigest.getInstance("MD5");
            md.update(data, 0, data.length);
            md5hash = md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            //Do nothing
        }
        return convertToHex(md5hash);
    }

    public static String fromStream(InputStream s) throws IOException
    {
        MessageDigest md;
        byte[] md5hash = new byte[32];

        try
        {
            md = MessageDigest.getInstance("MD5");

            byte[] buf = new byte[2048];
            int n = 0;
            while ((n = s.read(buf)) > -1)
            {
                md.update(buf, 0, n);
            }
            md5hash = md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            //Do nothing
        }
        return convertToHex(md5hash);
    }


    private static String convertToHex(byte[] data)
    {
        StringBuffer buf = new StringBuffer();
        for (byte aData : data)
        {
            int halfbyte = (aData >>> 4) & 0x0F;
            int two_halfs = 0;
            do
            {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = aData & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

}
