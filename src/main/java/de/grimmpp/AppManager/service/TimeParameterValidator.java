package de.grimmpp.AppManager.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TimeParameterValidator {

    public static final String KEY = "time";
    public static final String DEFAULT_VALUE = "8h";

    public static final boolean doesNotContainOrValidTimeParameter(Map<String,Object> map) {
        return !containsTimeParameter(map) || validateParameterValue(map);
    }

    public static final boolean containsTimeParameter(Map<String,Object> parameters) {
        return parameters.containsKey(KEY);
    }

    public static final boolean validateParameterValue(Map<String,Object> parameters) {
        if (!containsTimeParameter(parameters)) return  false;
        return validateParameterValue(parameters.get(KEY).toString());
    }

    public static final String getParameterTime(Map<String,Object> parameters, String defaultTime) {
        if (!containsTimeParameter(parameters)) return defaultTime;
        if (!validateParameterValue(parameters)) throw new RuntimeException();
        return formattingAndCleaning(parameters.get(KEY).toString());
    }

    public static final String getParameterTime(CreateServiceInstanceRequest request, String defaultTime) {
        return getParameterTime(request.getParameters(), defaultTime);
    }

    public static final String getParameterTime(CreateServiceInstanceBindingRequest request, String defaultTime) {
        return getParameterTime(request.getParameters(), defaultTime);
    }

    public static final long getTimeInMilliSecFromParameterValue(Map<String,Object> parameters, String defaultTime) {
        String time = defaultTime;
        if (containsTimeParameter(parameters)) time = parameters.get(KEY).toString();
        return getTimeInMilliSecFromParameterValue(time);
    }

    public static final boolean validateParameterValue(String parameterValue) {
        long time;

        try {
            time = getTimeInMilliSecFromParameterValue(parameterValue);
        } catch (Throwable e) {
            log.error("Error in validating time parameter.", e);
            return  false;
        }

        return time > 0;
    }

    /**
     *
     * @param parameterValue is e.g. "1w 3d 5h 2m 23s"
     * @return
     */
    public static final long getTimeInMilliSecFromParameterValue(String parameterValue) {
        long time = 0;

        List<String> timeSections = Arrays.asList(formattingAndCleaning(parameterValue).split(" "));
        for (String s : timeSections) time += getTimeSection(s);

        return time;
    }

    private static String formattingAndCleaning(String parameterValue) {
        String value = parameterValue;
        value = value.toLowerCase();
        value = value.replaceAll(",", "");
        value = value.replaceAll(";", "");
        value = value.replaceAll("_", "");
        value = value.replaceAll("-", "");
        value = value.replaceAll("w", "w ");
        value = value.replaceAll("d", "d ");
        value = value.replaceAll("h", "h ");
        value = value.replaceAll("m", "m ");
        value = value.replaceAll("s", "s ");
        while(value.contains("  ")) value = value.replaceAll("  ", " ");

        return value.trim();
    }

    private static long getTimeSection(String timeSection) {
        long time = 0;
        Character c = timeSection.charAt(timeSection.length()-1);
        long l = Integer.valueOf(timeSection.substring(0, timeSection.length()-1));

        switch (c) {
            case 's':
                time += inMilliFromSecs(l);
                break;
            case 'm':
                time += inMilliFromMins(l);
                break;
            case 'h':
                time += inMilliFromHours(l);
                break;
            case 'd':
                time += inMilliFromDays(l);
                break;
            case 'w':
                time += inMilliFromWeeks(l);
                break;
            default:
                throw new RuntimeException("Not implemented: "+c);
        }
        return time;
    }

    private static long inMilliFromSecs(long sec) {
        return 1000*sec;
    }

    private static long inMilliFromMins(long min) {
        return inMilliFromSecs(min) *60;
    }

    private static long inMilliFromHours(long h) {
        return inMilliFromMins(h)*60;
    }

    private static long inMilliFromDays(long d) {
        return inMilliFromHours(d)*24;
    }

    private static long inMilliFromWeeks(long w) {
        return inMilliFromDays(w)*7;
    }
}
