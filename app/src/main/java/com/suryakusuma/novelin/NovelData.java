package com.suryakusuma.novelin;

import java.util.ArrayList;
import java.util.List;

public class NovelData {
    public static List<Novel> getAllNovels() {
        List<Novel> list = new ArrayList<>();
        list.add(new Novel(
                "Solo Leveling",
                "Chugong",
                "In a world where hunters, humans who possess magical abilities, must battle deadly monsters...",
                R.drawable.novel2,
                "Chapter 1: The Weakest Hunter..."
        ));
        list.add(new Novel(
                "Omniscient Reader",
                "Sing Shong",
                "Kim Dokja's favorite web novel comes to life, and he is the only one who knows how the world will end.",
                R.drawable.novel1,
                "Chapter 1: The world is destroyed..."
        ));
        list.add(new Novel(
                "The Beginning After The End",
                "TurtleMe",
                "King Grey has unrivaled strength, wealth, and prestige in a world governed by martial ability.",
                R.drawable.novel1,
                "Chapter 1: A King's end..."
        ));
        return list;
    }
}
