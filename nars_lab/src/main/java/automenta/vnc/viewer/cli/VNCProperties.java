// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.viewer.cli;

import automenta.vnc.utils.Strings;
import automenta.vnc.viewer.swing.gui.ConnectionsHistory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line interface parameters parser
 */
@Deprecated public class VNCProperties {
	private final Map<String, Option> options = new LinkedHashMap<>();
	private final List<String> plainOptions = new ArrayList<>();
	private boolean isSetPlainOptions = false;

    public VNCProperties(String host, int port) {
        addOption(ConnectionsHistory.NODE_HOST_NAME, host, "");
        addOption(ConnectionsHistory.NODE_PORT_NUMBER, Integer.toString(port), "");
    }

	public void addOption(String opName, String defaultValue, String desc) {
		Option op = new Option(opName, defaultValue, desc);
		options.put(opName.toLowerCase(), op);
	}

	public void parse(String... args) {
		for (String p : args) {
			if (p.startsWith("-")) {
				int skipMinuses = p.startsWith("--") ? 2 : 1;
				String[] params = p.split("=", 2);
				Option op = options.get(params[0].toLowerCase().substring(skipMinuses));
				if (op != null) {
					op.isSet = true;
					if (params.length > 1 && ! Strings.isTrimmedEmpty(params[1])) {
						op.value = params[1];
					}
				}
			} else if ( ! p.startsWith("-")) {
				isSetPlainOptions = true;
				plainOptions.add(p);
			}
		}
	}

	public String getValueFor(String param) {
		Option op = options.get(param.toLowerCase());
		return op != null ? op.value : null;
	}

	public boolean isSet(String param) {
		Option op = options.get(param.toLowerCase());
		return op != null && op.isSet;
	}

	public boolean isSetPlainOptions() {
		return isSetPlainOptions;
	}

	public String getPlainOptionAt(int index) {
		return plainOptions.get(index);
	}

	public int getPlainOptionsNumber() {
		return plainOptions.size();
	}

	/**
	 * Command line interface option
	 */
	private static class Option {
		protected final String opName;
        protected final String defaultValue;
        protected final String desc;
        protected String value;
		protected boolean isSet = false;
		public Option(String opName, String defaultValue, String desc) {
			this.opName = opName;
			this.defaultValue = defaultValue;
			this.desc = desc;
			this.value = defaultValue;
		}
	}

	public String optionsUsage() {
		StringBuilder sb = new StringBuilder();
		int maxNameLength = 0;
		for (Option op : options.values()) {
			maxNameLength = Math.max(maxNameLength, op.opName.length());
		}
		for (Option op : options.values()) {
			sb.append(" -").append(op.opName);
			for (int i=0; i<maxNameLength - op.opName.length(); ++i) {
				sb.append(' ');
			}
			sb.append(" : ").append(op.desc).append('\n');
		}
		return sb.toString();
	}
}
