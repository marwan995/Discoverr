import java.util.Scanner;


public class Testing {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);				//to read user input
		System.out.println("Number of Crawler Threads:");	//testing crawler
		int nThreads = scan.nextInt();
		Crawler crawler = new Crawler(nThreads, 8000);
		
		scan.close();
		
	}

}
