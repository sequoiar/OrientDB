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
package com.orientechnologies.orient.core.query.sql;

import com.orientechnologies.orient.core.query.operator.OQueryOperator;
import com.orientechnologies.orient.core.record.ORecordSchemaAware;

/**
 * Run-time query condition evaluator.
 * 
 * @author luca
 * 
 */
public class OSQLCondition {
	private static final String	NULL_VALUE	= "null";
	protected Object						left;
	protected OQueryOperator		operator;
	protected Object						right;

	public OSQLCondition(Object iLeft, OQueryOperator iOperator) {
		this.left = iLeft;
		this.operator = iOperator;
	}

	public OSQLCondition(Object iLeft, OQueryOperator iOperator, Object iRight) {
		this.left = iLeft;
		this.operator = iOperator;
		this.right = iRight;
	}

	public Object evaluate(ORecordSchemaAware<?> iRecord) {
		Object l = evaluate(iRecord, left);
		Object r = evaluate(iRecord, right);

		Object[] convertedValues = checkForConversion(l, r);
		if (convertedValues != null) {
			l = convertedValues[0];
			r = convertedValues[1];
		}

		return operator.evaluate(this, l, r);
	}

	private Object[] checkForConversion(Object l, Object r) {
		Object[] result = new Object[] { l, r };

		// INTEGERS
		if (r instanceof Integer && !(l instanceof Integer)) {
			if (l instanceof String && ((String) l).indexOf(".") > -1)
				result[0] = new Float((String) l).intValue();
			else
				result[0] = getInteger(l);
		} else if (l instanceof Integer && !(r instanceof Integer)) {
			if (r instanceof String && ((String) r).indexOf(".") > -1)
				result[1] = new Float((String) r).intValue();
			else
				result[1] = getInteger(r);
		}

		// FLOATS
		else if (r instanceof Float && !(l instanceof Float))
			result[0] = getFloat(l);
		else if (l instanceof Float && !(r instanceof Float))
			result[1] = getFloat(r);
		else
			// AVOID COPY SINCE NO CONVERSION HAPPENED
			return null;

		return result;
	}

	protected Integer getInteger(Object iValue) {
		if (iValue == null)
			return null;

		String stringValue = iValue.toString();

		if (NULL_VALUE.equals(stringValue))
			return null;

		if (stringValue.contains(".") || stringValue.contains(","))
			return (int) Float.parseFloat(stringValue);
		else
			return stringValue.length() > 0 ? new Integer(stringValue) : new Integer(0);
	}

	protected Float getFloat(Object iValue) {
		if (iValue == null)
			return null;

		String stringValue = iValue.toString();

		if (NULL_VALUE.equals(stringValue))
			return null;

		return stringValue.length() > 0 ? new Float(stringValue) : new Float(0);
	}

	protected Object evaluate(ORecordSchemaAware<?> iRecord, Object iValue) {
		if (iValue instanceof OSQLValue)
			return ((OSQLValue) iValue).getValue(iRecord);

		if (iValue instanceof OSQLCondition)
			// NESTED CONDITION: EVALUATE IT RECURSIVELY
			return ((OSQLCondition) iValue).evaluate(iRecord);

		// SIMPLE VALUE: JUST RETURN IT
		return iValue;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(');
		buffer.append(left);
		buffer.append(' ');
		buffer.append(operator);
		buffer.append(' ');
		buffer.append(right);
		buffer.append(')');

		return buffer.toString();
	}

	public Object getLeft() {
		return left;
	}

	public Object getRight() {
		return right;
	}
}