package com.epower.rpc.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 包扫描
 * 
 * @author xie
 * @source
 * <p>
 * http://www.jb51.net/article/36320.htm
 *
 */
public class PackageScanner {

	public static List<String> getClassName(String packageName) {
		List<String> classNames = new ArrayList<String>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			String resourceName = packageName.replaceAll("\\.", "/");
			URL url = loader.getResource(resourceName);
			File urlFile = new File(url.toURI());
			File[] files = urlFile.listFiles();
			for (File f : files) {
				getClassName(packageName, f, classNames);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return classNames;
	}

	/**
	 * 获取类名称
	 * @param packageName
	 * @param packageFile
	 * @param list
	 */
	private static void getClassName(String packageName, File packageFile, List<String> list) {

		if (packageFile.isFile() && packageFile.getName().endsWith(".class")) {
			if (packageName.indexOf(".") == 0) {
				packageName = new StringBuilder(packageName).deleteCharAt(0).toString();
			}
			list.add(packageName + "." + packageFile.getName().replace(".class", ""));
		} else {
			File[] files = packageFile.listFiles();
			String tmPackageName = packageName + "." + packageFile.getName();
			if (files != null) {
				for (File f : files) {
					getClassName(tmPackageName, f, list);
				}
			}
		}
	}
}
