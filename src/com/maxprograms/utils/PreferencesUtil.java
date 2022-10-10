package com.maxprograms.utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;

/**
 * see: 
 * 
 * @author alex_smarandache
 *
 */
public class PreferencesUtil {

  private void PreferencesUtil() {
   // not instantiate
  }
  
  
  public static boolean getTranslateComments() throws IOException {
    Preferences prefs = Preferences.getInstance();
    return prefs.get("XMLOptions", "TranslateComments", "No").equalsIgnoreCase("Yes");    
  }
  
  
  public static String getDefaultSRX() throws IOException {
    Preferences prefs = Preferences.getInstance();
    File srxFolder = new File(Preferences.getPreferencesDir(), "srx"); 
    File defaultSrx = new File(srxFolder, "default.srx"); 
    return prefs.get("DefaultSRX", "srx", defaultSrx.getAbsolutePath());  
  }

  public static Language getDefaultSource() throws IOException {
    Preferences prefs = Preferences.getInstance();
    return LanguageUtils.getLanguage(prefs.get("DefaultSourceLanguages", "default", "en-US"));   
  }

  public static List<Language> getDefaultTargets() throws IOException {
    TreeSet<Language> tree = new TreeSet<>(new Comparator<Language>() {

      @Override
      public int compare(Language o1, Language o2) {
        return o1.getDescription().compareTo(o2.getDescription());
      }

    });
    Preferences prefs = Preferences.getInstance();
    JSONObject table = prefs.get("DefaultTargetLanguages"); 
    Iterator<String> keys = table.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      tree.add(new Language(key, table.getString(key)));
    }
    if (tree.isEmpty()) {
      tree.add(LanguageUtils.getLanguage("fr")); 
      tree.add(LanguageUtils.getLanguage("de")); 
      tree.add(LanguageUtils.getLanguage("it")); 
      tree.add(LanguageUtils.getLanguage("es")); 
      tree.add(LanguageUtils.getLanguage("ja-JP")); 
    }
    List<Language> result = new Vector<>();
    result.addAll(tree);
    return result;
  }
  
}
