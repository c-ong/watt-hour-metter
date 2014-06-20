package org.ong.mmcp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SlaveStation {
	
	/**
	 * Address field A0~A5: The address field comprises 6 bytes and each byte 
	 * is composed of 2 BCD codes.  It can express the address with 12 decimal 
	 * digits at most. The address can be ID of meter, or asset number, user 
	 * number or device number, and so on which can be decided by users. When 
	 * the length of address is less than 6 bytes, the rest bytes can be filled 
	 * with hexadecimal number AAH. The lower address code is prior while higher 
	 * one is in the latter.  It's a broadcast address when the address value 
	 * is 999999999999H.
	 */
	/*package*/ final Bus bus;
	
	public final String address;
	
	// Execution plan
	/*package*/ Set<Operation> executionPlans;
	
	/*package*/ SlaveStation(Bus bus, String address) {
		this.bus = bus;
		this.address = address;
		
		init();
	}	
	
	public SlaveStation withOperations(Operation... operations) {
		executionPlans.addAll( Arrays.asList( operations ) );
		
		return this;
	}
	
	private void init() {
		executionPlans = new HashSet<Operation>();
	}
	
	// XXX This constructor which never be called.
	private SlaveStation() {
		this.bus = null;
		this.address = null;
		
		throw new RuntimeException();
	}
}