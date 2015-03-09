package com.lihang.pti;

import java.io.File;

public class Launcher {

	public static void main(String[] args) throws Exception {
		String configPath = "config.json";
		if (args.length > 0) {
			configPath = args[0];
		}
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			File jarFile = new File(System.getProperty("java.class.path"));
			configFile = new File(jarFile.getParentFile(), configPath);
		}
		Config config = Config.load(configFile);
		new ThumbTask(config).run();
	}
}
