package org.ong.mmcp.io;

import android.os.Parcel;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class BusSocket {
	protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
		return false;
	}

	private boolean transact(int code, Parcel data, Parcel reply, int flags) {
		// mRemote.transact
		return false;
	}
	
	private byte obtainParityCheckByte() {
		return 0x00;
	}
	
	private byte generateSeed() {
		return 0x00;
	}
	
	public byte[] encryptData(byte[] plainText,
			                  int securityMechanism,
			                  byte[] initVector,
			                  byte[] targetPublicKey) {
			                              
		return new byte[] {};
	}
	
	private void calculateEncryptionToken() {
	}
	
	private void resetSecurityKeys() {
		
	}
}