package ru.boomearo.blockhunt.utils;

import java.util.Random;

public final class RandomUtil {

	public static int getRandomNumberRange(int min, int max) {
	    Random randomGenerator = new Random();
	    return randomGenerator.nextInt((max - min) + 1) + min;
	}
	
}
