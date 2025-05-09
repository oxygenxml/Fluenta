/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.fluenta.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.maxprograms.utils.DateFormatterUtil;
import com.maxprograms.utils.TextUtils;

public class ProjectEvent {

	public static final String XLIFF_CREATED = "0";
	public static final String XLIFF_IMPORTED = "1";
	public static final String XLIFF_CANCELLED = "2";

	private String language;
	private Date date;
	private String type;
	private int build;

	public ProjectEvent(String type, Date date, String language, int build) {
		this.type = type;
		this.date = date;
		this.language = language;
		this.build = build;
	}

	public ProjectEvent(JSONObject json) throws JSONException, ParseException {
		this.type = json.getString("type");
		this.date =  DateFormatterUtil.parseDate(json.getString("date"));
		this.language = json.getString("language");
		this.build = json.getInt("build");
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("type", type);
		json.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date));
		json.put("language", language);
		json.put("build", build);
		return json;
	}

	public String getLanguage() {
		return language;
	}

	public Date getDate() {
		return date;
	}

	public String getType() {
		return type;
	}

	public int getBuild() {
		return build;
	}

	public String getDateString() {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR) + "-" + TextUtils.pad(c.get(Calendar.MONTH) + 1, 2) + "-"
				+ TextUtils.pad(c.get(Calendar.DAY_OF_MONTH), 2) +
				" " + TextUtils.pad(c.get(Calendar.HOUR_OF_DAY) + 1, 2) + ":"
				+ TextUtils.pad(c.get(Calendar.MINUTE), 2);
	}

	public static String getDescription(String event) {
		switch (event) {
			case XLIFF_CREATED:
				return Messages.getString("ProjectEvent.0");
			case XLIFF_CANCELLED:
				return Messages.getString("ProjectEvent.1");
			case XLIFF_IMPORTED:
				return Messages.getString("ProjectEvent.2");
			default:
				return Messages.getString("ProjectEvent.3");
		}
	}
}
