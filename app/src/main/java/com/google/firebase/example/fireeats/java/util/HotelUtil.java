package com.google.firebase.example.fireeats.java.util;

import android.content.Context;

import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.java.model.Hotel;

import java.util.Arrays;
import java.util.Random;

public class HotelUtil {

    private static final String TAG = "HoteltUtil";

    private static final String[] PHOTOS_ARRAY = {
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_1.jpg?alt=media&token=ea87696f-b487-4217-8c0b-95a8a775bacf",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_2.jpg?alt=media&token=179760c8-33f2-408b-a68f-0cb6a0041514",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_3.jpg?alt=media&token=15d9e617-21ec-415b-8061-656116946397",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_4.jpg?alt=media&token=5c25ee47-e722-4bcc-9e2f-b68eed23b441",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_5.jpg?alt=media&token=77099a8c-9881-4c02-9c4f-c590a665dbee",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_6.jpg?alt=media&token=5818df7f-b3e8-4bf2-a53f-e9a49e7d3838",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_7.jpg?alt=media&token=5a31eb64-a771-4f92-9b42-b992ba4fee8b",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_8.jpg?alt=media&token=4636d129-b1c9-4193-b6b2-89ad31d7f1dc",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_9.jpg?alt=media&token=253f5918-ab94-4f22-b48d-6381bdb7f170",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_10.jpg?alt=media&token=a671c78f-dc4c-470b-bebd-4ddea262e46e",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_11.jpg?alt=media&token=cce6f841-25f5-4b1a-a2a5-0f34343ac971",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_12.jpg?alt=media&token=4b6a3d19-d12b-41dd-961e-2e11cc15b0f9",
            "https://firebasestorage.googleapis.com/v0/b/odz-android-2.appspot.com/o/hotel_13.jpg?alt=media&token=a2845697-a216-448b-8915-f310e6fe3613",
    };
    private static final String[] NAME_FIRST_WORDS = {
            "Royal",
            "Golden",
            "Crystal",
            "Silver",
            "Sunrise",
            "Paradise",
            "Grand",
            "Majestic",
            "Elegant",
            "Blissful",
            "Serene",
            "Radiant",
            "Tranquil",
            "Exquisite",
            "Luminous",
            "Opulent",
            "Captivating",
            "Enchanting",
            "Harmonious",
            "Splendid",
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Oasis",
            "Haven",
            "Retreat",
            "Resort",
            "Plaza",
            "Palace",
            "Lodge",
            "Manor",
            "Heights",
            "Gardens",
            "Crest",
            "Heights",
            "Vista",
            "Meadows",
            "Escapes",
    };

    public static Hotel getRandom(Context context) {
        Hotel hotel = new Hotel();
        Random random = new Random();

        // Cities (first elemnt is 'Any')
        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);

        // Categories (first element is 'Any')
        String[] categories = context.getResources().getStringArray(R.array.categories);
        categories = Arrays.copyOfRange(categories, 1, categories.length);

        int[] prices = new int[]{1, 2, 3, 4, 5};

        hotel.setName(getRandomName(random));
        hotel.setCity(getRandomString(cities, random));
        hotel.setCategory(getRandomString(categories, random));
        hotel.setPhoto(getRandomPhoto(random));
        hotel.setPrice(getRandomInt(prices, random));
        hotel.setNumRatings(random.nextInt(20));


        return hotel;
    }

    public static String getPriceString(Hotel hotel) {
        return getPriceString(hotel.getPrice());
    }

    public static String getPriceString(int priceInt) {
        switch (priceInt) {
            case 1:
                return "$";
            case 2:
                return "$$";
            case 3:
                return "$$$";
            case 4:
                return "$$$$";
            case 5:
            default:
                return "$$$$$";
        }
    }

    private static String getRandomPhoto(Random random) {
        return getRandomString(PHOTOS_ARRAY, random);
    }
    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

}
