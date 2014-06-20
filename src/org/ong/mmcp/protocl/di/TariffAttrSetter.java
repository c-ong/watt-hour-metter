package org.ong.mmcp.protocl.di;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface TariffAttrSetter<T> extends AttributeSetter {
	T setTariff(Tariff tariff);
}