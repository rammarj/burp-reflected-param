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
        StringBuffer a = new StringBuffer();
        int nextInt, temp;
        SplittableRandom sr = new SplittableRandom();
        for (int i = 0; i < lenght; i++) {
            nextInt = sr.nextInt(0, 2);
            temp = nextInt == 1 ? sr.nextInt('A', 'Z') : sr.nextInt('a', 'z');
            a.append((char) temp);
        }
        return a.toString();
    }
}
