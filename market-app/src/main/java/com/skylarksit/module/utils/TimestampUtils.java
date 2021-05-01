package com.skylarksit.module.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimestampUtils {

	/**
	 * Return an ISO 8601 combined date and time string for current date/time
	 * 
	 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 */
	public static String getISO8601StringForCurrentDate() {
		Date now = new Date();
		return getISO8601StringForDate(now);
	}

	/**
	 * Return an ISO 8601 combined date and time string for specified date/time
	 * 
	 * @param date
	 *            Date
	 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 */
	private static String getISO8601StringForDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.US);

	public static String getDuration(long millis) {

		if (millis == 0) {
			return "N/A";
		}

		final Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.HOUR, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.add(Calendar.MILLISECOND, (int) millis);

		String formatted = sdf.format(cal1.getTime());
		if (TimeUnit.MILLISECONDS.toMinutes(millis) < 60) {
			formatted = formatted.replaceFirst("12", "00");
		}
		return formatted;
	}

	public static String getElapsedTime(long startDate, long endDate){

		//milliseconds
		long different = endDate - startDate;

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		Long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		String result = "";

		if (elapsedDays>0){
			result += elapsedDays.intValue() + " d";
		}

		Long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		if (elapsedHours>0){
			if (!result.isEmpty()) result += " ";
			result += elapsedHours.intValue() + " hr";
		}

		Long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		if (elapsedMinutes>0 && elapsedDays == 0){
			if (!result.isEmpty()) result += " ";
			result += elapsedMinutes.intValue() + " min";
		}

		Long elapsedSeconds = different / secondsInMilli;

		if (elapsedMinutes < 1 && elapsedSeconds>0 && elapsedHours == 0 && elapsedDays == 0){
			if (!result.isEmpty()) result += " ";
			result += elapsedSeconds.intValue() + "s";
		}

		return result;
	}


	/**
	 * Private constructor: class cannot be instantiated
	 */
	private TimestampUtils() {
	}
}
