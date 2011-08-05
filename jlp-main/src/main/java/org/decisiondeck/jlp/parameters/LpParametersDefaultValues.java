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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

public class LpParametersDefaultValues {
    private static Map<LpIntParameter, Integer> s_ints = null;

    private LpParametersDefaultValues() {
	/** Non-instantiable. */
    }

    /**
     * Retrieves the default value associated to the given parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return the default value, possibly <code>null</code> as this is a meaningful value for some parameters.
     */
    static public Double getDefaultValue(LpDoubleParameter parameter) {
	lazyInit();
	return s_doubles.get(parameter);
    }

    private static void lazyInit() {
	if (s_doubles == null) {
	    /** Unfotunately guava's ImmutableMap does not accept null values. */
	    // final Builder<IlpDoubleParameter, Double> doublesBuilder = ImmutableMap.builder();
	    // doublesBuilder.put(IlpDoubleParameter.MAX_CPU_SECONDS, null);
	    // doublesBuilder.put(IlpDoubleParameter.MAX_TREE_SIZE_MB, null);
	    // doublesBuilder.put(IlpDoubleParameter.MAX_WALL_SECONDS, null);
	    // s_doubles = doublesBuilder.build();
	    //
	    // final Builder<IlpIntParameter, Integer> intsBuilder = ImmutableMap.builder();
	    // intsBuilder.put(IlpIntParameter.MAX_THREADS_NULLABLE, null);
	    // s_ints = intsBuilder.build();
	    //
	    // final Builder<IlpStringParameter, String> stringsBuilder = ImmutableMap.builder();
	    // stringsBuilder.put(IlpStringParameter.WORK_DIR_NULLABLE, null);
	    // s_strings = stringsBuilder.build();

	    s_doubles = Maps.newHashMap();
	    s_doubles.put(LpDoubleParameter.MAX_CPU_SECONDS, null);
	    s_doubles.put(LpDoubleParameter.MAX_TREE_SIZE_MB, null);
	    s_doubles.put(LpDoubleParameter.MAX_WALL_SECONDS, null);
	    s_doubles = Collections.unmodifiableMap(s_doubles);

	    s_ints = Maps.newHashMap();
	    s_ints.put(LpIntParameter.MAX_THREADS, null);
	    s_ints.put(LpIntParameter.DETERMINISTIC, Integer.valueOf(0));
	    s_ints = Collections.unmodifiableMap(s_ints);

	    s_strings = Maps.newHashMap();
	    s_strings.put(LpStringParameter.WORK_DIR, null);
	    s_strings = Collections.unmodifiableMap(s_strings);

	    assert (s_doubles.size() == LpDoubleParameter.values().length);
	    assert (s_ints.size() == LpIntParameter.values().length);
	    assert (s_strings.size() == LpStringParameter.values().length);
	}
    }

    /**
     * Retrieves the default double values.
     * 
     * @return a (possibly read-only) copy of the default values as a map.
     */
    static public Map<LpDoubleParameter, Double> getDefaultDoubleValues() {
	lazyInit();
	return s_doubles;
    }

    /**
     * Retrieves the default integer values.
     * 
     * @return a (possibly read-only) copy of the default values as a map.
     */
    static public Map<LpIntParameter, Integer> getDefaultIntValues() {
	lazyInit();
	return s_ints;
    }

    /**
     * Retrieves the default string values.
     * 
     * @return a (possibly read-only) copy of the default values as a map.
     */
    static public Map<LpStringParameter, String> getDefaultStringValues() {
	lazyInit();
	return s_strings;
    }

    /**
     * Retrieves the default value associated to the given parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return the default value, possibly <code>null</code> as this is a meaningful value for some parameters.
     */
    static public Integer getDefaultValue(LpIntParameter parameter) {
	lazyInit();
	return s_ints.get(parameter);
    }

    /**
     * Retrieves the default value associated to the given parameter. This method allows for more flexible use as the
     * type of the parameter must not be known but the parameter must be a correct type, otherwise an exception is
     * thrown.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return the default value, possibly <code>null</code> as this is a meaningful value for some parameters.
     */
    static public Object getDefaultValueObject(Object parameter) {
	lazyInit();
	final Object value;
	if (parameter instanceof LpIntParameter) {
	    LpIntParameter intParameter = (LpIntParameter) parameter;
	    value = s_ints.get(intParameter);
	} else if (parameter instanceof LpDoubleParameter) {
	    LpDoubleParameter doubleParameter = (LpDoubleParameter) parameter;
	    value = s_doubles.get(doubleParameter);
	} else if (parameter instanceof LpStringParameter) {
	    LpStringParameter stringParameter = (LpStringParameter) parameter;
	    value = s_strings.get(stringParameter);
	} else {
	    throw new IllegalArgumentException("Unknown parameter type.");
	}
	return value;
    }

    /**
     * Retrieves the default value associated to the given parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return the default value, possibly <code>null</code> as this is a meaningful value for some parameters.
     */
    static public String getDefaultValue(LpStringParameter parameter) {
	lazyInit();
	return s_strings.get(parameter);
    }

    private static Map<LpDoubleParameter, Double> s_doubles = null;
    private static Map<LpStringParameter, String> s_strings = null;
}
