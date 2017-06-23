package com.alecgorge.minecraft.jsonapi.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecursiveDirLister {
	File aStartingDir;
	
	public RecursiveDirLister (File s) {
		aStartingDir = s;
	}
	
	/**
	* Recursively walk a directory tree and return a List of all
	* Files found; the List is sorted using File.compareTo().
	*
	* @param aStartingDir is a valid directory, which can be read.
	*/
	public List<String> getFileListing() throws FileNotFoundException {
		validateDirectory(aStartingDir);
		
		List<String> result = getFileListingNoSort(aStartingDir, true);
		Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
		
		return result;
	}
	
	public List<String> getSingleFileListing() throws FileNotFoundException {
		validateDirectory(aStartingDir);
		
		List<String> result = getFileListingNoSort(aStartingDir, false);
		Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
		
		return result;
	}
	
	private List<String> getFileListingNoSort(File aStartingDir, boolean recursive) throws FileNotFoundException {
		List<String> result = new ArrayList<String>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for(File file : filesDirs) {
			result.add(file.toString().replace('\\', '/')+(file.isFile() ? "" : "/")); //always add, even if directory
			if (recursive && !file.isFile()) {
				//must be a directory
				//recursive call!
				List<String> deeperList = getFileListingNoSort(file, recursive);
				result.addAll(deeperList);
			}
		}
		return result;
	}
	
	/**
	* Directory is valid if it exists, does not represent a file, and can be read.
	*/
	private void validateDirectory (File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}
}
