/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.serialization.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.orientechnologies.orient.core.record.ORecord;

@SuppressWarnings("unchecked")
public class OJSONWriter {
	private Writer	out;
	private boolean	prettyPrint			= true;
	private boolean	firstAttribute	= true;

	public OJSONWriter(Writer out) {
		this.out = out;
	}

	public void beginObject() throws IOException {
		beginObject(0, false, null);
	}

	public void beginObject(final int iIdentLevel, final boolean iNewLine, final Object iName) throws IOException {
		if (!firstAttribute)
			out.append(", ");

		format(iIdentLevel, iNewLine);

		if (iName != null)
			out.append("'" + iName.toString() + "':");

		out.append(" {");

		firstAttribute = true;
	}

	public void endObject() throws IOException {
		format(0, true);
		out.append("}");
	}

	public void endObject(final int iIdentLevel, final boolean iNewLine) throws IOException {
		format(iIdentLevel, iNewLine);
		out.append("}");
	}

	public void beginCollection(final int iIdentLevel, final boolean iNewLine, final String iName) throws IOException {
		if (!firstAttribute)
			out.append(", ");

		format(iIdentLevel, iNewLine);

		out.append(writeValue(iName));
		out.append(": [");

		firstAttribute = true;
	}

	public void endCollection(final int iIdentLevel, final boolean iNewLine) throws IOException {
		format(iIdentLevel, iNewLine);
		out.append("]");
	}

	public void writeAttribute(final int iIdentLevel, final boolean iNewLine, final String iName, final Object iValue)
			throws IOException {
		if (!firstAttribute)
			out.append(", ");

		format(iIdentLevel, iNewLine);

		out.append(writeValue(iName));
		out.append(": ");
		out.append(writeValue(iValue));

		firstAttribute = false;
	}

	public void writeValue(final int iIdentLevel, final boolean iNewLine, final Object iValue) throws IOException {
		if (!firstAttribute)
			out.append(", ");

		format(iIdentLevel, iNewLine);

		out.append(writeValue(iValue));

		firstAttribute = false;
	}

	public static String writeValue(final Object iValue) throws IOException {
		StringBuilder buffer = new StringBuilder();

		if (iValue == null)
			buffer.append("null");

		else if (iValue instanceof ORecord<?>) {
			ORecord<?> linked = (ORecord<?>) iValue;
			if (linked.getIdentity().isValid())
				buffer.append(linked.getIdentity().toString());
			else
				buffer.append("\"" + linked.toString() + "\"");

		} else if (iValue.getClass().isArray()) {

			if (iValue instanceof byte[]) {
				buffer.append("'");
				for (int i = 0; i < Array.getLength(iValue); ++i) {
					buffer.append(String.format("%03d", Array.getByte(iValue, i)));
				}
				buffer.append("'");
			} else {
				buffer.append("[");
				for (int i = 0; i < Array.getLength(iValue); ++i) {
					if (i > 0)
						buffer.append(", ");
					buffer.append(writeValue(Array.get(iValue, i)));
				}
				buffer.append("]");
			}

		} else if (iValue instanceof Collection<?>) {
			Collection<Object> coll = (Collection<Object>) iValue;
			buffer.append("[");
			int i = 0;
			for (Iterator<Object> it = coll.iterator(); it.hasNext(); ++i) {
				if (i > 0)
					buffer.append(", ");
				buffer.append(writeValue(it.next()));
			}
			buffer.append("]");

		} else if (iValue instanceof Map<?, ?>) {
			Map<Object, Object> map = (Map<Object, Object>) iValue;
			buffer.append("{");
			int i = 0;
			Entry<Object, Object> entry;
			for (Iterator<Entry<Object, Object>> it = map.entrySet().iterator(); it.hasNext(); ++i) {
				entry = it.next();
				if (i > 0)
					buffer.append(", ");
				buffer.append(writeValue(entry.getKey()));
				buffer.append(": ");
				buffer.append(writeValue(entry.getValue()));
			}
			buffer.append("}");

		} else if (iValue instanceof String) {
			buffer.append('\'');
			buffer.append(iValue.toString());
			buffer.append('\'');

		} else
			buffer.append(iValue.toString());

		return buffer.toString();
	}

	public void flush() throws IOException {
		out.flush();
	}

	public void close() throws IOException {
		out.close();
	}

	private void format(final int iIdentLevel, final boolean iNewLine) throws IOException {
		if (iNewLine)
			out.append("\n");

		if (prettyPrint)
			for (int i = 0; i < iIdentLevel; ++i)
				out.append("  ");
	}

	public void append(final String iText) throws IOException {
		out.append(iText);
	}
}