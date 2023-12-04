/**
 * This package contains specialized scenery classes that can replace {@link com.ogerardin.xplane.scenery.SceneryPackage}
 * to provide additional functionalty for certain scenery packages.
 * To implement a custom scenery class:<ul>
 * <li>extend {@link com.ogerardin.xplane.scenery.SceneryPackage}</li>
 * <li>provide a constructor that takes a {@link java.nio.file.Path} that represents the scenery folder.
 * The constructor must succeed only if the specified folder the target for this class, otherwise it should
 * throw {@link java.lang.InstantiationException}. You may use {@link com.ogerardin.xplane.util.IntrospectionHelper#require}
 * to check for a condition</li>
 * <li>override methods as needed</li>
 * </ul>
 */
package com.ogerardin.xplane.scenery.custom;

