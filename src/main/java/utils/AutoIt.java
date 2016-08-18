package utils;

import java.io.File;

import com.jacob.com.LibraryLoader;

import autoitx4java.AutoItX;

public class AutoIt {

	private AutoItX engine;
	private static AutoIt instance = null;

	private AutoIt() {
		init();
	}

	private void init() {
		String dllPath = SystemUtils.Files.getResourcesDirectoryPath() + "\\lib\\jacob-1.18-x64.dll"; //TODO : Should be according to os 
		File file = new File(dllPath);
		if (!file.exists())
			System.err.println("file is missing");
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, file.getAbsolutePath());
		engine = new AutoItX();
	}

	public static AutoIt getInstance() {
		if (instance == null) {
			instance = new AutoIt();
		}
		return instance;
	}

	public static AutoItX engine() {
		return getInstance().engine;
	}

}
