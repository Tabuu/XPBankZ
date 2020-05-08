package nl.tabuu.xpbankz.util;

import org.bukkit.entity.Player;

/**
 * This class does experience calculations based on formulas found at <a href="https://minecraft.gamepedia.com/Experience#Leveling_up">the wiki</a>.
 */
public class ExperienceUtil {

    public static final int MAX_EXPERIENCE_LEVEL = 21863;

    /**
     * Returns the required experience points for the next experience level.
     *
     * @param level The current experience level.
     * @return The required experience points for the next experience level.
     */
    public static int pointsToNextLevel(int level) {
        if (level <= 15) return 2 * level + 7;

        else if (level <= 30) return 5 * level - 38;

        return 9 * level - 158;
    }

    /**
     * Converts the given experience level into the required experience points.
     *
     * @param level The experience level to get the required experience points of.
     * @return The amount of experience points required for this experience level.
     */
    public static int levelToPoints(int level) {
        int points = 0;
        for (int i = 0; i < level; i++) points += pointsToNextLevel(i);

        return points;
    }

    /**
     * Returns the amount of experience points required to go from the current experience level to the desired experience level.
     *
     * @param currentLevel The current experience level.
     * @param desiredLevel The desired experience level.
     * @return The amount of experience points required to go from the current experience level to the desired experience level.
     */
    public static int levelToPoints(int currentLevel, int desiredLevel) {
        return levelToPoints(desiredLevel) - levelToPoints(currentLevel);
    }

    /**
     * Returns the experience points converted to experience levels (disregarding the progress);
     *
     * @param points The experience points to convert to experience levels.
     * @return The experience points converted to experience levels (disregarding the progress);
     */
    public static int pointToLevel(int points) {
        int level = 0;

        while (points > 0) {
            points -= pointsToNextLevel(level);
            if(points >= 0)
                level++;
        }

        return level;
    }

    /**
     * Returns the total amount of experience points of that player.
     *
     * @param player The player to get the experience points of.
     * @return The total amount of experience points of that player.
     */
    public static int getPoints(Player player) {
        int points = levelToPoints(player.getLevel());
        return points + (int) (pointsToNextLevel(player.getLevel()) * player.getExp());
    }

    /**
     * Sets the players experience points to the given experience points.
     *
     * @param player The player to set the experience points of.
     * @param points The experience points to set to the player.
     */
    public static void setPoints(Player player, int points) {
        int level = pointToLevel(points);
        points -= levelToPoints(level);

        float progress = 1f / pointsToNextLevel(level) * points;

        player.setLevel(level);
        player.setExp(progress);
    }
}
