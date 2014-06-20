package org.ong.mmcp.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BluetoothExpert {
	/**
	 * 
	 * 与设备配对 参考源码：platform/packages/apps/Settings.git
	 * 
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean createBond(BluetoothDevice device)
			throws Exception {
		Method createBondMethod = device.getClass()
				.getMethod( "createBond" );
		createBondMethod.setAccessible( true );
		
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);

		return returnValue.booleanValue();
	}

	/**
	 * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
	 * 
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean removeBond(BluetoothDevice device)
			throws Exception {

		Method removeBondMethod = device.getClass()
				.getMethod( "removeBond" );
		
		removeBondMethod.setAccessible( true );
		Boolean returnValue = (Boolean) removeBondMethod.invoke( device );

		return returnValue.booleanValue();
	}

	static public boolean setPin(BluetoothDevice device,
			String value) throws Exception {

		try	{
			Method setPinMethod = device.getClass()
					.getDeclaredMethod( 
							"setPin",					
							new Class[]	{ byte[].class } );
			
			setPinMethod.setAccessible( true );
			
			Boolean returnValue = (Boolean) setPinMethod.invoke(
					device,
					new Object[] { value.getBytes() } );
			
			Log.e( "returnValue", "" + returnValue );
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();

		}

		return true;
	}
	
	public static boolean setPairingConfirmation(BluetoothDevice device, boolean confirm) 
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, 
			InvocationTargetException {
		
		Method setPairingConfirmationMethod = device.getClass()
				.getMethod( "setPairingConfirmation", 
							new Class[] { boolean.class } );
		
		setPairingConfirmationMethod.setAccessible( true );

		// cancelBondProcess()

		Boolean returnedValue = (Boolean) setPairingConfirmationMethod
				.invoke( device, new Object[] { confirm } );
		
		return returnedValue.booleanValue();		
	}

	// 取消用户输入
	static public boolean cancelPairingUserInput(BluetoothDevice device) throws Exception {

		Method cancelPairingUserInputMethod = device.getClass()
				.getMethod( "cancelPairingUserInput" );
		cancelPairingUserInputMethod.setAccessible( true );

		// cancelBondProcess()

		Boolean returnValue = (Boolean) cancelPairingUserInputMethod.invoke( device );

		return returnValue.booleanValue();
	}

	// 取消配对
	static public boolean cancelBondProcess(BluetoothDevice device) throws Exception {

		Method cancelBondProcessMethod = device.getClass()
				.getMethod( "cancelBondProcess" );
		
		cancelBondProcessMethod.setAccessible( true );
		Boolean returnValue = (Boolean) cancelBondProcessMethod.invoke( device );

		return returnValue.booleanValue();
	}

	/**
	 * 
	 * 
	 * 
	 * @param clazz
	 */
	static public void printAllInform(Class clazz) {

		try {
			// 取得所有方法
			Method[] hideMethod = clazz.getMethods();

			int i = 0;

			for ( ; i < hideMethod.length; i++ ) {
				Log.e( "method name", hideMethod[i].getName() + ";and the i is:" + i );
			}

			// 取得所有常量
			Field[] allFields = clazz.getFields();

			for ( i = 0; i < allFields.length; i++ ) {
				Log.e( "Field name", allFields[i].getName() );
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}