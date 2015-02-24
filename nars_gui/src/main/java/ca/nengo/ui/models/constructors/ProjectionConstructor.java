package ca.nengo.ui.models.constructors;

import ca.nengo.ui.configurable.ConfigException;
import ca.nengo.ui.configurable.ConfigResult;
import ca.nengo.ui.configurable.Property;
import ca.nengo.ui.configurable.descriptors.PString;

public abstract class ProjectionConstructor extends AbstractConstructable {
	protected static final Property pName = new PString("Name");

	protected abstract boolean IsNameAvailable(String name);

	protected abstract Object createModel(ConfigResult configuredProperties, String uniqueName) throws ConfigException;

	@Override
	protected final Object configureModel(ConfigResult configuredProperties) throws ConfigException {
		String originalName = (String) configuredProperties.getValue(pName);
		String name = originalName;

		// Ensure unique name
		//
		int i = 1;
		while (!IsNameAvailable(name)) {
			name = String.format("%s (%d)", originalName, i++);
		}

		return createModel(configuredProperties, name);
	}
}
