/**
 * This package contains specialized aircraft classes that can replace {@link com.ogerardin.xplane.aircraft.Aircraft}
 * to provide additional functionalty for certain aircraft.
 * To implement a custom aircraft class:<ul>
 * <li>extend {@link com.ogerardin.xplane.aircraft.Aircraft}</li>
 * <li>provide a constructor that takes {@link com.ogerardin.xplane.XPlane} and {@link com.ogerardin.xplane.file.AcfFile}.
 * The constructor must succeed only if the specified ACF file is the target for this class, otherwise it should
 * throw {@link java.lang.InstantiationException}. You may use {@link com.ogerardin.xplane.util.IntrospectionHelper#require}
 * to check for a condition</li>
 * <li>override methods as needed</li>
 * </ul>
 */
package com.ogerardin.xplane.aircraft.custom;

