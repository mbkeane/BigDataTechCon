package com.conversantmedia.tutorial.flume;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple demonstration of Interceptor changing event content.
 *
 * Created by mkeane on 11/1/15.
 */
public class ColumnFilterInterceptor implements Interceptor {

	private static final Logger logger
			= LoggerFactory.getLogger(ColumnFilterInterceptor.class);
	private final List<Integer> columns;

	public ColumnFilterInterceptor(List<Integer> columns) {
		this.columns = columns;
	}

	public void initialize() {
		// no-op
	}

	public Event intercept(Event event) {
		String values;
		if (!columns.equals(Collections.emptyList())) {
			try {
				StringBuilder result = new StringBuilder();
				values = new String(event.getBody(), "UTF-8");
				String[] array = values.split(",", -1);
				for (int i : columns) {
					result.append(array[i]).append(",");
				}
				event.setBody(result.substring(0, result.length() - 1).getBytes());
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	/**
	 * Read java doc for this method - if you return null things go bad...
	 *
	 * @param events
	 * @return
	 */
	public List<Event> intercept(List<Event> events) {
		for (Event event : events) {
			intercept(event);
		}
		return events;
	}

	public void close() {
		// no-op
	}

	/**
	 * Builder which builds new instance of the ChannelBifurcationInterceptor.
	 */
	public static class Builder implements Interceptor.Builder {

		private List<Integer> columns = new ArrayList<>();

		public void configure(Context context) {
			String[] array = context.getString("columns", "").split(",");
			if (array.length < 1) {
				columns = Collections.EMPTY_LIST;
			}
			else {
				for (String col : array) {
					columns.add(NumberUtils.toInt(col, 0));
				}
			}
		}

		@Override
		public Interceptor build() {
			logger.info("Creating ColumnFilterInterceptor");
			return new ColumnFilterInterceptor(columns);
		}
	}
}

