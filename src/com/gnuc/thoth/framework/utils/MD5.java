package com.gnuc.thoth.framework.utils;
import java.security.MessageDigest;

public final class MD5
{
	public static final String getMD5HEX(final String text)
	{
		try
		{
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(text.getBytes("UTF-8"));
			final byte[] bytes = digest.digest();
			final StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < bytes.length; i++)
			{
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1)
				{
					hex = "0" + hex;
				}
				buffer.append(hex);
			}
			return buffer.toString();
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
