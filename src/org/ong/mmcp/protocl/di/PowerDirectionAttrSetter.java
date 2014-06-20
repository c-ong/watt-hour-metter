package org.ong.mmcp.protocl.di;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface PowerDirectionAttrSetter<T>  extends AttributeSetter {
	T setPowerDirection(PowerDirection direction);
}