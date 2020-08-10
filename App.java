package sentinelFormatter.bot;

import java.net.URISyntaxException;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Hello world!
 *
 */
//@SpringBootApplication
public class App {
	
	public static void main(String[] args){
		//SpringApplication.run(App.class, args);
		ApiContextInitializer.init();

		TelegramBotsApi botsApi = new TelegramBotsApi();

		try {
			botsApi.registerBot(new Bot());
		} catch (TelegramApiException | URISyntaxException e) {
			e.printStackTrace();
		}

	}
}