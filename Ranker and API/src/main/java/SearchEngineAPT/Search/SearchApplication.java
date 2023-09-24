package SearchEngineAPT.Search;

import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@SpringBootApplication
@RestController
public class SearchApplication {
	static mongoDB DB=new mongoDB();
	static TfIdfRanker Ranker;

	public static void main(String[] args) {
		Ranker= new TfIdfRanker();
		SpringApplication.run(SearchApplication.class, args);
	}

	@GetMapping("/search")
	public List<Document> searchByName(@RequestParam String q)  {
		long startTime = System.nanoTime();
		q=q.toLowerCase();
		DB.Add_to_QuaryList(q);
		List<Document>answer=Ranker.RankAll(q);
		answer.add(new Document("time",(double) (System.nanoTime() - startTime) / 1_000_000_000.0));
		return answer;
	}

	@GetMapping("/Query")
	public Set<String> queryList() {
		return DB.GetQuearyList();
	}

}
