package io.knowledgeassets.myskills.server.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class MySkillsSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
	private Object filterObject;
	private Object returnObject;
	private Object target;

	public MySkillsSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	/**
	 * Checks whether the given user ID equals the user ID of the authenticated principal.
	 *
	 * @param userId User ID to check against the principal.
	 * @return <code>true</code> if the given user ID equals the user ID of the authenticated principal.
	 */
	public boolean isPrincipalUserId(String userId) {
		Object principal = getPrincipal();
		if (principal instanceof UserIdentity) {
			return ((UserIdentity) principal).getUserId().equals(userId);
		}
		return false;
	}

	public void setFilterObject(Object filterObject) {
		this.filterObject = filterObject;
	}

	public Object getFilterObject() {
		return filterObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	/**
	 * Sets the "this" property for use in expressions. Typically this will be the "this" property of the {@code
	 * JoinPoint} representing the method invocation which is being protected.
	 *
	 * @param target the target object on which the method in is being invoked.
	 */
	void setThis(Object target) {
		this.target = target;
	}

	public Object getThis() {
		return target;
	}
}
