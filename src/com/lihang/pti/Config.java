package com.lihang.pti;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONObject;

public class Config {

	public static Config load(File file) throws IOException {
		StringBuilder source = new StringBuilder();
		for (Scanner in = new Scanner(file); in.hasNextLine();) {
			source.append(in.nextLine());
		}
		JSONObject json = new JSONObject(source.toString());
		return new Config(json);
	}

	private final JSONObject json;

	private Config(JSONObject json) {
		this.json = json;
	}
	
	public String get(String key) {
		String[] subkeys = key.split("\\.");
		JSONObject parent = json;
		for (int i = 0; i < subkeys.length; i++) {
			String k = subkeys[i];
			if (i == subkeys.length - 1) {
				return parent.get(k).toString();
			} else {
				parent = parent.getJSONObject(k);
			}
		}
		throw new RuntimeException(key + " does not exist");
	}

	public boolean getBool(String key) {
		return Boolean.valueOf(get(key));
	}

	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}

}
