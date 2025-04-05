package org.adsc.adsc_bot.utilties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class FileHandle
{
	private static final Properties config = new Properties();

	static
	{
		try (FileInputStream fis = new FileInputStream("config.properties"))
		{
			config.load(fis);
		}
		catch (IOException e)
		{
			config.setProperty("token", "");
			config.setProperty("db.url", "");
			config.setProperty("db.user", "");
			config.setProperty("db.password", "");
		}
	}

	public static String readConfig(String key)
	{
		return config.getProperty(key);
	}
}