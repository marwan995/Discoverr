//import com.mongodb.MongoClient;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Attribute;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import java.io.*;
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map.Entry;
//import java.util.TreeMap;
//
//
//public class Crawler implements Runnable{
//
//	private static int MAX_CRAWLS;
//	private static HashMap<String, Boolean> banned;							//restricted links read from Robot.txt
//	private static HashMap<String, Boolean> downloaded;					//to store downloaded documents
//	private static FileWriter wFile;									//not a buffered writer as we don't close the file till the end
//	private static HashMap<String, String> visited;						//to store compact strings
//	private static MongoCollection<org.bson.Document> col;
//	private static TreeMap<String, Integer> seedMap;
//	private static String [] seeds = { "https://www.reddit.com/","https://www.cnn.com/","https://en.wikipedia.org", "https://npr.org", "https://www.nytimes.com", "https://abcnews.go.com", "https://weather.com", "https://stackoverflow.com/",
//			"https://www.allrecipes.com/","https://www.imdb.com/","https://www.google.com",
//			"https://www.facebook.com",
//			"https://www.twitter.com",
//			"https://www.instagram.com",
//			"https://www.linkedin.com",
//			"https://www.reddit.com",
//			"https://www.pinterest.com",
//			"https://www.amazon.com",
//			"https://www.ebay.com",
//			"https://www.etsy.com",
//			"https://www.apple.com",
//			"https://www.microsoft.com",
//			"https://www.netflix.com",
//			"https://www.hulu.com",
//			"https://www.nytimes.com",
//			"https://www.washingtonpost.com",
//			"https://www.bbc.com",
//			"https://www.cnn.com",
//			"https://www.theguardian.com",
//			"https://www.forbes.com",
//			"https://www.wsj.com",
//			"https://www.ted.com",
//			"https://www.coursera.org",
//			"https://www.udacity.com",
//			"https://www.khanacademy.org",
//			"https://www.stackoverflow.com",
//			"https://www.github.com",
//			"https://www.dribbble.com",
//			"https://www.behance.net",
//			"https://www.instagram.com",
//			"https://www.500px.com",
//			"https://www.flickr.com",
//			"https://www.unsplash.com",
//			"https://www.imgur.com",
//			"https://www.wikimedia.org",
//			"https://www.quora.com",
//			"https://www.tripadvisor.com",
//			"https://www.booking.com",
//			"https://www.expedia.com",
//			"https://www.airbnb.com",
//			"https://www.zillow.com",
//			"https://www.realtor.com",
//			"https://www.nike.com",
//			"https://www.underarmour.com",
//			"https://www.lululemon.com",
//			"https://www.nfl.com",
//			"https://www.espn.com",
//			"https://www.cbs.com",
//			"https://www.nbc.com",
//			"https://www.abc.com",
//			"https://www.pbs.org",
//			"https://www.npr.org",
//			"https://www.who.int",
//			"https://www.cdc.gov",
//			"https://www.nih.gov",
//			"https://www.who.int",
//			"https://www.imdb.com",
//			"https://www.rottentomatoes.com",
//			"https://www.metacritic.com",
//			"https://www.ign.com",
//			"https://www.gamespot.com",
//			"https://www.pcmag.com",
//			"https://www.cnet.com",
//			"https://www.wired.com",
//			"https://www.theverge.com",
//			"https://www.engadget.com",
//			"https://www.techcrunch.com",
//			"https://www.mashable.com",
//			"https://www.gizmodo.com",
//			"https://www.arstechnica.com",
//			"https://www.bleacherreport.com",
//			"https://www.mlb.com",
//			"https://www.nhl.com",
//			"https://www.ufc.com",
//			"https://www.ncaa.com",
//			"https://www.pga.com",
//			"https://www.fifa.com",
//			"https://www.uefa.com",
//			"https://www.nasa.gov",
//			"https://www.space.com",
//			"https://www.nationalgeographic.com",
//			"https://www.smithsonianmag.com",
//			"https://www.britannica.com",
//			"https://www.history.com",
//			"https://www.natlawreview.com",
//			"https://www.law.cornell.edu",
//			"https://www.findlaw.com",
//			"https://www.usatoday.com",
//			"https://www.cnbc.com",
//			"https://www.bloomberg.com",
//			"https://www.finance.yahoo.com",
//			"https://www.investopedia.com", "https://www.fool.com", "https://www.zerohedge.com", "https://www.seekingalpha.com", "https://www.barrons.com", "https://www.wsj.com/market-data", "https://www.coinmarketcap.com", "https://www.cryptocompare.com", "https://www.bitcointalk.org", "https://www.cointelegraph.com", " https://www.webmd.com/","https://www.health.harvard.edu/","https://my.clevelandclinic.org/" };
//
//
//
//	//-------------------------------------------Methods-------------------------------------------
//
//	//-----------------Constructor to create threads and start them
//	public Crawler(int nThreads, int maxCrawls) {
//		if(nThreads > 16)											//checking the max number of threads
//		{
//			System.out.println("\nError: Number of threads is too big\n");
//			return;
//		}
//
//		seedMap = new TreeMap<String, Integer>();
//		MAX_CRAWLS = maxCrawls;
//		banned = new HashMap<String, Boolean>();
//		downloaded = new HashMap<String, Boolean>();
//		visited = new HashMap<String, String>();
//		MongoClient mongoClient = new MongoClient("localhost", 27017);
//		MongoDatabase db = mongoClient.getDatabase("SearchEngine");
//		col = (MongoCollection<org.bson.Document>) db.getCollection("Crawler");
//
//		for(String str : seeds)
//		{
//			seedMap.put(str, 0);
//		}
//
//		try
//		{				//reading banned URLs from Robot.txt
//
//			FileReader file = new FileReader("Resources\\Robot.txt");
//			BufferedReader reader = new BufferedReader(file);
//
//			String str = reader.readLine();
//			while(str != null)
//			{
//				banned.put(str,false);
//				str = reader.readLine();
//			}
//
//			reader.close();
//			file.close();
//		}
//		catch (IOException e)												//if an error happened while opening Robot.txt
//		{
//			System.out.println("Error Openning Robot.txt");
//		}
//
//		try
//		{																//reading downloaded URLs
//			File tempFile = new File("Resources\\Downloaded.txt");
//			if(!tempFile.exists())											//if the file doesn't exist create it
//				tempFile.createNewFile();
//
//			FileReader file = new FileReader("Resources\\Downloaded.txt");
//			BufferedReader reader = new BufferedReader(file);
//
//			String str = reader.readLine();
//			while(str != null)
//			{
//				downloaded.put(str,false);
//				str = reader.readLine();
//			}
//			reader.close();
//			file.close();
//
//			wFile = new FileWriter("Resources\\Downloaded.txt", true);				//true is to append to the file
//		}
//		catch (IOException e)
//		{
//			System.out.println("Error with Downloaded.txt");
//		}
//
//		for(int i =0;i <nThreads ; i++)
//		{
//			Thread t= new Thread(new Crawler());
//			t.start();												//starting threads
//		}
//	}
//
//
//
//	//-----------------Constructor for multithreading
//	//dummy constructor for threads
//	private Crawler()
//	{}
//
//
//	//-----------------Used for multithreading
//	//each thread crawls different pages and adds them to a list
//	public void run()
//	{
//		for(Entry<String, Integer> node : seedMap.entrySet())
//		{
//
//			synchronized (seedMap)
//			{
//				if(node.getValue() == 0)
//					node.setValue(1);
//				else
//					continue;
//			}
//			try
//			{
//				crawl(node.getKey(),0);
//
//			}
//			catch (MalformedURLException e)
//			{
//				System.out.println("\nThread faced an Error with URL");
//			}
//		}
//	}
//
//
//	//-----------------Recursively crawls pages, checks if they're repeated, gets links in them, adds them to a list
//	private void crawl(String url, int level) throws MalformedURLException
//	{
//		if(level > 5)
//			return;
//		url = normalize(url);
//		Document doc = request(url);								//crawl it to get the links in it
//		Document doc2 = request(url);
//		String mainDomain = null;
//		if(doc != null && doc2 != null && doc.documentType() != null && doc.documentType().toString().contains("html") && !url.toLowerCase().contains("login"))
//		{
//			String compactKey = compact(doc.body().text());
//			synchronized (visited)				//lock the list
//			{
//				try {
//					mainDomain = new URL(url).getHost();
//				}
//				catch (MalformedURLException e1)
//				{
//					System.out.println("Error Getting The Domain");
//				}
//
//				if((!visited.containsKey(compactKey) && (mainDomain != null && checkBanned(mainDomain)) && (downloaded.isEmpty() || !downloaded.containsKey(url))))
//				{	//if it's not visited before and not banned and not downloaded before
//					System.out.println(visited.size() + " | " + Thread.currentThread().getId() + " " + url);
////					System.out.println(compactKey);
//					visited.put(compactKey, url);
//
//					try
//					{
//						synchronized (wFile)
//						{
//							wFile.write(url + "\n");			//write it to downloaded.txt
//						}
//					}
//					catch (IOException e)
//					{				//when the file is closed it throws an error so suppress it
//						System.out.println("Error Writing to Downloaded.txt");
//					}
//				}
//				else
//					return;
//			}
//
//
//			if(visited.size() <= MAX_CRAWLS)								//stop when max number of documents is reached
//			{
//				org.bson.Document tempBSON = new org.bson.Document();
//				removeAttributes(doc2.body());
//				String text = doc2.html().replaceAll("<[^>]>|\"[^\"]\"", "").replaceAll("[^\\p{L}\\p{Nd}]+", " ");
//
//				ArrayList<String> references = new ArrayList<String>();
//				for(Element a : doc.select("a[href]"))
//				{
//					String link = normalize(a.absUrl("href"));
//					String newDomain = null;
//					try {
//						newDomain = new URL(link).getHost();
//					}
//					catch (MalformedURLException e)
//					{
//						System.out.println("Error Getting The Domain");
//					}
//
//					if(newDomain != null && !newDomain.substring(newDomain.indexOf('.') + 1, newDomain.length()).equals(mainDomain.substring(mainDomain.indexOf('.') + 1, mainDomain.length())))
//						references.add(normalize(link));
//				}
//
//
//				tempBSON.append("title",doc2.title())
//						.append("url",url)
//						.append("WordsCount",text.split("\\s+").length)
//						.append("page", doc2.body().toString())
//						.append("links", references);
//
//				org.bson.Document query = new org.bson.Document("url", url);
//				FindIterable<org.bson.Document> check=col.find(query);
//
//
//				if(!check.iterator().hasNext())
//
//					col.insertOne(tempBSON);
//
//				for(Element a : doc.select("a[href]"))				//loop through the anchor tags in the page
//				{
//					String link = a.absUrl("href");					//get the absolute url
//
//					if(visited.size() <= MAX_CRAWLS)				//to avoid writing to the file after closing
//						crawl(link, level + 1);									//recursively crawl the new link
//				}
//			}
//			else															//when MAX_CRAWLS URLs are crawled close the file
//			{
//				try
//				{
//					wFile.close();
//				}
//				catch (IOException e)
//				{
//					System.out.println("Error Closing Downloaded.txt");
//				}
//			}
//		}
//	}
//
//
//	//-----------------Sends an HTTP request and returns the response with the full document
//	private Document request(String url) throws MalformedURLException
//	{
//
//		try
//		{
//			Connection con = Jsoup.connect(url);				//set the connection to the website
//			Document doc = con.get();							//make HTTP request
//
//			if(con.response().statusCode() == 200)				//HTTP request status code was OK
//			{
//				return doc;
//			}
//			return null;
//		} catch (IOException e) {
//			System.out.println(url + " Error Requesting Document");
//			return null;
//		}
//	}
//
//	private String compact(String baseText)
//	{
//		String compactSTR = new String();
//		String [] splittedSTR = baseText.split(" ");
//
//		int increment = (splittedSTR.length > 10 )? splittedSTR.length/10 : 1;
//
//		for(int i=0;i<splittedSTR.length;i+=increment)
//		{
//			compactSTR += splittedSTR[i];
//		}
//		return compactSTR;
//	}
//	private String normalize(String url)
//	{
//		try
//		{
//			String domain = new URL(url).getHost();
//			if(domain.length() - domain.replace(".", "").length() < 2)
//			{
//				String [] temp= url.split("//");
//				if(temp.length != 1)
//					url = temp[0] + "//www." + temp[1];
//			}
//		}
//		catch (MalformedURLException e1)
//		{
//			System.out.println("Error Normalizing the URL");
//		}
//
//		if(url.contains("#"))
//			url = url.subSequence(0, url.indexOf("#")).toString();
//		if(url.contains("?"))
//			url = url.subSequence(0, url.indexOf("?")).toString();
//
//		String temp = url;
//		try
//		{
//			temp = new URI(temp).normalize().toString();
//
//			if(temp.charAt(temp.length()-1) != '/' && (temp.length() - temp.replace(":", "").length() < 2))
//				temp = temp.concat("/");
//		}
//		catch (URISyntaxException e)
//		{
//			System.out.println("Error Normalizing the URL");
//		}
//		return temp;
//	}
//	public static void removeAttributes(Element element) {
//		Elements scripts = element.getElementsByTag("script");
//		Elements svg = element.getElementsByTag("svg");
//		Elements footer = element.getElementsByTag("footer");
//		svg.remove();
//		scripts.remove();
//		footer.remove();
//		element.removeAttr("class");
//		element.removeAttr("id");
//		element.removeAttr("style");
//		element.removeAttr("href");
//		for( Attribute i:element.attributes()){
//			element.removeAttr(i.getKey().toString());
//		}
//		for (Element child : element.children()) {
//			removeAttributes(child);
//		}
//	}
//	private static boolean checkBanned(String domain)
//	{
//		if(downloaded.isEmpty())
//			return true;
//
//		for(Entry<String, Boolean> e: downloaded.entrySet())
//		{
//			if(e.getKey().equals(domain))
//				return false;
//		}
//		return true;
//	}
//}

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


public class Crawler implements Runnable{

	private static int MAX_CRAWLS;
	private static ConcurrentHashMap<String, Boolean> banned;							//restricted links read from Robot.txt
	private static ConcurrentHashMap<String, Boolean> downloaded;					//to store downloaded documents
	private static FileWriter wFile;									//not a buffered writer as we don't close the file till the end
	private static ConcurrentHashMap<String, String> visited;						//to store compact strings
	private static MongoCollection<org.bson.Document> col;
	private static TreeMap<String, Boolean> seedMap;
	private static String [] seeds = { "https://www.reddit.com/","https://www.cnn.com/","https://en.wikipedia.org", "https://npr.org", "https://www.nytimes.com", "https://abcnews.go.com", "https://weather.com", "https://www.youtube.com/", "https://stackoverflow.com/",
			"https://www.allrecipes.com/","https://www.imdb.com/","https://www.google.com",
			"https://www.youtube.com",
			"https://www.facebook.com",
			"https://www.twitter.com",
			"https://www.instagram.com",
			"https://www.linkedin.com",
			"https://www.reddit.com",
			"https://www.pinterest.com",
			"https://www.amazon.com",
			"https://www.ebay.com",
			"https://www.etsy.com",
			"https://www.apple.com",
			"https://www.microsoft.com",
			"https://www.netflix.com",
			"https://www.hulu.com",
			"https://www.nytimes.com",
			"https://www.washingtonpost.com",
			"https://www.bbc.com",
			"https://www.cnn.com",
			"https://www.theguardian.com",
			"https://www.forbes.com",
			"https://www.wsj.com",
			"https://www.huffpost.com",
			"https://www.buzzfeed.com",
			"https://www.ted.com",
			"https://www.coursera.org",
			"https://www.udacity.com",
			"https://www.khanacademy.org",
			"https://www.stackoverflow.com",
			"https://www.github.com",
			"https://www.dribbble.com",
			"https://www.behance.net",
			"https://www.instagram.com",
			"https://www.500px.com",
			"https://www.flickr.com",
			"https://www.unsplash.com",
			"https://www.imgur.com",
			"https://www.wikimedia.org",
			"https://www.quora.com",
			"https://www.tripadvisor.com",
			"https://www.booking.com",
			"https://www.expedia.com",
			"https://www.airbnb.com",
			"https://www.zillow.com",
			"https://www.realtor.com",
			"https://www.nike.com",
			"https://www.adidas.com",
			"https://www.underarmour.com",
			"https://www.lululemon.com",
			"https://www.nba.com",
			"https://www.nfl.com",
			"https://www.espn.com",
			"https://www.cbs.com",
			"https://www.nbc.com",
			"https://www.abc.com",
			"https://www.pbs.org",
			"https://www.npr.org",
			"https://www.who.int",
			"https://www.cdc.gov",
			"https://www.nih.gov",
			"https://www.who.int",
			"https://www.imdb.com",
			"https://www.rottentomatoes.com",
			"https://www.metacritic.com",
			"https://www.ign.com",
			"https://www.gamespot.com",
			"https://www.pcmag.com",
			"https://www.cnet.com",
			"https://www.wired.com",
			"https://www.theverge.com",
			"https://www.engadget.com",
			"https://www.techcrunch.com",
			"https://www.mashable.com",
			"https://www.gizmodo.com",
			"https://www.arstechnica.com",
			"https://www.bleacherreport.com",
			"https://www.mlb.com",
			"https://www.nhl.com",
			"https://www.ufc.com",
			"https://www.ncaa.com",
			"https://www.pga.com",
			"https://www.fifa.com",
			"https://www.uefa.com",
			"https://www.nasa.gov",
			"https://www.space.com",
			"https://www.nationalgeographic.com",
			"https://www.smithsonianmag.com",
			"https://www.britannica.com",
			"https://www.history.com",
			"https://www.natlawreview.com",
			"https://www.law.cornell.edu",
			"https://www.lawyers.com",
			"https://www.findlaw.com",
			"https://www.usatoday.com",
			"https://www.cnbc.com",
			"https://www.bloomberg.com",
			"https://www.finance.yahoo.com",
			"https://www.investopedia.com", "https://www.fool.com", "https://www.zerohedge.com", "https://www.seekingalpha.com", "https://www.barrons.com", "https://www.wsj.com/market-data", "https://www.coinmarketcap.com", "https://www.cryptocompare.com", "https://www.bitcointalk.org", "https://www.cointelegraph.com", "https://www.coinbase.com", "https://www.binance.com", "https://www.kraken.com", "https://www.poloniex.com", "https://www.bitstamp.net"};



	//-------------------------------------------Methods-------------------------------------------

	//-----------------Constructor to create threads and start them
	public Crawler(int nThreads, int maxCrawls) {
		if(nThreads > 16)											//checking the max number of threads
		{
			System.out.println("\nError: Number of threads is too big\n");
			return;
		}

		seedMap = new TreeMap<String, Boolean>();
		MAX_CRAWLS = maxCrawls;
		banned = new ConcurrentHashMap<String, Boolean>();
		downloaded = new ConcurrentHashMap<String, Boolean>();
		visited = new ConcurrentHashMap<String, String>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("SearchEngine");
		col = (MongoCollection<org.bson.Document>) db.getCollection("CrawlerV2");

		for(String str : seeds)
		{
			seedMap.put(str, false);
		}

		try
		{				//reading banned URLs from Robot.txt

			FileReader file = new FileReader("Resources\\Robot.txt");
			BufferedReader reader = new BufferedReader(file);

			String str = reader.readLine();
			while(str != null)
			{
				if(str.contains("Disallow:"))
				{
					str = str.substring(10);
					if(str.equals("*"))
						return;
					banned.put(str,false);
				}
				else
					seedMap.put(str, false);
				str = reader.readLine();
			}
			reader.close();
			file.close();
		}
		catch (IOException e)												//if an error happened while opening Robot.txt
		{
			System.out.println("Error Openning Robot.txt");
		}

		try
		{																//reading downloaded URLs
			File tempFile = new File("Resources\\Downloaded.txt");
			if(!tempFile.exists())											//if the file doesn't exist create it
				tempFile.createNewFile();

			FileReader file = new FileReader("Resources\\Downloaded.txt");
			BufferedReader reader = new BufferedReader(file);

			String str = reader.readLine();
			while(str != null)
			{
				downloaded.put(str,false);
				str = reader.readLine();
			}
			reader.close();
			file.close();

			wFile = new FileWriter("Resources\\Downloaded.txt", true);				//true is to append to the file
		}
		catch (IOException e)
		{
			System.out.println("Error with Downloaded.txt");
		}

		for(int i =0;i <nThreads; i++)
		{
			Thread t= new Thread(new Crawler());
			t.start();												//starting threads
		}
	}



	//-----------------Constructor for multithreading
	//dummy constructor for threads
	private Crawler()
	{}


	//-----------------Used for multithreading
	//each thread crawls different pages and adds them to a list
	public void run()
	{
		for(Entry<String, Boolean> node : seedMap.entrySet())
		{

			synchronized (seedMap)
			{
				if(node.getValue() == false)
					node.setValue(true);
				else
					continue;
			}
			try
			{
				System.out.println("\t\t\t Started seed: "+ node.getKey());
				crawl(node.getKey(),0);
			}
			catch (MalformedURLException e)
			{
				System.out.println("\nThread faced an Error with URL");
			}
		}
	}


	//-----------------Recursively crawls pages, checks if they're repeated, gets links in them, adds them to a list
	private void crawl(String url, int level) throws MalformedURLException
	{
		if(level > 1)
		{
//			System.out.println("\t\t\tStopped");
			return;
		}
//		url = normalize(url);
		Document doc = request(url);								//crawl it to get the links in it
		Document doc2 = request(url);
		String mainDomain = null;
		if(doc != null && doc2 != null && doc.documentType() != null && doc.documentType().toString().contains("html") && !url.toLowerCase().contains("login"))
		{
			String compactKey = compact(doc.body().text());
			try {
				mainDomain = new URL(url).getHost();
			}
			catch (MalformedURLException e1)
			{
				System.out.println("Error Getting The Domain");
			}

			if((!visited.containsKey(compactKey) && (mainDomain != null && checkBanned(mainDomain)) && (downloaded.isEmpty() || !downloaded.containsKey(url))))
			{	//if it's not visited before and not banned and not downloaded before
				System.out.println(visited.size() + " | " + Thread.currentThread().getId() + " " + url);
				visited.put(compactKey, url);

				try
				{
					synchronized (wFile)
					{
						wFile.write(url + "\n");			//write it to downloaded.txt
					}
				}
				catch (IOException e)
				{				//when the file is closed it throws an error so suppress it
					System.out.println("Error Writing to Downloaded.txt");
				}
			}
			else
				return;

			if(visited.size() <= MAX_CRAWLS)								//stop when max number of documents is reached
			{
				org.bson.Document tempBSON = new org.bson.Document();
				removeAttributes(doc2.body());
				String text = doc2.html().replaceAll("<[^>]*>|\"[^\"]*\"", "").replaceAll("[^\\p{L}\\p{Nd}]+", " ");

				ArrayList<String> references = new ArrayList<String>();
				for(Element a : doc.select("a[href]"))
				{
					String link = a.absUrl("href");
//					String link = normalize(a.absUrl("href"));
					String newDomain = null;
					try {
						newDomain = new URL(link).getHost();
					}
					catch (MalformedURLException e)
					{
						System.out.println("Error Getting The Domain");
					}

					if(newDomain != null && !newDomain.substring(newDomain.indexOf('.') + 1, newDomain.length()).equals(mainDomain.substring(mainDomain.indexOf('.') + 1, mainDomain.length())))
						references.add(link);
//						references.add(normalize(link));
				}


				tempBSON.append("title",doc2.title())
						.append("url",url)
						.append("WordsCount",text.split("\\s+").length)
						.append("page", doc2.body().toString())
						.append("links", references);

				org.bson.Document query = new org.bson.Document("url", url);
				FindIterable<org.bson.Document> check=col.find(query);


				if(!check.iterator().hasNext())

					col.insertOne(tempBSON);

				for(Element a : doc.select("a[href]"))				//loop through the anchor tags in the page
				{
//					System.out.println("\t\t\t\t"+ doc.select("a[href]").size());
					String link = a.absUrl("href");					//get the absolute url

					if(visited.size() <= MAX_CRAWLS)				//to avoid writing to the file after closing
						crawl(link, level + 1);									//recursively crawl the new link
				}
			}
			else															//when MAX_CRAWLS URLs are crawled close the file
			{
				try
				{
					wFile.close();
				}
				catch (IOException e)
				{
					System.out.println("Error Closing Downloaded.txt");
				}
			}
		}
	}


	//-----------------Sends an HTTP request and returns the response with the full document
	private Document request(String url) throws MalformedURLException
	{

		try
		{
			Connection con = Jsoup.connect(url);				//set the connection to the website
			Document doc = con.get();							//make HTTP request

			if(con.response().statusCode() == 200)				//HTTP request status code was OK
			{
				return doc;
			}
			return null;
		} catch (IllegalArgumentException e) {
			System.out.println(url + " Error Requesting Document");
			return null;
		} catch (IOException e)
		{
			System.out.println(url + " Error Requesting Document");
			return null;
		}
	}

	private String compact(String baseText)
	{
		String compactSTR = new String();
		String [] splittedSTR = baseText.split(" ");

		int increment = (splittedSTR.length > 10 )? splittedSTR.length/10 : 1;

		for(int i=0;i<splittedSTR.length;i+=increment)
		{
			compactSTR += splittedSTR[i];
		}
		return compactSTR;
	}


//	private String normalize(String url)
//	{
//		try
//		{
//			String domain = new URL(url).getHost();
//			if(domain.length() - domain.replace(".", "").length() < 2)
//			{
//				String [] temp= url.split("//");
//				if(temp.length != 1)
//					url = temp[0] + "//www." + temp[1];
//			}
//		}
//		catch (MalformedURLException e1)
//		{
//			System.out.println("Error Normalizing the URL");
//		}
//
//		if(url.contains("#"))
//			url = url.subSequence(0, url.indexOf("#")).toString();
//		if(url.contains("?"))
//			url = url.subSequence(0, url.indexOf("?")).toString();
//
//		String temp = url;
//		try
//		{
//			temp = new URI(temp).normalize().toString();
//
//			if(temp.charAt(temp.length()-1) != '/' && (temp.length() - temp.replace(":", "").length() < 2))
//				temp = temp.concat("/");
//		}
//		catch (URISyntaxException e)
//		{
//			System.out.println("Error Normalizing the URL");
//		}
//		return temp;
//	}


	public static void removeAttributes(Element element) {
		Elements scripts = element.getElementsByTag("script");
		Elements svg = element.getElementsByTag("svg");
		Elements footer = element.getElementsByTag("footer");
		svg.remove();
		scripts.remove();
		footer.remove();
		element.removeAttr("class");
		element.removeAttr("id");
		element.removeAttr("style");
		element.removeAttr("href");
		for( Attribute i:element.attributes()){
			element.removeAttr(i.getKey().toString());
		}
		for (Element child : element.children()) {
			removeAttributes(child);
		}
	}



	private static boolean checkBanned(String domain)
	{
		if(banned.isEmpty())
			return true;

		for(Entry<String, Boolean> e: banned.entrySet())
		{
			if(e.getKey().equals(domain))
				return false;
		}
		return true;
	}
}