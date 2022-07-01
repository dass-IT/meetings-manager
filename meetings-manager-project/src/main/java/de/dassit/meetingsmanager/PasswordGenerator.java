/*
 * Copyright 2022 dass IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dassit.meetingsmanager;

import java.util.Random;

/**
 * A trivial password generator sutable for embedding a
 * password-like string in an URL.
 * @author Sebastian Lederer <sebastian.lederer@dass-it.de>
 *
 */
public class PasswordGenerator {
	public static String password(int length)
	{
	        // String symbol = "-/.^&*_!@%=+>)"; 
	        String cap_letter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
	        String small_letter = "abcdefghijklmnopqrstuvwxyz"; 
	        String numbers = "0123456789"; 


	        String finalString = cap_letter + small_letter + 
	                numbers /* + symbol */; 

	        Random random = new Random(); 

	        char[] password = new char[length]; 

	        for (int i = 0; i < length; i++) 
	        { 
	            password[i] = 
	                    finalString.charAt(random.nextInt(finalString.length())); 

	        } 
	        return new String(password);
	}
}
