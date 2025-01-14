/*******************************************************************************
 * Copyright (c) 2015-2022 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.SilentErrorHandler;

public class DitaUtils {

	private static Map<String, String> filesTable;
	private static List<String> filesMap;

	private DitaUtils() {
		// do not instantiate this class
	}

	public static SortedSet<String> getFiles(String map) {
		filesMap = new Vector<>();
		filesTable = new Hashtable<>();
		File f = new File(map);
		parseMap(map, f.getParent());
		TreeSet<String> filesTree = new TreeSet<>();
		filesTree.addAll(filesMap);
		return filesTree;
	}

	private static void parseMap(String map, String home) {
		if (map.startsWith("#")) {
			// self file
			return;
		}
		try {
			String path = map;
			File f = new File(map);
			if (!f.isAbsolute()) {
				if (map.indexOf("#") != -1) {
					path = FileUtils.getAbsolutePath(home, map.substring(0, map.indexOf("#")));
				} else {
					path = FileUtils.getAbsolutePath(home, map);
				}
				f = new File(path);
			}
			if (!f.exists()) {
				return;
			}
			if (filesTable.containsKey(path)) {
				return;
			}
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalog(Preferences.getInstance().getCatalogFile()));
			builder.setErrorHandler(new SilentErrorHandler());
			Document doc = builder.build(path);
			Element mapRoot = doc.getRootElement();
			if (!filesTable.containsKey(path)) {
				filesTable.put(path, "");
				filesMap.add(path);
			} else {
				return;
			}
			recurse(mapRoot, path);
		} catch (IOException | SAXException | ParserConfigurationException | URISyntaxException e) {
			// do nothing
		}
	}

	private static void recurse(Element root, String parent)
			throws SAXException, IOException, ParserConfigurationException {
		String href = root.getAttributeValue("href");
		if (!href.isEmpty()) {
			parseMap(href, parent);
		}
		String conref = root.getAttributeValue("conref");
		if (!conref.isEmpty()) {
			if (conref.indexOf("#") != -1) {
				conref = conref.substring(0, conref.indexOf("#"));
			}
			parseMap(conref, parent);
		}
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next(), parent);
		}
	}

}
