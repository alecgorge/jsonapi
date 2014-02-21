package com.google.code.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedPattern {

	private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");

	private Pattern pattern;
	private String namedPattern;
	private List<String> groupNames;

    public static NamedPattern compile(String regex) {
        return new NamedPattern(regex, 0);
    }

    public static NamedPattern compile(String regex, int flags) {
        return new NamedPattern(regex, flags);
    }

    private NamedPattern(String regex, int i) {
    	namedPattern = regex;
    	pattern = buildStandardPattern(regex);
    	groupNames = extractGroupNames(regex);
	}

	public int flags() {
		return pattern.flags();
	}

	public NamedMatcher matcher(CharSequence input) {
		return new NamedMatcher(this, input);
	}

	Pattern pattern() {
		return pattern;
	}

	public String standardPattern() {
		return pattern.pattern();
	}

	public String namedPattern() {
		return namedPattern;
	}

	public List<String> groupNames() {
		return groupNames;
	}

	public String[] split(CharSequence input, int limit) {
		return pattern.split(input, limit);
	}

	public String[] split(CharSequence input) {
		return pattern.split(input);
	}

	public String toString() {
		return namedPattern;
	}

	static List<String> extractGroupNames(String namedPattern) {
		List<String> groupNames = new ArrayList<String>();
		Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
		while(matcher.find()) {
			groupNames.add(matcher.group(1));
		}
		return groupNames;
	}

	static Pattern buildStandardPattern(String namedPattern) {
		return Pattern.compile(NAMED_GROUP_PATTERN.matcher(namedPattern).replaceAll("("));
	}

}
