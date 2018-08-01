/*
 * Copyright (c) 2018 Evolveum
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

package com.evolveum.midpoint.gui.impl.page.admin.configuration.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.DisplayNamePanel;
import com.evolveum.midpoint.gui.api.component.autocomplete.AutoCompleteTextPanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.impl.component.MultivalueContainerDetailsPanel;
import com.evolveum.midpoint.gui.impl.component.MultivalueContainerListPanel;
import com.evolveum.midpoint.gui.impl.component.MultivalueContainerListPanelWithDetailsPanel;
import com.evolveum.midpoint.gui.impl.component.data.column.EditableLinkColumnForContainerWrapper;
import com.evolveum.midpoint.gui.impl.component.data.column.EditableTextColumnForContainerWrapper;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AllFilter;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.TypeFilter;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.data.column.CheckBoxHeaderColumn;
import com.evolveum.midpoint.web.component.data.column.IconColumn;
import com.evolveum.midpoint.web.component.data.column.InlineMenuButtonColumn;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.form.Form;
import com.evolveum.midpoint.web.component.input.DropDownChoicePanel;
import com.evolveum.midpoint.web.component.input.ListMultipleChoicePanel;
import com.evolveum.midpoint.web.component.input.StringChoiceRenderer;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.prism.ContainerValueWrapper;
import com.evolveum.midpoint.web.component.prism.ContainerWrapper;
import com.evolveum.midpoint.web.component.prism.ItemVisibility;
import com.evolveum.midpoint.web.component.prism.ItemWrapper;
import com.evolveum.midpoint.web.component.prism.ObjectWrapperFactory;
import com.evolveum.midpoint.web.component.prism.PrismContainerHeaderPanel;
import com.evolveum.midpoint.web.component.prism.PrismContainerPanel;
import com.evolveum.midpoint.web.component.prism.PropertyWrapper;
import com.evolveum.midpoint.web.component.prism.ValueWrapper;
import com.evolveum.midpoint.web.page.admin.configuration.dto.StandardLoggerType;
import com.evolveum.midpoint.web.session.PageStorage;
import com.evolveum.midpoint.web.session.UserProfileStorage;
import com.evolveum.midpoint.web.session.UserProfileStorage.TableId;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AppenderConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AuditingConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClassLoggerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingComponentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LoggingLevelType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;

/**
 * @author skublik
 */
public class LoggingConfigurationTabPanel extends BasePanel<ContainerWrapper<LoggingConfigurationType>> {

	private static final long serialVersionUID = 1L;
	
	private static final Trace LOGGER = TraceManager.getTrace(LoggingConfigurationTabPanel.class);
	
	private static final String ID_LOGGING = "logging";
	private static final String ID_APPENDERS_HEADER = "appendersHeader";
	private static final String ID_APPENDERS = "appenders";
	private static final String ID_LOGGERS_HEADER = "loggersHeader";
    private static final String ID_LOGGERS = "loggers";
    private static final String ID_AUDITING = "audit";
    private static final String ID_APPENDER_SEARCH_FRAGMENT = "appenderSearchFragment";
    private static final String ID_LOGGER_SEARCH_FRAGMENT = "appenderSearchFragment";
    protected static final String ID_SPECIFIC_CONTAINERS_FRAGMENT = "specificContainersFragment";
    protected static final String ID_SPECIFIC_CONTAINER = "specificContainers";

//	private static final String TASK_CREATE_APPENDER = "createAppender";
    
    
    IModel<ContainerWrapper<AppenderConfigurationType>> appenderModel = null;
    IModel<ContainerWrapper<ClassLoggerConfigurationType>> loggerModel = null;
    
    public LoggingConfigurationTabPanel(String id, IModel<ContainerWrapper<LoggingConfigurationType>> model) {
        super(id, model);
		
    }

    @Override
    protected void onInitialize() {
    		super.onInitialize();
    		initLayout();
    }
    
    protected void initLayout() {
    	
    	PrismContainerPanel<LoggingConfigurationType> loggingPanel = new PrismContainerPanel<LoggingConfigurationType>(ID_LOGGING, getModel(), true, new Form<>("form"), itemWrapper -> getLoggingVisibility(itemWrapper.getPath()), getPageBase());
    	add(loggingPanel);
    	
    	TableId tableId = UserProfileStorage.TableId.OBJECT_POLICIES_TAB_TABLE;
    	int itemPerPage = (int) ((PageBase)LoggingConfigurationTabPanel.this.getPage()).getItemsPerPage(UserProfileStorage.TableId.OBJECT_POLICIES_TAB_TABLE);
    	PageStorage pageStorage = ((PageBase)LoggingConfigurationTabPanel.this.getPage()).getSessionStorage().getObjectPoliciesConfigurationTabStorage();
    	
    	ContainerValueWrapper<LoggingConfigurationType> containerValueWrapper = getModelObject().getValues().get(0);
    	
    	ContainerWrapper<ClassLoggerConfigurationType> loggersContainerWrap = containerValueWrapper.findContainerWrapper(new ItemPath(getModel().getObject().getPath(), LoggingConfigurationType.F_CLASS_LOGGER));
    	IModel<ContainerWrapper<ClassLoggerConfigurationType>> loggerModel = new AbstractReadOnlyModel<ContainerWrapper<ClassLoggerConfigurationType>>() {

    		private static final long serialVersionUID = 1L;

			@Override
    		public ContainerWrapper<ClassLoggerConfigurationType> getObject() {
    			return loggersContainerWrap;
    		}

    	};
    	
    	this.loggerModel = loggerModel;
    	
    	PrismContainerHeaderPanel<ClassLoggerConfigurationType> loggersHeader = new PrismContainerHeaderPanel<ClassLoggerConfigurationType>(ID_LOGGERS_HEADER, loggerModel) {
    		
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isAddButtonVisible() {
    			return false;
    		};
    		
    	};
    	add(loggersHeader);
    	
    	MultivalueContainerListPanel<ClassLoggerConfigurationType> loggersMultivalueContainerListPanel = new MultivalueContainerListPanel<ClassLoggerConfigurationType>(ID_LOGGERS, loggerModel,
    			tableId, itemPerPage, pageStorage) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected List<ContainerValueWrapper<ClassLoggerConfigurationType>> postSearch(
					List<ContainerValueWrapper<ClassLoggerConfigurationType>> items) {
				return getLoggers();
			}
			
			@Override
			protected void newItemPerformed(AjaxRequestTarget target) {
				PrismContainerValue<ClassLoggerConfigurationType> newLogger = loggerModel.getObject().getItem().createNewValue();
		        ContainerValueWrapper<ClassLoggerConfigurationType> newLoggerWrapper = getLoggersMultivalueContainerListPanel().createNewItemContainerValueWrapper(newLogger, loggerModel);
		        newLoggerWrapper.setShowEmpty(true, false);
		        newLoggerWrapper.computeStripes();
		        loggerEditPerformed(target, Model.of(newLoggerWrapper), null);
			}
			
			@Override
			protected void initPaging() {
				initAppenderPaging(); 
			}
			
			@Override
			protected Fragment getSearchPanel(String contentAreaId) {
				return new Fragment(contentAreaId, ID_LOGGER_SEARCH_FRAGMENT, LoggingConfigurationTabPanel.this);
			}
			
			@Override
			protected boolean enableActionNewObject() {
				return true;
			}
			
			@Override
			protected ObjectQuery createQuery() {
			        return LoggingConfigurationTabPanel.this.createAppendersQuery();
			}
			
			@Override
			protected List<IColumn<ContainerValueWrapper<ClassLoggerConfigurationType>, String>> createColumns() {
				return initLoggersBasicColumns();
			}

			@Override
			protected void initCustomLayout() {
				
			}

			@Override
			protected void itemPerformedForDefaultAction(AjaxRequestTarget target,
					IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel,
					List<ContainerValueWrapper<ClassLoggerConfigurationType>> listItems) {
				loggerEditPerformed(target, rowModel, listItems);
			}
		};
		
		add(loggersMultivalueContainerListPanel);
    	
    	ContainerWrapper<AppenderConfigurationType> containerWrap = containerValueWrapper.findContainerWrapper(new ItemPath(getModel().getObject().getPath(), LoggingConfigurationType.F_APPENDER));
    	IModel<ContainerWrapper<AppenderConfigurationType>> appenderModel = new AbstractReadOnlyModel<ContainerWrapper<AppenderConfigurationType>>() {

    		private static final long serialVersionUID = 1L;

			@Override
    		public ContainerWrapper<AppenderConfigurationType> getObject() {
    			return containerWrap;
    		}

    	};
    	
    	this.appenderModel = appenderModel;
    	
    	PrismContainerHeaderPanel<AppenderConfigurationType> appenderHeader = new PrismContainerHeaderPanel<AppenderConfigurationType>(ID_APPENDERS_HEADER, appenderModel) {
    		
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isAddButtonVisible() {
    			return false;
    		};
    	};
    	add(appenderHeader);
    	
    	MultivalueContainerListPanelWithDetailsPanel<AppenderConfigurationType> appendersMultivalueContainerListPanel = new MultivalueContainerListPanelWithDetailsPanel<AppenderConfigurationType>(ID_APPENDERS, appenderModel,
    			tableId, itemPerPage, pageStorage) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected List<ContainerValueWrapper<AppenderConfigurationType>> postSearch(
					List<ContainerValueWrapper<AppenderConfigurationType>> items) {
				return getAppenders();
			}
			
			@Override
			protected void newItemPerformed(AjaxRequestTarget target) {
				newAppendersClickPerformed(target);
			}
			
			@Override
			protected void initPaging() {
				initAppenderPaging(); 
			}
			
			@Override
			protected Fragment getSearchPanel(String contentAreaId) {
				return new Fragment(contentAreaId, ID_APPENDER_SEARCH_FRAGMENT, LoggingConfigurationTabPanel.this);
			}
			
			@Override
			protected boolean enableActionNewObject() {
				return true;
			}
			
			@Override
			protected ObjectQuery createQuery() {
			        return LoggingConfigurationTabPanel.this.createAppendersQuery();
			}
			
			@Override
			protected List<IColumn<ContainerValueWrapper<AppenderConfigurationType>, String>> createColumns() {
				return initAppendersBasicColumns();
			}

			@Override
			protected MultivalueContainerDetailsPanel<AppenderConfigurationType> getMultivalueContainerDetailsPanel(
					ListItem<ContainerValueWrapper<AppenderConfigurationType>> item) {
				return LoggingConfigurationTabPanel.this.getAppendersMultivalueContainerDetailsPanel(item);
			}
		};
		
		add(appendersMultivalueContainerListPanel);
		
		ContainerWrapper<AuditingConfigurationType> auditWrapper = containerValueWrapper.findContainerWrapper(new ItemPath(getModel().getObject().getPath(), LoggingConfigurationType.F_AUDITING));
    	IModel<ContainerWrapper<AuditingConfigurationType>> auditModel = new AbstractReadOnlyModel<ContainerWrapper<AuditingConfigurationType>>() {

    		private static final long serialVersionUID = 1L;

			@Override
    		public ContainerWrapper<AuditingConfigurationType> getObject() {
    			return auditWrapper;
    		}

    	};
		PrismContainerPanel<AuditingConfigurationType> auditPanel = new PrismContainerPanel<AuditingConfigurationType>(ID_AUDITING, auditModel, true, new Form<>("form"), null, getPageBase());
    	add(auditPanel);
    	
    	
		
		setOutputMarkupId(true);
	}
    
    private ItemVisibility getLoggingVisibility(ItemPath pathToCheck) {
    	if(pathToCheck.isSubPathOrEquivalent(new ItemPath(getModelObject().getPath(), LoggingConfigurationType.F_ROOT_LOGGER_APPENDER)) ||
    			pathToCheck.isSubPathOrEquivalent(new ItemPath(getModelObject().getPath(), LoggingConfigurationType.F_ROOT_LOGGER_LEVEL))){
			return ItemVisibility.AUTO;
		}
		return ItemVisibility.HIDDEN;
	}

	private List<ContainerValueWrapper<AppenderConfigurationType>> getAppenders() {
//    	LOGGER.info("XXXXXXXXXXXXX values: " + appenderModel.getObject().getValues());
    	return appenderModel.getObject().getValues();
    }
    
    private List<ContainerValueWrapper<ClassLoggerConfigurationType>> getLoggers() {
//    	LOGGER.info("XXXXXXXXXXXXX values: " + appenderModel.getObject().getValues());
    	return loggerModel.getObject().getValues();
    }
    
    private List<IColumn<ContainerValueWrapper<ClassLoggerConfigurationType>, String>> initLoggersBasicColumns() {
    	List<IColumn<ContainerValueWrapper<ClassLoggerConfigurationType>, String>> columns = new ArrayList<>();
    	
    	columns.add(new CheckBoxHeaderColumn<>());

		columns.add(new IconColumn<ContainerValueWrapper<ClassLoggerConfigurationType>>(Model.of("")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> createIconModel(IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
				return new AbstractReadOnlyModel<String>() {

					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return WebComponentUtil.createDefaultBlackIcon(SystemConfigurationType.COMPLEX_TYPE);
					}
				};
			}

		});
		
		columns.add(new EditableLinkColumnForContainerWrapper<ContainerValueWrapper<ClassLoggerConfigurationType>>(createStringResource("LoggingConfigurationTabPanel.loggers.package")){
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> createLinkModel(IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
            	ClassLoggerConfigurationType logger = rowModel.getObject().getContainerValue().getValue();
            	String loggerPackage = logger.getPackage();
            	return Model.of(loggerPackage);
            }
            
            @Override
            protected Component createInputPanel(String componentId,
            		IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
            	
            	List<String> allLoggers = new ArrayList<String>();
            	IModel<List<StandardLoggerType>> standardLoggers = WebComponentUtil.createReadonlyModelFromEnum(StandardLoggerType.class);
            	IModel<List<LoggingComponentType>> componentLoggers = WebComponentUtil.createReadonlyModelFromEnum(LoggingComponentType.class);
            	
            	for(StandardLoggerType standardLogger : standardLoggers.getObject()) {
            		allLoggers.add(standardLogger.name());
            	}
            	for(LoggingComponentType componentLogger : componentLoggers.getObject()) {
            		allLoggers.add(componentLogger.name());
            	}
            	
            	PropertyWrapper<String> packageWrapper = (PropertyWrapper<String>)rowModel.getObject().findPropertyWrapper(new ItemPath(rowModel.getObject().getPath(), ClassLoggerConfigurationType.F_PACKAGE));
            	AutoCompleteTextPanel<String> autoCompleteTextPanel = new AutoCompleteTextPanel<String>(componentId, new PropertyModel<String>(packageWrapper.getValues(), "[0].value.value") {
            		
            		@Override
					public void setObject(String name) {
            			
            			String packageValue = name;
            			for (StandardLoggerType standardLogger : StandardLoggerType.values()) {
            		        if (name.equals(standardLogger.name())) {
            		        	packageValue = standardLogger.getValue();
            		        }
            		    }
            			
            			for (LoggingComponentType componentLogger : LoggingComponentType.values()) {
            		        if (name.equals(componentLogger.name())) {
            		        	packageValue = componentLogger.value();
            		        }
            		    }
						super.setObject(packageValue);
					}
            	
            	}
            	, String.class, new AbstractAutoCompleteRenderer() {

					private static final long serialVersionUID = 1L;

					@Override
					protected String getTextValue(Object name) {
						
						for (StandardLoggerType standardLogger : StandardLoggerType.values()) {
            		        if (name.equals(standardLogger.name())) {
            		        	return standardLogger.getValue();
            		        }
            		    }
            			
            			for (LoggingComponentType componentLogger : LoggingComponentType.values()) {
            		        if (name.equals(componentLogger.name())) {
            		        	return componentLogger.value();
            		        }
            		    }
            			
						return name.toString();
					}

					@Override
					protected void renderChoice(Object name, Response response, String criteria) {
						
						String displayName = name.toString();
						for (StandardLoggerType standardLogger : StandardLoggerType.values()) {
            		        if (name.equals(standardLogger.name())) {
            		        	displayName = createStringResource("StandardLoggerType." + name).getString();
            		        }
            		    }
            			
            			for (LoggingComponentType componentLogger : LoggingComponentType.values()) {
            		        if (name.equals(componentLogger.name())) {
            		        	displayName = createStringResource("LoggingComponentType." + name).getString();
            		        }
            		    }
            			
            			displayName = Strings.escapeMarkup(displayName).toString();
						response.write(displayName);
					}
					
            	}) {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public Iterator<String> getIterator(String input) {
						return allLoggers.iterator();
					}
					
				};
				return autoCompleteTextPanel;
            }

            @Override
            public void onClick(AjaxRequestTarget target, IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
            	loggerEditPerformed(target, rowModel, null);
            }
        });
		
		columns.add(new EditableTextColumnForContainerWrapper<ContainerValueWrapper<ClassLoggerConfigurationType>, String>(createStringResource("LoggingConfigurationTabPanel.loggers.level")){
            private static final long serialVersionUID = 1L;

			@Override
			protected Component createInputPanel(String componentId,
					IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
				ClassLoggerConfigurationType logger = rowModel.getObject().getContainerValue().getValue();
				PropertyWrapper<LoggingLevelType> levelWrapper = (PropertyWrapper<LoggingLevelType>)rowModel.getObject().findPropertyWrapper(new ItemPath(rowModel.getObject().getPath(), ClassLoggerConfigurationType.F_LEVEL));
				DropDownChoicePanel<LoggingLevelType> dropDownChoicePanel = new DropDownChoicePanel<>(componentId,
                        new PropertyModel<LoggingLevelType>(levelWrapper.getValues(), "[0].value.value"),
                        WebComponentUtil.createReadonlyModelFromEnum(LoggingLevelType.class));
				return dropDownChoicePanel;
			}

			@Override
			protected Component staticPanel(String componentId,
					IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
				ClassLoggerConfigurationType logger = rowModel.getObject().getContainerValue().getValue();
				return new Label(componentId, WebComponentUtil.createLocalizedModelForEnum(logger.getLevel(), getPageBase()));
			}
        });
		
		columns.add(new EditableTextColumnForContainerWrapper<ContainerValueWrapper<ClassLoggerConfigurationType>, String>(createStringResource("LoggingConfigurationTabPanel.loggers.appender")){
            private static final long serialVersionUID = 1L;

			@Override
			protected Component createInputPanel(String componentId,
					IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
				ClassLoggerConfigurationType logger = rowModel.getObject().getContainerValue().getValue();
				
				IModel<Map<String, String>> options = new Model(null);
                Map<String, String> optionsMap = new HashMap<>();
                optionsMap.put("nonSelectedText", createStringResource("LoggingConfigPanel.appenders.Inherit").getString());
                options.setObject(optionsMap);
                PropertyWrapper<String> appenderWrapper = (PropertyWrapper<String>)rowModel.getObject().findPropertyWrapper(new ItemPath(rowModel.getObject().getPath(), ClassLoggerConfigurationType.F_APPENDER));
                
                List<String> list = new ArrayList<>();

                for (ValueWrapper appender : appenderWrapper.getValues()) {
                    list.add(appender.getValue().getRealValue());
                }
                
                LOGGER.info("XXXXXXXXXXXXXXXXXX appenders: " + appenderWrapper.getValues().get(0).getValue());

                ListMultipleChoicePanel panel = new ListMultipleChoicePanel<>(componentId,
                    new IModel<List<String>>(){

						private static final long serialVersionUID = 1L;
                	
						@Override
						public void setObject(List<String> object) {
							logger.getAppender().clear();
							logger.getAppender().addAll(object);
						}

						@Override
						public void detach() {
						}

						@Override
						public List<String> getObject() {
							return list;
						}
                	},
                    new AbstractReadOnlyModel<List<String>>() {

                    	private static final long serialVersionUID = 1L;
                        @Override
                        public List<String> getObject() {
                            List<String> list = new ArrayList<>();

                            for (AppenderConfigurationType appender : getModelObject().getValues().get(0).getContainerValue().getValue().getAppender()) {
                                list.add(appender.getName());
                            }

                            return list;
                        }
                	},
                    StringChoiceRenderer.simple(), options);

                return panel;
			}

			@Override
			protected Component staticPanel(String componentId,
					IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel) {
				ClassLoggerConfigurationType logger = rowModel.getObject().getContainerValue().getValue();
				
				IModel<String> model = Model.of("");
				if(logger.getAppender().isEmpty()){
					model = createStringResource("LoggingConfigPanel.appenders.Inherit");
				} else{
					model = new LoadableModel<String>() {
                	
						private static final long serialVersionUID = 1L;

						@Override
						protected String load() {
							StringBuilder builder = new StringBuilder();
							for (String appender : logger.getAppender()) {
								if (logger.getAppender().indexOf(appender) != 0) {
									builder.append(", ");
								}
								builder.append(appender);
							}

							return builder.toString();
						}
					};
				}
			
				return new Label(componentId, model);
			}
        });
		
		List<InlineMenuItem> menuActionsList = getLoggersMultivalueContainerListPanel().getDefaultMenuActions();
		columns.add(new InlineMenuButtonColumn<>(menuActionsList, menuActionsList.size(), getPageBase()));
		
        return columns;
	}
    
    private void loggerEditPerformed(AjaxRequestTarget target, IModel<ContainerValueWrapper<ClassLoggerConfigurationType>> rowModel,
    		List<ContainerValueWrapper<ClassLoggerConfigurationType>> listItems) {
    	if(rowModel != null) {
    		ContainerValueWrapper<ClassLoggerConfigurationType> logger = rowModel.getObject();
        	logger.setSelected(true);
    	} else {
    		for(ContainerValueWrapper<ClassLoggerConfigurationType> logger : listItems) {
    			logger.setSelected(true);
    		}
    	}
        target.add(getLoggersMultivalueContainerListPanel());
    }
    
    
    protected void newAppendersClickPerformed(AjaxRequestTarget target) {
        PrismContainerValue<AppenderConfigurationType> newObjectPolicy = appenderModel.getObject().getItem().createNewValue();
        ContainerValueWrapper<AppenderConfigurationType> newAppenderContainerWrapper = getAppendersMultivalueContainerListPanel().createNewItemContainerValueWrapper(newObjectPolicy, appenderModel);
        newAppenderContainerWrapper.setShowEmpty(true, false);
        newAppenderContainerWrapper.computeStripes();
        getAppendersMultivalueContainerListPanel().itemDetailsPerformed(target, Arrays.asList(newAppenderContainerWrapper));
	}
    
    private MultivalueContainerDetailsPanel<AppenderConfigurationType> getAppendersMultivalueContainerDetailsPanel(
			ListItem<ContainerValueWrapper<AppenderConfigurationType>> item) {
    	MultivalueContainerDetailsPanel<AppenderConfigurationType> detailsPanel = new  MultivalueContainerDetailsPanel<AppenderConfigurationType>(MultivalueContainerListPanelWithDetailsPanel.ID_ITEM_DETAILS, item.getModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected DisplayNamePanel<AppenderConfigurationType> createDisplayNamePanel(String displayNamePanelId) {
				IModel<AppenderConfigurationType> displayNameModel = new AbstractReadOnlyModel<AppenderConfigurationType>() {

		    		private static final long serialVersionUID = 1L;

					@Override
		    		public AppenderConfigurationType getObject() {
		    			return item.getModelObject().getContainerValue().getValue();
		    		}

		    	};
				return new DisplayNamePanel<AppenderConfigurationType>(displayNamePanelId, displayNameModel);
			}
			
			@Override
			protected ItemVisibility getBasicTabVisibity(ItemWrapper itemWrapper, ItemPath parentPath) {
					LOGGER.info("XXXXXXXXXXXXX path: " + itemWrapper.getPath());
				return ItemVisibility.VISIBLE;
			}
			
			@Override
			protected  Fragment getSpecificContainers(String contentAreaId) {
				Fragment specificContainers = new Fragment(contentAreaId, ID_SPECIFIC_CONTAINERS_FRAGMENT, LoggingConfigurationTabPanel.this);
//				
//				Form form = new Form<>("form");
//		    	ItemPath itemPath = getModelObject().getPath();
//		    	IModel<ContainerValueWrapper<AppenderConfigurationType>> model = getModel();
//		    	model.getObject().setShowEmpty(true, true);
//		    	model.getObject().getContainer().setShowOnTopLevel(true);
//		    	ContainerValuePanel panel;
//		    	if(item.getModelObject().getContainerValue().getValue() instanceof FileAppenderConfigurationType) {
//		    		
//		    		LOGGER.info("XXXXXXXXXXXXX item: " + item.getModelObject().get);
//		    		LOGGER.info("XXXXXXXXXXXXX getFilePattern: " + ((FileAppenderConfigurationType)item.getModelObject().getContainerValue().getValue()).getFilePattern());
//		    		
//		    		FileAppenderConfigurationType appender = (FileAppenderConfigurationType) item.getModelObject().getContainerValue().getValue();
//		    		ContainerWrapperFactory cwf = new ContainerWrapperFactory(getPageBase());
//		    		Task task = LoggingConfigurationTabPanel.this.getPageBase().createSimpleTask(TASK_CREATE_APPENDER);
//		    		ContainerWrapper<FileAppenderConfigurationType> wrapper = cwf.createContainerWrapper((PrismContainer<FileAppenderConfigurationType>)appender.asPrismContainerValue().getContainer(), item.getModelObject().getObjectStatus(), 
//		    				item.getModelObject().getObjectStatus(), item.getModelObject().getPath(), task);
//		    		ContainerValueWrapper<FileAppenderConfigurationType> value = cwf.createContainerValueWrapper(wrapper, (PrismContainerValue<FileAppenderConfigurationType>)appender.asPrismContainerValue(), item.getModelObject().getObjectStatus(), item.getModelObject().getStatus(), item.getModelObject().getPath(), task);
//		    		
//		    		IModel<ContainerValueWrapper<FileAppenderConfigurationType>> valueModel = new LoadableModel<ContainerValueWrapper<FileAppenderConfigurationType>>(false) {
//
//		    			private static final long serialVersionUID = 1L;
//
//		    			@Override
//		    			protected ContainerValueWrapper<FileAppenderConfigurationType> load() {
//		    				return value;
//		    			}
//		    		};
//		    		
//		    		panel = new ContainerValuePanel<FileAppenderConfigurationType>(ID_SPECIFIC_CONTAINER, valueModel, true, form,
//		    				itemWrapper -> getBasicTabVisibity(itemWrapper, itemPath), getPageBase());
////		    	} else if(item.getModelObject().getContainerValue().getValue() instanceof SyslogAppenderConfigurationType) {
////		    		panel = new ContainerValuePanel<SyslogAppenderConfigurationType>(ID_SPECIFIC_CONTAINER, model, true, form,
////							null, getPageBase());
//		    	} else {
//		    		panel = new ContainerValuePanel<AppenderConfigurationType>(ID_SPECIFIC_CONTAINER, getModel(), true, form,
//		    				itemWrapper -> getBasicTabVisibity(itemWrapper, itemPath), getPageBase());
//		    	}
//		    	specificContainers.add(panel);
				return specificContainers;
			}
			
		};
		return detailsPanel;
	}
    
	private MultivalueContainerListPanelWithDetailsPanel<AppenderConfigurationType> getAppendersMultivalueContainerListPanel(){
		return ((MultivalueContainerListPanelWithDetailsPanel<AppenderConfigurationType>)get(ID_APPENDERS));
	}
	
	private MultivalueContainerListPanel<ClassLoggerConfigurationType> getLoggersMultivalueContainerListPanel(){
		return ((MultivalueContainerListPanel<ClassLoggerConfigurationType>)get(ID_LOGGERS));
	}
    
    private ObjectQuery createAppendersQuery() {
    	TypeFilter filter = TypeFilter.createType(AppenderConfigurationType.COMPLEX_TYPE, new AllFilter());
    	return ObjectQuery.createObjectQuery(filter);
    }
    
    private void initAppenderPaging() {
    	getPageBase().getSessionStorage().getLoggingConfigurationTabAppenderTableStorage().setPaging(ObjectPaging.createPaging(0, (int) ((PageBase)getPage()).getItemsPerPage(UserProfileStorage.TableId.LOGGING_TAB_APPENDER_TABLE)));
    }
    
    private void initLoggerPaging() {
    	getPageBase().getSessionStorage().getLoggingConfigurationTabLoggerTableStorage().setPaging(ObjectPaging.createPaging(0, (int) ((PageBase)getPage()).getItemsPerPage(UserProfileStorage.TableId.LOGGING_TAB_APPENDER_TABLE)));
    }
    
    private List<IColumn<ContainerValueWrapper<AppenderConfigurationType>, String>> initAppendersBasicColumns() {
		List<IColumn<ContainerValueWrapper<AppenderConfigurationType>, String>> columns = new ArrayList<>();

		columns.add(new CheckBoxHeaderColumn<>());

		columns.add(new IconColumn<ContainerValueWrapper<AppenderConfigurationType>>(Model.of("")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> createIconModel(IModel<ContainerValueWrapper<AppenderConfigurationType>> rowModel) {
				return new AbstractReadOnlyModel<String>() {

					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return WebComponentUtil.createDefaultBlackIcon(SystemConfigurationType.COMPLEX_TYPE);
					}
				};
			}

		});
		
		columns.add(new LinkColumn<ContainerValueWrapper<AppenderConfigurationType>>(createStringResource("PolicyRulesPanel.nameColumn")){
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> createLinkModel(IModel<ContainerValueWrapper<AppenderConfigurationType>> rowModel) {
            	AppenderConfigurationType appender = rowModel.getObject().getContainerValue().getValue();
            	String name = appender.getName();
           		if (StringUtils.isBlank(name)) {
            		return createStringResource("AssignmentPanel.noName");
            	}
            	return Model.of(name);
            }

            @Override
            public void onClick(AjaxRequestTarget target, IModel<ContainerValueWrapper<AppenderConfigurationType>> rowModel) {
            	getAppendersMultivalueContainerListPanel().itemDetailsPerformed(target, rowModel);
            }
        });
		
		List<InlineMenuItem> menuActionsList = getAppendersMultivalueContainerListPanel().getDefaultMenuActions();
		columns.add(new InlineMenuButtonColumn<>(menuActionsList, menuActionsList.size(), getPageBase()));
		
        return columns;
	}
    
    private void addAjaxFormComponentUpdatingBehavior(FormComponent component){
        component.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {}
        });
    }
}

