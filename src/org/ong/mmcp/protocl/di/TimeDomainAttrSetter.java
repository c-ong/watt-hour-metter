package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/**
 * 时域属性
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface TimeDomainAttrSetter<T> extends AttributeSetter {
	T setTimeDomain(TimeDomain time_domain);
}