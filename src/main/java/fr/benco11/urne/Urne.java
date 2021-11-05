package fr.benco11.urne;

import fr.benco11.urne.config.UrneConfiguration;

public class Urne {
    public static void main(String... args) {
        UrneBot bot = new UrneBot(UrneConfiguration.loadConfiguration());
        bot.init();
    }
}
