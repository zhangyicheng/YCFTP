package com.galaxywind.ycftp.utils;

import android.util.Log;

public class HexUtil {
	
	/*
	 * 普通字符转为十六进制字符
	 */
	public static String toHexString(String s) {
		String str = "";
		if(s!=null)
		for (int i = 0; i < s.length(); i++) {
			int ch = s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	public static byte[] toByteArray(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str.getBytes();
	}

	/*
	 * 
	 */
	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static byte[] int2Byte(int intValue) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (intValue >> 8 * (3 - i) & 0xFF);
			// System.out.print(Integer.toBinaryString(b[i])+" ");
			System.out.print((b[i] & 0xFF) + " ");
		}
		return b;
	}

	public static int byte2Int(byte[] b) {
		int intValue = 0, tempValue = 0xFF;
		for (int i = 0; i < b.length; i++) {
			intValue += (b[i] & 0xFF) << (8 * (3 - i));
			// System.out.print(Integer.toBinaryString(intValue)+" ");
		}
		return intValue;
	}

//	public static byte uniteBytes(byte src0, byte src1) {
//		byte b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
//				.byteValue();
//		b0 = (byte) (b0 << 4);
//		byte b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
//				.byteValue();
//		byte ret = (byte) (b0 | b1);
//		return ret;
//	}
//
//	/*
//	 * 将十进制字符串转为字节数组
//	 */
//	public static byte[] HexString2Bytes(String src) {
//		byte[] ret = new byte[src.length()/2];
//		byte[] tmp = src.getBytes();
//		for (int i = 0; i < ret.length; ++i) {
//			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
//		}
//		return ret;
//	}
	
	/*
	 * 将字节数组显示为十六进制字符串
	 */
	public static String byte2String(byte[] bytes){
		StringBuffer sb = new StringBuffer(bytes.length);
		for(int i=0; i<bytes.length; i++){
			byte b1 = (byte) ((bytes[i]>>4)&0x0F);
			byte b2 = (byte) (bytes[i]&0x0f);
			sb.append(Integer.toHexString(b1)).append(Integer.toHexString(b2));
		}
		return sb.toString().toUpperCase();
	}
	
	/*
	 * 十六进制字符串转为字节数组
	 */
	public static byte[] HexString2Bytes(String sString) 
	{ 
		byte[] result = new byte[sString.length()/2]; 
		byte[] temp = sString.getBytes(); 
		for(int i=0; i<sString.length()/2; ++i )
		{ 
			result[i] = uniteBytes(temp[i*2], temp[i*2+1]); 
		} 
		return result; 
	}
	
	public static byte uniteBytes(byte src0, byte src1) {
	    byte _b0 = Byte.decode("0x" + new String(new byte[] {src0})).byteValue();
	    _b0 = (byte) (_b0 << 4);
	    byte _b1 = Byte.decode("0x" + new String(new byte[] {src1})).byteValue();
	    byte result = (byte) (_b0 | _b1);
	    return result;
	}
	
	 /** 
     *  
     * @param hexString 
     * @return 将十六进制转换为字节数组 
     */  
	public static String hexString2binaryString(String hexString)  
    {  
        if (hexString == null || hexString.length() % 2 != 0)  
            return null;  
        StringBuffer bSb = new StringBuffer(), tmp = new StringBuffer();  
        int length = hexString.length();
        for (int i = 0; i < length; i++)  
        {  
            tmp.append("0000"  
                    + Integer.toBinaryString(Integer.parseInt(hexString  
                            .substring(i, i + 1), 16)));  
            bSb.append(tmp.substring(tmp.length() - 4));  
        }  
        return bSb.toString();  
    }   
	
	/**
	 * 电源控制器状态十六进制转2进制
	 * @param hexStr
	 * @return
	 */
	public static String formatStateBinary(String hexStr){
		StringBuffer result = new StringBuffer();
		StringBuffer temp1 = new StringBuffer();
		int strLength = hexStr.length();
		String temp;
		int offset;
		for(int i=0;i<strLength;i++){
			temp = Integer.toBinaryString(Integer.valueOf(hexStr.substring(i, i+1), 16));
			offset = 4 - temp.length();
			switch (offset) {
			case 1:
				temp = "0"+temp;
				break;
			case 2:
				temp = "00"+temp;
				break;
			case 3:
				temp = "000"+temp;
				break;
			default:
				break;
			}
			temp1.append(temp);
			if(i%2!=0){
				result.append(temp1.reverse());
				temp1.setLength(0);
			}
		}
		return result.toString();
	}
	
	/**
	 * 字符串翻转
	 * @param s
	 * @return
	 */
	public static String reverse1(String s) {

		  int length = s.length();

		  if (length <= 1)

		   return s;

		  String left = s.substring(0, length / 2);

		  String right = s.substring(length / 2, length);

		  return reverse1(right) + reverse1(left);
		 }

	
	/**
	 * 将带符号十六进制温度转为十进制
	 * @param hexStr
	 * @return
	 */
	public static String formatTemperature(String hexStr){
		StringBuffer decimalSb = new StringBuffer();
		if(hexStr.substring(0, 2).equals("2B")){
//			decimalSb.append("+");
		}else{
			decimalSb.append("-");
		}
		if(hexStr.length()>6){
			decimalSb.append((char)Integer.parseInt(hexStr.substring(2, 4), 16)).append((char)Integer.parseInt(hexStr.substring(4, 6), 16)).append(".").append((char)Integer.parseInt(hexStr.substring(6, 8), 16));
		}else{
			decimalSb.append((char)Integer.parseInt(hexStr.substring(2, 4), 16)).append((char)Integer.parseInt(hexStr.substring(4, 6), 16));
		}
		return decimalSb.toString();
	}
	
	/**
	 * 将十六进制电压信息转为十进制
	 * @param hexStr
	 * @return
	 */
	public static String formatVoltage(String hexStr){
		double vol = (Integer.valueOf(hexStr, 16))*0.1;
		Log.d("电压"+hexStr, ""+vol);
		return String.valueOf(vol);
	}
	
	public static String formatIrcVoltage(String hexStr){
		double vol = 2+(Integer.valueOf(hexStr, 16))*0.01;
		Log.d("电压"+hexStr, ""+vol);
		return String.valueOf(vol);
	}
	
	/**
	 * 将十六进制湿度信息转为十进制
	 * @param hexStr
	 * @return
	 */
	public static String formatHumidity(String hexStr){
		StringBuffer humi = new StringBuffer();
		if(hexStr.length()>2){
			humi.append((char)Integer.parseInt(hexStr.substring(0, 2), 16)).append((char)Integer.parseInt(hexStr.substring(2, 4), 16)).append(".").append((char)Integer.parseInt(hexStr.substring(4, 6), 16));
		}else{
			humi.append((char)Integer.parseInt(hexStr.substring(0, 2), 16)).append("0");
		}
		return humi.toString();
	}
	
	public static String fromatUniqueKey(String str1,String str2,String str3,String str4){
		String stra = Integer.toHexString(Integer.parseInt(str1)).toUpperCase();
		String strb = Integer.toHexString(Integer.parseInt(str2)).toUpperCase();
		String strc = Integer.toHexString(Integer.parseInt(str3)).toUpperCase();
		String strd = Integer.toHexString(Integer.parseInt(str4)).toUpperCase();
		if(stra.length()<2){
			stra= "0"+stra;
		}
		if(strb.length()<4){
			if(strb.length()==1){
				strb= "000"+strb;
			}else if(strb.length()==2){
				strb= "00"+strb;
			}else if(strb.length()==3){
				strb="0"+strb;
			}
		}if(strc.length()<4){
			if(strc.length()==1){
				strc= "000"+strc;
			}else if(strc.length()==2){
				strc= "00"+strc;
			}else if(strc.length()==3){
				strc="0"+strc;
			}
		}if(strd.length()<4){
			 if(strd.length()==1){
				strd= "000"+strd;
			}else if(strd.length()==2){
				strd= "00"+strd;
			}else if(strd.length()==3){
				strd="0"+strd;
			}
		}
		return stra+strb+strc+strd;

	}
	
	public static String fromatDelKey(String str1,String str2){
		String stra = Integer.toHexString(Integer.parseInt(str1)).toUpperCase();
		String strb = Integer.toHexString(Integer.parseInt(str2)).toUpperCase();
		if(stra.length()<2){
			stra= "0"+stra;
		}
		if(strb.length()<4){
			if(strb.length()==1){
				strb= "000"+strb;
			}else if(strb.length()==2){
				strb= "00"+strb;
			}else if(strb.length()==3){
				strb="0"+strb;
			}
		}
		return stra+strb;
	}
	
}
