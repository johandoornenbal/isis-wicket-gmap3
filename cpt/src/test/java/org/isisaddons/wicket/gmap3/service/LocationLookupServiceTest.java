/*
 *  Copyright 2013~2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.wicket.gmap3.service;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.resource.ResourceStreamSource;

import org.isisaddons.wicket.gmap3.cpt.applib.Location;
import org.isisaddons.wicket.gmap3.cpt.service.LocationLookupService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LocationLookupServiceTest {

	private LocationLookupService locationLookupService;
	
	@Before
	public void setup() {
		locationLookupService = new LocationLookupService(){
			@Override
			protected IsisConfiguration getConfiguration(){
				class MockConfig implements IsisConfiguration {
					@Override public IsisConfiguration createSubset(final String prefix) {
						return null;
					}

					@Override public boolean getBoolean(final String name) {
						return false;
					}

					@Override public boolean getBoolean(final String name, final boolean defaultValue) {
						return false;
					}

					@Override public Color getColor(final String name) {
						return null;
					}

					@Override public Color getColor(final String name, final Color defaultValue) {
						return null;
					}

					@Override public Font getFont(final String name) {
						return null;
					}

					@Override public Font getFont(final String name, final Font defaultValue) {
						return null;
					}

					@Override public String[] getList(final String name) {
						return new String[0];
					}

					@Override public String[] getList(final String name, final String defaultListAsCommaSeparatedArray) {
						return new String[0];
					}

					@Override public int getInteger(final String name) {
						return 0;
					}

					@Override public int getInteger(final String name, final int defaultValue) {
						return 0;
					}

					@Override public IsisConfiguration getProperties(final String withPrefix) {
						return null;
					}

					@Override public String getString(final String str){
						return "[A_VALID_API_KEY_HERE]";
					}

					@Override public String getString(final String name, final String defaultValue) {
						return null;
					}

					@Override public boolean hasProperty(final String name) {
						return false;
					}

					@Override public boolean isEmpty() {
						return false;
					}

					@Override public Iterator<String> iterator() {
						return null;
					}

					@Override public Iterable<String> asIterable() {
						return null;
					}

					@Override public int size() {
						return 0;
					}

					@Override public ResourceStreamSource getResourceStreamSource() {
						return null;
					}

					@Override public Map<String, String> asMap() {
						return null;
					}
				}
				return new MockConfig();
			}
		};
	}
	
	@Test
	public void whenValid() {
		Location location = locationLookupService.lookup("10 Downing Street,London,UK");
		assertThat(location, is(not(nullValue())));
		assertEquals(51.503, location.getLatitude(), 0.01);
		assertEquals(-0.128, location.getLongitude(), 0.01);
	}

	@Test
	public void whenInvalid() {
		Location location = locationLookupService.lookup("$%$%^Y%^fgnsdlfk glfg");
		assertThat(location, is(nullValue()));
	}

}
