/**
 * Copyright © 2010-2011 Olivier Cailloux
 *
 *     This file is part of JLP.
 *
 *     JLP is free software: you can redistribute it and/or modify it under the
 *     terms of the GNU Lesser General Public License version 3 as published by
 *     the Free Software Foundation.
 *
 *     JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp.parameters;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Equivalence;
import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class LpParametersUtils {
    /**
     * Returns {@code true} iff the given parameters are equivalent, i.e. have the same value for each parameter.
     * 
     * @param a
     *            may be <code>null</code>.
     * @param b
     *            may be <code>null</code>.
     * @return <code>true</code> iff the given objects are equivalent.
     */
    static public boolean equivalent(LpParameters a, LpParameters b) {
	return getEquivalenceRelation().equivalent(a, b);
    }

    static public Map<Enum<?>, Object> getAllNullValues() {
	final HashMap<Enum<?>, Object> nullValues = Maps.newHashMap();
	nullValues.put(LpDoubleParameter.MAX_WALL_SECONDS, null);
	nullValues.put(LpDoubleParameter.MAX_CPU_SECONDS, null);
	nullValues.put(LpDoubleParameter.MAX_TREE_SIZE_MB, null);
	nullValues.put(LpIntParameter.MAX_THREADS, null);
	nullValues.put(LpStringParameter.WORK_DIR, null);
	return nullValues;
    }

    /**
     * Retrieves all the parameters, including those that have a default value, as a list of properties, using a
     * reasonable format for export (with English locale for numbers). Values that are <code>null</code> are transformed
     * to the string "null".
     * 
     * @param parameters
     *            not <code>null</code>.
     * @return not <code>null</code>.
     */
    static public Properties getAsProperties(LpParameters parameters) {
	final Properties properties = new Properties();

	for (LpStringParameter parameter : LpParametersDefaultValues.getDefaultStringValues().keySet()) {
	    final String value = parameters.getValue(parameter);
	    properties.setProperty(parameter.toString(), value == null ? "null" : value);
	}

	final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);

	formatter.setMinimumFractionDigits(3);
	formatter.setMaximumFractionDigits(3);
	for (LpDoubleParameter parameter : LpParametersDefaultValues.getDefaultDoubleValues().keySet()) {
	    final Double value = parameters.getValue(parameter);
	    properties.setProperty(parameter.toString(), value == null ? "null" : formatter.format(value));
	}

	formatter.setMinimumFractionDigits(0);
	formatter.setMaximumFractionDigits(0);
	for (LpIntParameter parameter : LpParametersDefaultValues.getDefaultIntValues().keySet()) {
	    final Integer value = parameters.getValue(parameter);
	    properties.setProperty(parameter.toString(), value == null ? "null" : formatter.format(value));
	}

	return properties;
    }

    static public Equivalence<LpParameters> getEquivalenceRelation() {
	return new Equivalence<LpParameters>() {
	    @Override
	    public boolean equivalent(LpParameters a, LpParameters b) {
		if (a == b) {
		    return true;
		}
		if (a == null || b == null) {
		    return false;
		}
		if (!a.getDoubleParameters().equals(b.getDoubleParameters())) {
		    return false;
		}
		if (!a.getIntParameters().equals(b.getIntParameters())) {
		    return false;
		}
		if (!a.getStringParameters().equals(b.getStringParameters())) {
		    return false;
		}
		return true;
	    }

	    @Override
	    public int hash(LpParameters t) {
		if (t == null) {
		    return 0;
		}
		return Objects.hashCode(t.getDoubleParameters(), t.getIntParameters(), t.getStringParameters());
	    }
	};
    }

    public static Set<Enum<?>> getParameters() {
	final SetView<Enum<?>> doublesAndStrings = Sets.union(LpParametersDefaultValues.getDefaultDoubleValues()
		.keySet(), LpParametersDefaultValues.getDefaultStringValues().keySet());

	return Sets.union(LpParametersDefaultValues.getDefaultIntValues().keySet(), doublesAndStrings);
    }

    static public Predicate<Double> getValidator(LpDoubleParameter parameter) {
	switch (parameter) {
	case MAX_CPU_SECONDS:
	    return new Predicate<Double>() {
		@Override
		public boolean apply(Double value) {
		    return value == null || value.doubleValue() > 0d;
		}
	    };
	case MAX_TREE_SIZE_MB:
	    return new Predicate<Double>() {
		@Override
		public boolean apply(Double value) {
		    return value == null || value.doubleValue() > 0d;
		}
	    };
	case MAX_WALL_SECONDS:
	    return new Predicate<Double>() {
		@Override
		public boolean apply(Double value) {
		    return value == null || value.doubleValue() > 0d;
		}
	    };
	}
	throw new IllegalStateException("Unknown parameter.");
    }

    static public Predicate<Integer> getValidator(LpIntParameter parameter) {
	switch (parameter) {
	case MAX_THREADS:
	    return new Predicate<Integer>() {
		@Override
		public boolean apply(Integer value) {
		    return value == null || value.intValue() > 0;
		}
	    };
	case DETERMINISTIC:
	    return new Predicate<Integer>() {
		@Override
		public boolean apply(Integer value) {
		    if (value == null) {
			return false;
		    }
		    final int pValue = value.intValue();
		    return pValue == 0 || pValue == 1;
		}
	    };
	}
	throw new IllegalStateException("Unknown parameter.");
    }

    static public Predicate<String> getValidator(LpStringParameter parameter) {
	switch (parameter) {
	case WORK_DIR:
	    return new Predicate<String>() {
		@Override
		public boolean apply(String value) {
		    return value == null || !value.isEmpty();
		}
	    };
	}
	throw new IllegalStateException("Unknown parameter.");
    }

    /**
     * Retrieves a hash code for the given parameter. Two equivalent objects receive the same hash code, etc. etc. (see
     * {@link Object#hashCode()}).
     * 
     * @param parameter
     *            may be <code>null</code>.
     * @return a hash code.
     */
    static public int hash(LpParameters parameter) {
	return getEquivalenceRelation().hash(parameter);
    }

    public static boolean removeAllValues(LpParameters parameters) {
	boolean modified = false;
	for (Enum<?> parameter : getParameters()) {
	    final boolean changed = parameters.setValueAsObject(parameter,
		    LpParametersDefaultValues.getDefaultValueObject(parameter));
	    modified = modified || changed;
	}
	return modified;
    }

    /**
     * Overrides all values in the target object with values in the source one, including those that have default values
     * in the source object.
     * 
     * @param target
     *            not <code>null</code>.
     * @param source
     *            not <code>null</code>.
     * 
     * @return <code>true</code> iff the state of the target object changed as a result of this call. Equivalently,
     *         <code>false</code> iff the given source equals the given target.
     */
    public static boolean setAllValues(LpParameters target, LpParameters source) {
	boolean modified = false;
	for (Enum<?> parameter : getParameters()) {
	    final boolean changed = target.setValueAsObject(parameter, source.getValueAsObject(parameter));
	    modified = modified || changed;
	}
	return modified;
    }

    public static String toString(LpParameters parameters) {
	final ToStringHelper helper = Objects.toStringHelper(parameters);
	final MapJoiner mapFormatter = Joiner.on(", ").useForNull("null").withKeyValueSeparator("=");
	final String toStrInts = mapFormatter.join(parameters.getIntParameters());
	final String toStrDoubles = mapFormatter.join(parameters.getDoubleParameters());
	final String toStrStrings = mapFormatter.join(parameters.getStringParameters());

	final Joiner joiner = Joiner.on(", ");
	final Predicate<CharSequence> isNonEmpty = Predicates.contains(Pattern.compile(".+"));
	final Iterable<String> nonEmptyMaps = Iterables.filter(
		Arrays.asList(new String[] { toStrInts, toStrDoubles, toStrStrings }), isNonEmpty);
	final String res = joiner.join(nonEmptyMaps);
	helper.addValue(res);
	return helper.toString();
	// final StringBuilder builder = new StringBuilder(helper.toString());
	// joiner.appendTo(builder, Strings.emptyToNull(toStrInts), Strings.emptyToNull(toStrDoubles),
	// Strings.emptyToNull(toStrStrings));
	// mapFormatter.appendTo(builder, getIntParameters());
	// mapFormatter.appendTo(builder, getDoubleParameters());
	// mapFormatter.appendTo(builder, getStringParameters());
	// return res;
    }
}
