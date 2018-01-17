/*
 * Distributed Systems Lab
 * Copyright (C) Konrad Iwanicki, 2012-2014
 *
 * This file contains code samples for the distributed systems
 * course. It is intended for internal use only.
 */

public class ByteArrayUtils {

	public static String byteArrayToString(byte[] arr, int offset, int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = offset, n = Math.min(arr.length, offset + len); i < n; ++i) {
			String hex = Integer.toHexString(0xFF & arr[i]);
			if (hex.length() < 2) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
