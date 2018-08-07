/*
 * Copyright (c) 2010-2018 Evolveum
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

package com.evolveum.midpoint.provisioning.impl.errorhandling;

import java.util.ArrayList;
import java.util.Collection;

import com.evolveum.midpoint.provisioning.impl.ConstraintsChecker;
import com.evolveum.midpoint.provisioning.impl.ProvisioningContext;
import com.evolveum.midpoint.provisioning.impl.ProvisioningOperationState;
import com.evolveum.midpoint.provisioning.impl.ResourceManager;
import com.evolveum.midpoint.schema.util.ShadowUtil;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.provisioning.api.ProvisioningOperationOptions;
import com.evolveum.midpoint.provisioning.api.ResourceOperationDescription;
import com.evolveum.midpoint.provisioning.ucf.api.GenericFrameworkException;
import com.evolveum.midpoint.provisioning.util.ProvisioningUtil;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.DeltaConvertor;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.result.AsynchronousOperationResult;
import com.evolveum.midpoint.schema.result.AsynchronousOperationReturnValue;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AvailabilityStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FailedOperationTypeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PendingOperationExecutionStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

@Component
public class CommunicationExceptionHandler extends ErrorHandler {
	
	private static final String OPERATION_HANDLE_GET_ERROR = CommunicationExceptionHandler.class.getName() + ".handleGetError";
	private static final String OPERATION_HANDLE_ADD_ERROR = CommunicationExceptionHandler.class.getName() + ".handleAddError";
	private static final String OPERATION_HANDLE_MODIFY_ERROR = CommunicationExceptionHandler.class.getName() + ".handleModifyError";
	private static final String OPERATION_HANDLE_DELETE_ERROR = CommunicationExceptionHandler.class.getName() + ".handleDeleteError";
	
	private static final Trace LOGGER = TraceManager.getTrace(CommunicationExceptionHandler.class);

	@Override
	public PrismObject<ShadowType> handleGetError(ProvisioningContext ctx,
			PrismObject<ShadowType> repositoryShadow, GetOperationOptions rootOptions, Exception cause,
			Task task, OperationResult parentResult) throws SchemaException, GenericFrameworkException,
			CommunicationException, ObjectNotFoundException, ObjectAlreadyExistsException,
			ConfigurationException, SecurityViolationException, ExpressionEvaluationException {
		
		ResourceType resource = ctx.getResource();
		if (!ProvisioningUtil.isDoDiscovery(resource, rootOptions)) {
			throwException(cause, null, parentResult);
		}
		
		OperationResult result = parentResult.createSubresult(OPERATION_HANDLE_GET_ERROR);
		result.addParam("exception", cause.getMessage());
		
		markResourceDown(resource, result);
		
		// nothing to do, just return the shadow from the repo and set fetch
		// result..
		for (OperationResult subRes : parentResult.getSubresults()) {
			subRes.muteError();
		}
		result.recordPartialError("Could not get "+repositoryShadow+" from the resource "
				+ resource + ", because resource is unreachable. Returning shadow from the repository");
		repositoryShadow.asObjectable().setFetchResult(result.createOperationResultType());
//					operationResult.recordSuccess();
//					operationResult.computeStatus();
		return repositoryShadow;
	}
	
	@Override
	protected void throwException(Exception cause, ProvisioningOperationState<? extends AsynchronousOperationResult> opState, OperationResult result) throws CommunicationException {
		recordCompletionError(cause, opState, result);
		if (cause instanceof CommunicationException) {
			throw (CommunicationException)cause;
		} else {
			throw new CommunicationException(cause.getMessage(), cause);
		}
	}

	@Override
	public OperationResultStatus handleAddError(ProvisioningContext ctx,
			PrismObject<ShadowType> shadowToAdd,
			ProvisioningOperationOptions options,
			ProvisioningOperationState<AsynchronousOperationReturnValue<PrismObject<ShadowType>>> opState,
			Exception cause,
			OperationResult failedOperationResult,
			Task task,
			OperationResult parentResult)
				throws SchemaException, GenericFrameworkException, CommunicationException,
				ObjectNotFoundException, ObjectAlreadyExistsException, ConfigurationException,
				SecurityViolationException, ExpressionEvaluationException {
		
		OperationResult result = parentResult.createSubresult(OPERATION_HANDLE_ADD_ERROR);
		result.addParam("exception", cause.getMessage());
		ResourceType resource = ctx.getResource();
		markResourceDown(resource, result);
		handleRetriesAndAttempts(ctx, opState, options, cause, result);
		return postponeAdd(ctx, shadowToAdd, opState, failedOperationResult, result);
	}

	@Override
	public OperationResultStatus handleModifyError(ProvisioningContext ctx, PrismObject<ShadowType> repoShadow,
			Collection<? extends ItemDelta> modifications, ProvisioningOperationOptions options,
			ProvisioningOperationState<AsynchronousOperationReturnValue<Collection<PropertyDelta<PrismPropertyValue>>>> opState,
			Exception cause, OperationResult failedOperationResult, Task task, OperationResult parentResult)
			throws SchemaException, GenericFrameworkException, CommunicationException,
			ObjectNotFoundException, ObjectAlreadyExistsException, ConfigurationException,
			SecurityViolationException, ExpressionEvaluationException {
		
		OperationResult result = parentResult.createSubresult(OPERATION_HANDLE_MODIFY_ERROR);
		result.addParam("exception", cause.getMessage());
		ResourceType resource = ctx.getResource();
		markResourceDown(resource, result);
		handleRetriesAndAttempts(ctx, opState, options, cause, result);
		return postponeModify(ctx, repoShadow, modifications, opState, failedOperationResult, result);
	}

	@Override
	public OperationResultStatus handleDeleteError(ProvisioningContext ctx, PrismObject<ShadowType> repoShadow,
			ProvisioningOperationOptions options,
			ProvisioningOperationState<AsynchronousOperationResult> opState, Exception cause,
			OperationResult failedOperationResult, Task task, OperationResult parentResult)
			throws SchemaException, GenericFrameworkException, CommunicationException,
			ObjectNotFoundException, ObjectAlreadyExistsException, ConfigurationException,
			SecurityViolationException, ExpressionEvaluationException {
		OperationResult result = parentResult.createSubresult(OPERATION_HANDLE_DELETE_ERROR);
		result.addParam("exception", cause.getMessage());
		ResourceType resource = ctx.getResource();
		markResourceDown(resource, result);
		handleRetriesAndAttempts(ctx, opState, options, cause, result);
		return postponeDelete(ctx, repoShadow, opState, failedOperationResult, result);
	}

	private void handleRetriesAndAttempts(ProvisioningContext ctx, ProvisioningOperationState<? extends AsynchronousOperationResult> opState, ProvisioningOperationOptions options, Exception cause, OperationResult result) throws CommunicationException, ObjectNotFoundException, SchemaException, ConfigurationException, ExpressionEvaluationException {
		ResourceType resource = ctx.getResource();
		if (!isOperationRetryEnabled(resource) || !isCompletePostponedOperations(options)) {
			LOGGER.trace("Operation retry turned off for {}", resource);
			throwException(cause, opState, result);
		}
		
		int maxRetryAttempts = ProvisioningUtil.getMaxRetryAttempts(ctx);
		Integer attemptNumber = opState.getAttemptNumber();
		if (attemptNumber == null) {
			attemptNumber = 1;
		}
		if (attemptNumber >= maxRetryAttempts) {
			LOGGER.debug("Maximum nuber of retry attempts ({}) reached for operation on {}", attemptNumber, ctx.getResource() );
			throwException(cause, opState, result);
		}
	}

//
//	@Override
//	public <T extends ShadowType> T handleError(T shadow, FailedOperation op, Exception ex, 
//			boolean doDiscovery, boolean compensate, 
//			Task task, OperationResult parentResult) throws SchemaException, GenericFrameworkException, CommunicationException,
//			ObjectNotFoundException, ObjectAlreadyExistsException, ConfigurationException {
//
//		if (!doDiscovery) {
//			parentResult.recordFatalError(ex);
//			if (ex instanceof CommunicationException) {
//				throw (CommunicationException)ex;
//			} else {
//				throw new CommunicationException(ex.getMessage(), ex);
//			}
//		}
//		
//		Validate.notNull(shadow, "Shadow must not be null.");
//		
//		OperationResult operationResult = parentResult.createSubresult("com.evolveum.midpoint.provisioning.consistency.impl.CommunicationExceptionHandler.handleError." + op.name());
//		operationResult.addParam("shadow", shadow);
//		operationResult.addArbitraryObjectAsParam("currentOperation", op);
//		operationResult.addParam("exception", ex.getMessage());
//
//		// first modify last availability status in the resource, so by others
//		// operations, we can know that it is down
//		resourceManager.modifyResourceAvailabilityStatus(shadow.getResource().asPrismObject(), 
//				AvailabilityStatusType.DOWN, operationResult);
//		
//		if ((!isPostpone(shadow.getResource()) || !compensate) && !FailedOperation.GET.equals(op)) {
//			
//		}
//		
////		Task task = null; 
//		ObjectDelta delta = null;
//		ResourceOperationDescription operationDescription = null;
//		switch (op) {
//		case ADD:
//			// if it is first time, just store the whole account to the repo
//			LOGGER.trace("Postponing ADD operation for {}", ObjectTypeUtil.toShortString(shadow));
//			ResourceType resource = shadow.getResource();
//			if (shadow.getFailedOperationType() == null) {
////				ResourceType resource = shadow.getResource();
//				if (shadow.getName() == null) {
//					shadow.setName(new PolyStringType(ShadowUtil.determineShadowName(shadow.asPrismObject())));
//				}
//				if (shadow.getResourceRef() == null || shadow.getResourceRef().getOid() == null){
//					if (resource != null){
//					shadow.getResourceRef().setOid(shadow.getResource().getOid());
//					}
//				}
//				
//				if (shadow.getResourceRef() != null && resource != null){
//					shadow.setResource(null);
//				}
//				shadow.setAttemptNumber(getAttemptNumber(shadow));
//				shadow.setFailedOperationType(FailedOperationTypeType.ADD);
//				ConstraintsChecker.onShadowAddOperation(shadow);
//				// Unlike addObject calls during normal provisioning, here we preserve all activation information, including e.g. administrativeStatus.
//				// It is needed for shadow creation during error recovery.
//				String oid = repositoryService.addObject(shadow.asPrismObject(), null, operationResult);
//				shadow.setOid(oid);
//				if (LOGGER.isTraceEnabled()) {
//					LOGGER.trace("Stored new shadow for unfinished operation:\n{}", shadow.asPrismObject().debugDump(1));
//				}
//			
//				// if it is seccond time ,just increade the attempt number
//			} else {
//				if (FailedOperationTypeType.ADD == shadow.getFailedOperationType()) {
//
//					Collection<? extends ItemDelta> attemptdelta = createAttemptModification(shadow, null);
//					ConstraintsChecker.onShadowModifyOperation(attemptdelta);
//					repositoryService.modifyObject(ShadowType.class, shadow.getOid(), attemptdelta,
//							operationResult);
//			
//				}
//
//			}
//			// if the shadow was successfully stored in the repo, just mute the
//			// error
//			for (OperationResult subRes : parentResult.getSubresults()) {
//				subRes.muteError();
//			}
//			operationResult.computeStatus();
//			parentResult
//					.recordHandledError("Could not create object=" +shadow.getName().getOrig()+" on the resource, because "
//									+ ObjectTypeUtil.toShortString(resource)
//									+ " is unreachable at the moment. Shadow is stored in the repository and the resource object will be created when the resource goes online");   // there will be something like ": Add object failed" appended, so the final dot was a bit ugly here
//			
////			task = taskManager.createTaskInstance();
//			delta = ObjectDelta.createAddDelta(shadow.asPrismObject());
//			operationDescription = createOperationDescription(shadow, ex, resource, delta, task, operationResult);
//			changeNotificationDispatcher.notifyInProgress(operationDescription, task, parentResult);
//			return shadow;
//		case MODIFY:
//			if (shadow.getFailedOperationType() == null || shadow.getFailedOperationType() == FailedOperationTypeType.MODIFY) {
//
//				shadow.setFailedOperationType(FailedOperationTypeType.MODIFY);
//				Collection<ItemDelta> modifications = createShadowModification(shadow);
//
//				ConstraintsChecker.onShadowModifyOperation(modifications);
//				getCacheRepositoryService().modifyObject(ShadowType.class, shadow.getOid(), modifications,
//						operationResult);
//				delta = ObjectDelta.createModifyDelta(shadow.getOid(), modifications, shadow.asPrismObject().getCompileTimeClass(), prismContext);
////				operationResult.recordSuccess();
//				// return shadow;
//			} else {
//				if (FailedOperationTypeType.ADD == shadow.getFailedOperationType()) {
//					if (shadow.getObjectChange() != null && shadow.getOid() != null) {
//						Collection<? extends ItemDelta> deltas = DeltaConvertor.toModifications(shadow
//								.getObjectChange().getItemDelta(), shadow.asPrismObject().getDefinition());
//
//						ConstraintsChecker.onShadowModifyOperation(deltas);
//						repositoryService.modifyObject(ShadowType.class, shadow.getOid(), deltas,
//								operationResult);
//						delta = ObjectDelta.createModifyDelta(shadow.getOid(), deltas, shadow.asPrismObject().getCompileTimeClass(), prismContext);
//						// return shadow;
////						operationResult.recordSuccess();
//					}
//				}
//			}
//			for (OperationResult subRes : parentResult.getSubresults()) {
//				subRes.muteError();
//			}
//			operationResult.computeStatus();
//			parentResult
//					.recordHandledError("Could not apply modifications to "+ObjectTypeUtil.toShortString(shadow)+" on the "
//									+ ObjectTypeUtil.toShortString(shadow.getResource())
//									+ ", because resource is unreachable. Modifications will be applied when the resource goes online");
////			task = taskManager.createTaskInstance();
////			
//			operationDescription = createOperationDescription(shadow, ex, shadow.getResource(), delta, task, operationResult);
//			changeNotificationDispatcher.notifyInProgress(operationDescription, task, parentResult);
//			return shadow;
//		case DELETE:
//			shadow.setFailedOperationType(FailedOperationTypeType.DELETE);
//			Collection<ItemDelta> modifications = createShadowModification(shadow);
//
//			ConstraintsChecker.onShadowModifyOperation(modifications);
//			getCacheRepositoryService().modifyObject(ShadowType.class, shadow.getOid(), modifications,
//					operationResult);
//			for (OperationResult subRes : parentResult.getSubresults()) {
//				subRes.muteError();
//			}
//			parentResult
//					.recordHandledError("Could not delete " +ObjectTypeUtil.getShortTypeName(shadow)+ " from the resource "
//									+ ObjectTypeUtil.toShortString(shadow.getResource())
//									+ ", because resource is unreachable. Resource object will be delete when the resource goes online");
////			operationResult.recordSuccess();
//			operationResult.computeStatus();
////			task = taskManager.createTaskInstance();
////			task.setChannel(QNameUtil.qNameToUri(SchemaConstants.CHANGE_CHANNEL_DISCOVERY));
//			delta = ObjectDelta.createDeleteDelta(shadow.asPrismObject().getCompileTimeClass(), shadow.getOid(), prismContext);
//			operationDescription = createOperationDescription(shadow, ex, shadow.getResource(), delta, task, operationResult);
//			changeNotificationDispatcher.notifyInProgress(operationDescription, task, parentResult);
//			return shadow;
//		case GET:
//			// nothing to do, just return the shadow from the repo and set fetch
//			// result..
//			for (OperationResult subRes : parentResult.getSubresults()) {
//				subRes.muteError();
//			}
//			operationResult.recordPartialError("Could not get "+ObjectTypeUtil.toShortString(shadow)+" from the resource "
//					+ ObjectTypeUtil.toShortString(shadow.getResource())
//					+ ", because resource is unreachable. Returning shadow from the repository");
//			shadow.setFetchResult(operationResult.createOperationResultType());
////			operationResult.recordSuccess();
////			operationResult.computeStatus();
//			return shadow;
//		default:
//			throw new CommunicationException(ex);
//		}
//
//	}
//	
//	private <T extends ShadowType> Collection<ItemDelta> createShadowModification(T shadow) throws ObjectNotFoundException, SchemaException {
//		Collection<ItemDelta> modifications = new ArrayList<>();
//
//		PropertyDelta propertyDelta = PropertyDelta.createReplaceDelta(shadow.asPrismObject()
//				.getDefinition(), ShadowType.F_RESULT, shadow.getResult());
//		modifications.add(propertyDelta);
//
//		propertyDelta = PropertyDelta.createReplaceDelta(shadow.asPrismObject().getDefinition(),
//				ShadowType.F_FAILED_OPERATION_TYPE, shadow.getFailedOperationType());
//		modifications.add(propertyDelta);
//		if (shadow.getObjectChange() != null) {
//			propertyDelta = PropertyDelta.createReplaceDelta(shadow.asPrismObject().getDefinition(),
//					ShadowType.F_OBJECT_CHANGE, shadow.getObjectChange());
//			modifications.add(propertyDelta);
//		}
//	
//		modifications = createAttemptModification(shadow, modifications);
//		
//		return modifications;
//	}
	
}