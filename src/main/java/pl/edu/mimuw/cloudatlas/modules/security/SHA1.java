/*
 * Distributed Systems Lab
 * Copyright (C) Konrad Iwanicki, 2012-2014
 *
 * This file contains code samples for the distributed systems
 * course. It is intended for internal use only.
 */
package pl.edu.mimuw.cloudatlas.modules.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

	private final static String DIGEST_ALGORITHM = "SHA-1";
	private final static byte[] SAMPLE_BYTES =
		{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

	public static byte[] calculate(byte[] bytes) throws NoSuchAlgorithmException {
		MessageDigest digestGenerator =
				MessageDigest.getInstance(DIGEST_ALGORITHM);
		return digestGenerator.digest(SAMPLE_BYTES);
	}
	
}
