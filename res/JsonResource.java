package sentinelFormatter.bot.res;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Resources;

public class JsonResource {
	public static String getResource(String path) throws URISyntaxException, IOException {
		//URI uri = JsonResource.class.getClassLoader().getResource(path).toURI();
		//Path myFolderPath = Paths.get(uri);
		return Resources.toString(JsonResource.class.getClassLoader().getResource(path), StandardCharsets.UTF_8);
		//return String.join("", Files.readAllLines(myFolderPath));
		
		//String.join("", Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("district.json").toURI())));
	}
}
