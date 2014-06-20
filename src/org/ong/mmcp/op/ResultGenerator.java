package org.ong.mmcp.op;

public interface ResultGenerator<T extends IResult> {
	T generateResult();
}