package com.suryakusuma.novelin;

import java.util.ArrayList;
import java.util.List;

public class NovelData {
    public static List<Novel> getAllNovels() {
        List<Novel> list = new ArrayList<>();
        list.add(new Novel(
                "The Woman in White",
                "Wilkie Collinsq",
                "A ghostly woman warns a beautiful Victorian heiress about a count, and a strange spell haunts a mansion...",
                R.drawable.novel12,
                "Chapter 1: Once upon a time..."
        ));
        list.add(new Novel(
                "And Then There Were None",
                "Agatha Christie",
                "Europe teeters on the brink of war. Ten strangers are invited to Soldier Island, an isolated rock near the Devon coast...",
                R.drawable.novel11,
                "Chapter 1: Once upon a time..."
        ));
        list.add(new Novel(
                "The Midnight Library",
                "Matt Haig",
                "a woman named Nora Seed, who after reaching a breaking point in her life, finds herself in a mystical library...",
                R.drawable.novel10,
                "Chapter 1: Once upon a time..."
        ));
        list.add(new Novel(
                "Harry Potter",
                "J.K. Rowling",
                "Harry is described as having his father's perpetually untidy black hair, his mother's bright green eyes, and a lightning bolt-shaped scar on his forehead..",
                R.drawable.novel9,
                "Chapter 1: Once upon a time..."
        ));
        list.add(new Novel(
                "Metamorphosis",
                "Franz Kafka",
                "Gregor Samsa, from human to insect and the complications he faces in his new form...",
                R.drawable.novel8,
                "Chapter 1: he woke up with the body of a cockroach..."
        ));
        list.add(new Novel(
                "The Little Prince",
                "Antoine de Saint-Exupéry",
                "The titular character who travels from planet to planet...",
                R.drawable.novel7,
                "Chapter 1: He is a young, curious boy with golden hair who asks many questions..."
        ));
        list.add(new Novel(
                "If Cat Disappeared from the World",
                "Genki Kawamura",
                "Young mailman finds out he has no time left due to a terminal disease...",
                R.drawable.novel6,
                "Chapter 1: Suddenly he is approached by a devil..."
        ));
        list.add(new Novel(
                "Animal Farm",
                "George Orwell",
                "a novel about a group of animals who take control of the farm they live on...",
                R.drawable.novel4,
                "Chapter 1: on a farm..."
        ));
        list.add(new Novel(
                "Don Quixote",
                "Miguel De Cervantes Saavedra",
                "The middle-aged gentleman from La Mancha who becomes obsessed with chivalry books and decides to become a knight-errant...",
                R.drawable.novel3,
                "Chapter 1: Once upon a time..."
        ));
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
