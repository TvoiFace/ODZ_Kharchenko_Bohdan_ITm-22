package com.google.firebase.example.fireeats.java.util;

import com.google.firebase.example.fireeats.java.model.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RatingUtil {

    public static final String[] REVIEW_CONTENTS = {
            "This was horrible!",
            "This was bad. I'll never go back.",
            "it wasn't bad. But I can't recommend it.",
            "This was very good, I'd go back.",
            "This was excelent!  Best ever!"
    };

    public static List<Rating> getRandomList(int length) {
        List<Rating> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add(getRandom());
        }

        return result;
    }

    public static double getAverageRating(List<Rating> ratings) {
        double sum = 0.0;

        for (Rating rating : ratings) {
            sum += rating.getRating();
        }

        return sum / ratings.size();
    }

    public static Rating getRandom() {
        Rating rating = new Rating();

        Random random = new Random();

        double score = random.nextDouble() * 5.0;
        String text = REVIEW_CONTENTS[(int) Math.floor(score)];

        rating.setUserId(UUID.randomUUID().toString());
        rating.setUserName("Ivan Belgorod");
        rating.setRating(score);
        rating.setText(text);

        return rating;
    }

}
