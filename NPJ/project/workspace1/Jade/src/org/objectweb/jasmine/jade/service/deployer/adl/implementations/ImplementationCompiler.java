/**
 * Copyright (C) : INRIA - Domaine de Voluceau, Rocquencourt, B.P. 105, 
 * 78153 Le Chesnay Cedex - France 
 * 
 * contributor(s) : SARDES project - http://sardes.inrialpes.fr
 *
 * Contact : jade@inrialpes.fr
 *
 * This software is a computer program whose purpose is to provide a framework
 * to build autonomic systems, following an architecture-based approach.
 *
 * This software is governed by the CeCILL-C license under French law and 
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated with 
 * loading,  using,  modifying and/or developing or reproducing the software by 
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate,  and  that  also therefore means  that it is
 * reserved for developers  and  experienced professionals having in-depth 
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling 
 * the security of their systems and/or data to be ensured and,  more generally,
 * to use and operate it in the same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had 
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package org.objectweb.jasmine.jade.service.deployer.adl.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.FactoryProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;
import org.objectweb.deployment.scheduling.core.api.Task;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.attributes.AttributesContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;
import org.objectweb.fractal.adl.implementations.TemplateControllerContainer;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.deployer.adl.nodes.VirtualNode;
import org.objectweb.jasmine.jade.service.deployer.adl.nodes.VirtualNodeContainer;
import org.objectweb.jasmine.jade.service.deployer.adl.packages.Package;
import org.objectweb.jasmine.jade.service.deployer.adl.packages.PackageDescriptionImpl;
import org.objectweb.jasmine.jade.service.deployer.adl.packages.Packages;
import org.objectweb.jasmine.jade.service.deployer.adl.packages.PackagesContainer;
import org.objectweb.jasmine.jade.service.deployer.adl.property.Property;
import org.objectweb.jasmine.jade.service.deployer.adl.property.PropertyContainer;
import org.objectweb.jasmine.jade.util.ValueAttribute;


/**
 * A {@link PrimitiveCompiler} to compile {@link Implementation} nodes in
 * definitions.
 */
public class ImplementationCompiler implements BindingController,
		PrimitiveCompiler {

	/**
	 * Name of the mandatory interface bound to the
	 * {@link ImplementationBuilder} used by this compiler.
	 */
	public final static String BUILDER_BINDING = "builder";

	/**
	 * The {@link ImplementationBuilder} used by this compiler.
	 */

	public ImplementationBuilder builder;

	// --------------------------------------------------------------------------
	// Implementation of the BindingController interface
	// --------------------------------------------------------------------------

	public String[] listFc() {
		return new String[] { BUILDER_BINDING };
	}

	public Object lookupFc(final String itf) {
		if (itf.equals(BUILDER_BINDING)) {
			return builder;
		}
		return null;
	}

	public void bindFc(final String itf, final Object value) {
		if (itf.equals(BUILDER_BINDING)) {
			builder = (ImplementationBuilder) value;
		}
	}

	public void unbindFc(final String itf) {
		if (itf.equals(BUILDER_BINDING)) {
			builder = null;
		}
	}

	// --------------------------------------------------------------------------
	// Implementation of the Compiler interface
	// --------------------------------------------------------------------------

	public void compile(final List path, final ComponentContainer container,
			final TaskMap tasks, final Map context) throws ADLException {

		/*
		 * DEBUG begin 3 juil. 2006 jlegrand
		 */
		Task virtualNodeCleanTask = tasks.getTask("cleanNodes", "beurk");
		/*
		 * end
		 */

		String virtualNodeName = null;
		Task virtualNodeCreationTask = null;
		if (container instanceof VirtualNodeContainer) {
			VirtualNode virtualNode = ((VirtualNodeContainer) container)
					.getVirtualNode();

			if (virtualNode != null) {
				virtualNodeName = virtualNode.getName();

				try {
					virtualNodeCreationTask = tasks.getTask("allocNode",
							virtualNodeName);
				} catch (NoSuchElementException ignored) {
				}
			}
		}

		boolean template = context != null
				&& "true".equals(context.get("template"));

		/*
		 * Implementation container
		 */
		String implementation = null;
		if (container instanceof ImplementationContainer) {
			ImplementationContainer ic = (ImplementationContainer) container;
			Implementation i = ic.getImplementation();
			if (i != null) {
				implementation = i.getClassName();
			}
		}

		/*
		 * Controller container
		 */
		Object controller = null;
		if (container instanceof ControllerContainer) {
			ControllerContainer cc = (ControllerContainer) container;
			Controller ctrl = (Controller) cc.getController();
			if (ctrl != null) {
				String files = ctrl.getFiles();
				if (files != null && files.trim().length() > 0) {
					controller = new HashMap<String,String>();
					((Map<String,String>) controller).put("juliaconfig", files);
					((Map<String,String>) controller).put("controller", ctrl.getDescriptor());
				} else {
					controller = ctrl.getDescriptor();
				}
			}
		}

		/*
		 * TemplateController container
		 */
		String templateController = null;
		if (container instanceof TemplateControllerContainer) {
			TemplateControllerContainer tcc = (TemplateControllerContainer) container;
			if (tcc.getTemplateController() != null) {
				templateController = tcc.getTemplateController()
						.getDescriptor();
				template = true;
			}
		}

		/*
		 * Definition container
		 */
		String name = null;
		if (container instanceof Definition) {
			name = ((Definition) container).getName();
		} else if (container instanceof Component) {
			name = ((Component) container).getName();
		}

		String definition = null;
		if (container instanceof Definition) {
			definition = name;
		} else {
			definition = (String) ((Node) container)
					.astGetDecoration("definition");
		}

		/*
		 * Packages container
		 */
		Object[] packageDesc = null;

		if (container instanceof PackagesContainer) {

			Packages packages = ((PackagesContainer) container).getPackages();

			if (packages != null) {

				Package[] pkgs = packages.getPackages();
				List<PackageDescription> packageDescList = new ArrayList<PackageDescription>();
				Package pkg = null;

				for (int i = 0; i < pkgs.length; i++) {

					Map<String,String> propertiesMap = null;
					pkg = pkgs[i];
					Property[] properties = ((PropertyContainer) pkg)
							.getPropertys();

					if (properties != null && properties.length > 0) {

						propertiesMap = new HashMap<String,String>();

						for (int j = 0; j < properties.length; j++) {

							propertiesMap.put(properties[j].getName(),
									properties[j].getValue());

						}
					}

					packageDescList.add(new PackageDescriptionImpl(pkg
							.getName(), propertiesMap));
				}

				packageDesc = packageDescList.toArray();
			}
		}

		/*
		 * Attributes container
		 */
		boolean attrs = false;
		if (container instanceof AttributesContainer) {
			attrs = ((AttributesContainer) container).getAttributes() != null;
		}
		Component[] comps = ((ComponentContainer) container).getComponents();

		try {
			// the task may already exist, in case of a shared component
			tasks.getTask("create", container);
		} catch (NoSuchElementException e) {
			AbstractInstanceProviderTask createTask;
			if (comps.length > 0 || implementation == null) {
				if (implementation != null) {
					throw new ADLException("Implementation must be empty",
							(Node) container);
				}
				if (controller == null) {
					controller = "composite";
				}
				if (template) {
					if (templateController == null) {
						if (attrs) {
							templateController = "parametricCompositeTemplate";
						} else {
							templateController = "compositeTemplate";
						}
					}

					if (virtualNodeCreationTask != null) {

						createTask = newRemoteCreateTask(path, container, name,
								definition, templateController, new Object[] {
										controller, null }, packageDesc,
								virtualNodeCreationTask, context);

						createTask.addPreviousTask(virtualNodeCreationTask);

					} else {

						createTask = newCreateTask(path, container, name,
								definition, templateController, new Object[] {
										controller, null }, packageDesc,
								context);
					}

				} else {
					if (virtualNodeCreationTask != null) {

						createTask = newRemoteCreateTask(path, container, name,
								definition, controller, null, packageDesc,
								virtualNodeCreationTask, context);

						createTask.addPreviousTask(virtualNodeCreationTask);

					} else {
						createTask = newCreateTask(path, container, name,
								definition, controller, null, packageDesc,
								context);
					}
				}
			} else {
				if (controller == null) {
					controller = "primitive";
				}
				if (template) {
					if (templateController == null) {
						if (attrs) {
							templateController = "parametricPrimitiveTemplate";
						} else {
							templateController = "primitiveTemplate";
						}
					}

					if (virtualNodeCreationTask != null) {
						createTask = newRemoteCreateTask(path, container, name,
								definition, templateController, new Object[] {
										controller, implementation },
								packageDesc, virtualNodeCreationTask, context);
						createTask.addPreviousTask(virtualNodeCreationTask);
					} else {
						createTask = newCreateTask(path, container, name,
								definition, templateController, new Object[] {
										controller, implementation },
								packageDesc, context);
					}
				} else {
					if (virtualNodeCreationTask != null) {
						createTask = newRemoteCreateTask(path, container, name,
								definition, controller, implementation,
								packageDesc, virtualNodeCreationTask, context);
						createTask.addPreviousTask(virtualNodeCreationTask);
					} else {
						createTask = newCreateTask(path, container, name,
								definition, controller, implementation,
								packageDesc, context);
					}
				}
			}

			FactoryProviderTask typeTask = (FactoryProviderTask) tasks.getTask(
					"type", container);
			createTask.setFactoryProviderTask(typeTask);

			/*
			 * DEBUG begin 3 juil. 2006 jlegrand
			 */
			virtualNodeCleanTask.addPreviousTask(createTask);
			/*
			 * end
			 */

			tasks.addTask("create", container, createTask);
		}
	}

	public AbstractInstanceProviderTask newCreateTask(final List path,
			final ComponentContainer container, final String name,
			final String definition, final Object controller,
			final Object implementation, final Object[] packageDesc,
			final Map context) {

		return new CreateTask(builder, name, definition, controller,
				implementation, packageDesc);
	}

	public AbstractInstanceProviderTask newRemoteCreateTask(final List path,
			final ComponentContainer container, final String name,
			final String definition, final Object controller,
			final Object implementation, final Object[] packageDesc,
			Task virtualNodeCreationTask, final Map context) {

		return new RemoteCreateTask(builder, virtualNodeCreationTask, name,
				definition, controller, implementation, packageDesc);
	}

	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	static class CreateTask extends AbstractInstanceProviderTask {

		ImplementationBuilder builder;

		String name;

		String definition;

		Object controllerDesc;

		Object contentDesc;

		Object[] packageDesc;

		public CreateTask(final ImplementationBuilder builder,
				final String name, final String definition,
				final Object controllerDesc, final Object contentDesc,
				final Object[] packageDesc) {

			this.builder = builder;
			this.name = name;
			this.definition = definition;
			this.controllerDesc = controllerDesc;
			this.contentDesc = contentDesc;
			this.packageDesc = packageDesc;
		}

		public void execute(final Object context) throws Exception {
			if (getInstance() != null) {
				return;
			}
			Object type = getFactoryProviderTask().getFactory();

			Object result = builder.createComponent(type, name, definition,
					controllerDesc, contentDesc, packageDesc, context);
			setInstance(result);
		}

		public String toString() {
			return "T" + System.identityHashCode(this) + "[CreateTask(" + name
					+ "," + controllerDesc + "," + contentDesc + ")]";
		}
	}

	// ------------------------------------------------------------------------
	// 
	// ------------------------------------------------------------------------

	static class RemoteCreateTask extends CreateTask {

		Task virtualNodeCreationTask;

		public RemoteCreateTask(final ImplementationBuilder builder,
				Task virtualNodeCreationTask, final String name,
				final String definition, final Object controllerDesc,
				final Object contentDesc, final Object[] packageDesc) {

			super(builder, name, definition, controllerDesc, contentDesc,
					packageDesc);
			this.virtualNodeCreationTask = virtualNodeCreationTask;

		}

		@SuppressWarnings("unchecked")
		public void execute(final Object context) throws Exception {
			if (getInstance() != null) {
				return;
			}
			Object type = getFactoryProviderTask().getFactory();

			org.objectweb.fractal.api.Component node = (org.objectweb.fractal.api.Component) virtualNodeCreationTask
					.getResult();

			if (node == null) {
				throw new Exception("no node allocated");
			}

			Object value = ((ValueAttribute) Fractal.getAttributeController(node)).getValue();
			/*
			 * physicalNodeName is fixed in NodeLauncher, at the creation of the
			 * node
			 */
			String factoryName = Fractal.getNameController(node).getFcName();

			// TODO: remove
			// String factoryName = physicalNodeName + "_factory";

			((Map) context).put("factoryName", factoryName);
			((Map) context).put("value", value);


			Object result = builder.createComponent(type, name, definition,
					controllerDesc, contentDesc, packageDesc, context);
			setInstance(result);
		}

		public String toString() {
			return "T" + System.identityHashCode(this) + "[CreateTask(" + name
					+ "," + controllerDesc + "," + contentDesc + ")]";
		}
	}
}
