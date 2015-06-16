// Created by plusminus on 20:36:01 - 26.09.2008
package org.pyneo.maps.utils;

/**
 * @author Nicolas Gramlich
 */
public class MyMath implements Constants {
	public static double gudermannInverse(double aLatitude){
		return Math.log(Math.tan(PI_4 + (DEG2RAD * aLatitude / 2)));
	}

	public static double gudermann(double y){
		return RAD2DEG * Math.atan(Math.sinh(y));
	}

	public static int mod(int number, final int modulus){
		if(number > 0)
			return number % modulus;
		while(number < 0)
			number += modulus;
		return number;
	}
}
