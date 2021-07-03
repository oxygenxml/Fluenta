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

package com.maxprograms.tmengine;

import java.io.Serializable;
import java.util.Hashtable;

public class Tuv implements Serializable {

	private static final long serialVersionUID = -7241116367216426814L;
	
	private transient String tuid;
	private transient String lang;
	private String segment;
	private String pureText;
	private Hashtable<String,String> props;
	
	public Tuv(String tuid, String lang, String segment, String pureText) {
		this.tuid = tuid;
		this.lang = lang;
		this.segment = segment;
		this.pureText = pureText;
		props = new Hashtable<>();
	}

	public String getTuid() {
		return tuid;
	}

	public String getLang() {
		return lang;
	}
	
	public String getSegment() {
		return segment;
	}
	
	public String getPureText() {
		return pureText;
	}

	public void setKey(String tuid, String lang) {
		this.tuid = tuid;
		this.lang = lang;
	}
	
	public void setProperties(Hashtable<String, String> values) {
		props = values;
	}
	
	public Hashtable<String, String> getProperties() {
		return props;
	}
	
	public String getProperty(String name) {
		return props.get(name);
	}
}
