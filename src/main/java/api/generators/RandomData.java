package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomData {

    private static final String[] NAMES = {
            "John", "James", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles",
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen"
    };

    private static final String[] SURNAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "White", "Harris"
    };

    private static final Random RANDOM = new Random();

    private RandomData(){

    };

    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getUserPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%$#";


    }

    public static String getRandomValidUsername(){
        return NAMES[RANDOM.nextInt(NAMES.length)] + " " + SURNAMES[RANDOM.nextInt(SURNAMES.length)];
    }
}
