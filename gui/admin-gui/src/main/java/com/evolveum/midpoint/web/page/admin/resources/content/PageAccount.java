/*
 * Copyright (c) 2010-2017 Evolveum
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
package com.evolveum.midpoint.web.page.admin.resources.content;

import com.evolveum.midpoint.gui.api.component.tabs.PanelTab;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.AuthorizationAction;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.AjaxTabbedPanel;
import com.evolveum.midpoint.web.component.prism.ContainerStatus;
import com.evolveum.midpoint.web.component.prism.ObjectWrapper;
import com.evolveum.midpoint.web.component.util.ObjectWrapperUtil;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.admin.resources.PageAdminResources;
import com.evolveum.midpoint.web.page.admin.resources.PageResources;
import com.evolveum.midpoint.web.page.admin.resources.ShadowDetailsTabPanel;
import com.evolveum.midpoint.web.page.admin.resources.ShadowSummaryPanel;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lazyman
 */
@PageDescriptor(url = "/admin/resources/account", encoder = OnePageParameterEncoder.class, action = {
		@AuthorizationAction(actionUri = PageAdminResources.AUTH_RESOURCE_ALL,
				label = PageAdminResources.AUTH_RESOURCE_ALL_LABEL,
				description = PageAdminResources.AUTH_RESOURCE_ALL_DESCRIPTION),
		@AuthorizationAction(actionUri = AuthorizationConstants.AUTZ_UI_RESOURCES_ACCOUNT_URL,
				label = "PageAccount.auth.resourcesAccount.label",
				description = "PageAccount.auth.resourcesAccount.description")})
public class PageAccount extends PageAdminResources {

	private static final Trace LOGGER = TraceManager.getTrace(PageAccount.class);
	private static final String DOT_CLASS = PageAccount.class.getName() + ".";
	private static final String OPERATION_LOAD_ACCOUNT = DOT_CLASS + "loadAccount";
	private static final String OPERATION_SAVE_ACCOUNT = DOT_CLASS + "saveAccount";

	private static final String ID_MAIN_FORM = "mainForm";
	private static final String ID_SUMMARY = "summary";
	private static final String ID_TAB_PANEL = "tabPanel";
	private static final String ID_PROTECTED_MESSAGE = "protectedMessage";
	private static final String ID_SAVE = "save";
	private static final String ID_BACK = "back";

	public static final String PARAMETER_SELECTED_TAB = "tab";

	private LoadableModel<ObjectWrapper<ShadowType>> accountModel;

	public PageAccount(final PageParameters parameters) {
		accountModel = new LoadableModel<ObjectWrapper<ShadowType>>(false) {

			@Override
			protected ObjectWrapper<ShadowType> load() {
				return loadAccount(parameters);
			}
		};
		initLayout();
	}

	private ObjectWrapper<ShadowType> loadAccount(PageParameters parameters) {
		Task task = createSimpleTask(OPERATION_LOAD_ACCOUNT);
		OperationResult result = task.getResult();

		Collection<SelectorOptions<GetOperationOptions>> options = getOperationOptionsBuilder()
				.item(ShadowType.F_RESOURCE).resolve()
                .build();
		StringValue oid = parameters != null ? parameters.get(OnePageParameterEncoder.PARAMETER) : null;
		PrismObject<ShadowType> account = WebModelServiceUtils.loadObject(ShadowType.class, oid.toString(), options,
				PageAccount.this, task, result);

		if (account == null) {
			getSession().error(getString("pageAccount.message.cantEditAccount"));
			showResult(result);
			throw new RestartResponseException(PageResources.class);
		}

		ObjectWrapper wrapper = ObjectWrapperUtil.createObjectWrapper(null, null, account, ContainerStatus.MODIFYING, task, this);
		OperationResultType fetchResult = account.getPropertyRealValue(ShadowType.F_FETCH_RESULT, OperationResultType.class);
		try {
			wrapper.setFetchResult(OperationResult.createOperationResult(fetchResult));
		} catch (SchemaException e) {
			throw new SystemException(e.getMessage(), e);
		}
		wrapper.setShowEmpty(false);
		return wrapper;
	}

	private void initLayout() {
		add(new ShadowSummaryPanel(ID_SUMMARY, new PropertyModel<>(accountModel, "object"), this));


		WebMarkupContainer protectedMessage = new WebMarkupContainer(ID_PROTECTED_MESSAGE);
		protectedMessage.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				ObjectWrapper wrapper = accountModel.getObject();
				return wrapper.isProtectedAccount();
			}
		});
		add(protectedMessage);

		com.evolveum.midpoint.web.component.form.Form mainForm = new com.evolveum.midpoint.web.component.form.Form(ID_MAIN_FORM);
		mainForm.setMultiPart(true);
		add(mainForm);

		mainForm.add(createTabsPanel(mainForm));

		initButtons(mainForm);
	}


	private AjaxTabbedPanel<ITab> createTabsPanel(com.evolveum.midpoint.web.component.form.Form<ObjectWrapper<ShadowType>> form) {
		List<ITab> tabs = new ArrayList<>();

		tabs.add(new PanelTab(createStringResource("PageAccount.tab.details")) {
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer createPanel(String panelId) {
				return new ShadowDetailsTabPanel(panelId, form, accountModel, PageAccount.this);
			}
		});

		AjaxTabbedPanel<ITab> tabPanel = new AjaxTabbedPanel<ITab>(ID_TAB_PANEL, tabs) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onTabChange(int index) {
				updateBreadcrumbParameters(PARAMETER_SELECTED_TAB, index);
			}
		};
		tabPanel.setOutputMarkupId(true);
		return tabPanel;
	}


	private void initButtons(Form mainForm) {
		AjaxSubmitButton save = new AjaxSubmitButton(ID_SAVE, createStringResource("pageAccount.button.save")) {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				savePerformed(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				target.add(getFeedbackPanel());
			}
		};
		save.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				ObjectWrapper wrapper = accountModel.getObject();
				return !wrapper.isProtectedAccount();
			}
		});
		mainForm.add(save);

		AjaxButton back = new AjaxButton(ID_BACK, createStringResource("pageAccount.button.back")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				cancelPerformed(target);
			}
		};
		mainForm.add(back);
	}

	@Override
	protected IModel<String> createPageTitleModel() {
		return new LoadableModel<String>(false) {

			@Override
			protected String load() {
				PrismObject<ShadowType> account = accountModel.getObject().getObject();
				String accName = WebComponentUtil.getName(account);

				ResourceType resource = account.asObjectable().getResource();
				String name = WebComponentUtil.getName(resource);

				return createStringResourceStatic(PageAccount.this, "PageAccount.title", accName, name).getString();
			}
		};
	}

	private void savePerformed(AjaxRequestTarget target) {
		LOGGER.debug("Saving account changes.");

		OperationResult result = new OperationResult(OPERATION_SAVE_ACCOUNT);
		try {
			WebComponentUtil.revive(accountModel, getPrismContext());
			ObjectWrapper wrapper = accountModel.getObject();
			ObjectDelta<ShadowType> delta = wrapper.getObjectDelta();
			if (delta == null) {
				return;
			}
			if (delta.getPrismContext() == null) {
				getPrismContext().adopt(delta);
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Account delta computed from form:\n{}", new Object[]{delta.debugDump(3)});
			}

			if (delta.isEmpty()) {
				return;
			}
			WebComponentUtil.encryptCredentials(delta, true, getMidpointApplication());

			Task task = createSimpleTask(OPERATION_SAVE_ACCOUNT);
			Collection<ObjectDelta<? extends ObjectType>> deltas = new ArrayList<>();
			deltas.add(delta);

			getModelService().executeChanges(deltas, null, task, result);
			result.recomputeStatus();
		} catch (Exception ex) {
			result.recordFatalError("Couldn't save account.", ex);
			LoggingUtils.logUnexpectedException(LOGGER, "Couldn't save account", ex);
		}

		if (!result.isSuccess()) {
			showResult(result);
			target.add(getFeedbackPanel());
		} else {
			showResult(result);

			redirectBack();
		}
	}

	private void cancelPerformed(AjaxRequestTarget target) {
		redirectBack();
	}
}
