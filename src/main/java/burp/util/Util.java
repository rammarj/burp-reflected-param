/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.util;

import java.util.SplittableRandom;

/**
 *
 * @author Joaquin R. Martinez
 */
public class Util {
    /**
     * Generates a random string (for Multipart requests)
     * @param lenght the char number of the random string
     * @return the random string
     */
    public static String generateRandomString(int lenght) {
        SplittableRandom splittableRandom = new SplittableRandom();
        StringBuffer a = new StringBuffer();
        int nextInt, temp;
        for (int i = 0; i < lenght; i++) {
            nextInt = splittableRandom.nextInt(0, 2);
            if (nextInt == 1) {
                temp = splittableRandom.nextInt('A', 'Z');
            } else {
                temp = splittableRandom.nextInt('a', 'z');
            }
            a.append((char) temp);
        }
        return a.toString();
    }
}
