/*******************************************************************************
 * Copyright (c) 2015-2021 Maxprograms.
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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.maxprograms.xml.Element;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;

public class MemUtils {
	public static Vector<Element> sortMatches(List<Element> matches) {

    	Vector<Element> result = new Vector<Element>();
    	if (matches.size() == 0) {
    		return result;
    	}
    	if (matches.size() == 1) {
    		result.add(matches.get(0));
    		return result;
    	}
    	
    	TreeSet<Match> set = new TreeSet<Match>();
        Iterator<Element> i = matches.iterator();
        while (i.hasNext()) {
            Element el = i.next();
            Float value;
            String creationdate = "";  //$NON-NLS-1$
            List<Element> groups = el.getChildren("prop-group");  //$NON-NLS-1$
            Iterator<Element> gt = groups.iterator();
            while (gt.hasNext()) {
            	Element group = gt.next();
            	List<Element> props = group.getChildren("prop");  //$NON-NLS-1$
            	Iterator<Element> pt = props.iterator();
            	while (pt.hasNext()) {
            		Element prop = pt.next();
            		if (prop.getAttributeValue("prop-type").equals("creationdate")) {  //$NON-NLS-1$ //$NON-NLS-2$
            			creationdate = prop.getText();
            		}
            	}
            }
            try {
            	value = Float.valueOf(el.getAttributeValue("match-quality", "0.0"));  //$NON-NLS-1$ //$NON-NLS-2$
            } catch (java.lang.NumberFormatException nfe) {
            	value = Float.valueOf("0.0");  //$NON-NLS-1$
            }
            set.add(new Match(value, el, creationdate, el.getAttributeValue("origin","")));  //$NON-NLS-1$ //$NON-NLS-2$
        }
        Iterator<Match> it = set.iterator();
    	while (it.hasNext()) {
    		result.add(it.next().getMatch());
    	}
    	it = null;
    	set = null;
    	return result;
    }
	
	public static String createId() {
		Date now = new Date();
		long lng = now.getTime();
		// wait until we are in the next millisecond
		// before leaving to ensure uniqueness
		Date next = new Date();
		while (next.getTime() == lng) {
			next = null;
			next = new Date();
		}
		return "" + lng; //$NON-NLS-1$
	}
	
	public static String pureText(Element seg) {
		List<XMLNode> l = seg.getContent();
		Iterator<XMLNode> i = l.iterator();
		String text = ""; //$NON-NLS-1$
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.TEXT_NODE) {
				text = text + ((TextNode)o).getText();
			} else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				String type = ((Element)o).getName();
				// discard all inline elements
				// except <mrk>, <g> and <hi> 
				if (type.equals("mrk") || type.equals("hi") || type.equals("g")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Element e = (Element)o;
					text = text + recurse(e);
				}
			}
		}
		i = null;
		l = null;
		return text;
	}
	
	private static String recurse(Element seg) {
		// same as pureTex but without trimming returned text
		List<XMLNode> l = seg.getContent();
		Iterator<XMLNode> i = l.iterator();
		String text = ""; //$NON-NLS-1$
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.TEXT_NODE) {
				text = text + ((TextNode)o).getText();
			} else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				String type = ((Element)o).getName();
				// discard all inline elements
				// except <mrk>, <g> and <hi> 
				if (type.equals("mrk") || type.equals("hi") || type.equals("g")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Element e = (Element)o;
					text = text + recurse(e);
				}
			}
		}
		i = null;
		l = null;
		return text;
	}
}