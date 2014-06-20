package org.ong.mmcp.protocl;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class ControlCode {
	// 当由主站发出命令帧则‘从站异常标示’应该示为无效，但当D6为0时则标识
	// ‘从站正确应答’，这个设计有待争议
	// 按理说控制码不应该由用户选择
	
	// FIXME These be should is Enum type...
	
	/** 传输方向 */
	public static final byte IDX_TRANSFER_DIRECTION_D7 			= 0;
	public static final byte TRANSFER_DIRECTION_D7_LENG 			= 1;	
	public static final byte TRANSFER_DIRECTION_D7_VAL_0_REQUEST 	= 0;
	public static final byte TRANSFER_DIRECTION_D7_VAL_1_RESPONSE = 1;
	
	/** Flag indicates whether Slave station is abnormal */
	public static final byte IDX_SLAVE_STATOIN_ABNORMAL_FLAG_D6 				= 1;
	public static final byte SLAVE_STATOIN_ABNORMAL_FLAG_D6_LENG 				= 1;
	public static final byte SLAVE_STATOIN_ABNORMAL_FLAG_D6_VAL_0_ABNORMALLY 	= 0;
	public static final byte SLAVE_STATOIN_ABNORMAL_FLAG_D6_VAL_1_CORRECTLY 	= 1;
	
	/** Flag of subsequent frame */
	public static final byte IDX_HAS_SUBSEQUENT_FRAME_FLAG_D5 		= 2;
	public static final byte HAS_SUBSEQUENT_FRAME_FLAG_D5_LENG 		= 1;
	public static final byte HAS_SUBSEQUENT_FRAME_FLAG_D5_VAL_0_NO 	= 0;
	public static final byte HAS_SUBSEQUENT_FRAME_FLAG_D5_VAL_1_YES 	= 1;
	
	/** Function code */
	public static final byte IDX_FUNCTION_CODE_D4_TO_D0 			= 3;
	public static final byte FUNCTION_CODE_D4_TO_D0_LENG 			= 5;
}