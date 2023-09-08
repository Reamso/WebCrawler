package web.crawler.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import web.crawler.Crawler;

public class CrawlerTester {

	@Test
	public void isValidT1() {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.space/";
		Crawler c = new Crawler(domain);
		assertTrue(c.isValid(baseUrl));
	}

	@Test
	public void isValidT2() {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.spaace/";
		Crawler c = new Crawler(domain);
		assertArrayEquals(new boolean[]{ false, false}, 
						  new boolean[]{ c.isValid(baseUrl), c.isValid(null)});
	}
	
	@Test
	public void downloadWebPageT1() {
		String domain = "www.blankwebsite.com";
		String baseUrl = "http://www.blankwebsite.com/";
		Crawler c = new Crawler(domain);
		Document d = c.downloadWebPage(baseUrl);
		assertEquals("<html>\r\n"
				+ "	 <head>\r\n"
				+ "  <title>Blank website. Blank site. Nothing to see here.</title>\r\n"
				+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\r\n"
				+ "  <meta name=\"keywords\" content=\"blankwebsite, blank website\">\r\n"
				+ "  <meta name=\"description\" content=\"This resource is provided as a free service for those patrons looking for nothing.\">\r\n"
				+ "  <meta name=\"Author\" content=\"DLC Websites 1999-2023 - http://www.dlcwebsites.com\">\r\n"
				+ "  <meta name=\"Copyright\" content=\"DLC Websites 1999-2023 - http://www.dlcwebsites.com - online tools and entertainment\">\r\n"
				+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n"
				+ "  <link rel=\"stylesheet\" type=\"text/css\" href=\"sitestyle.css\">\r\n"
				+ " </head>\r\n"
				+ " <body bgcolor=\"#FFFFFF\">\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;</p>\r\n"
				+ "  <p>&nbsp;<b><a href=\"http://www.pointlesssites.com/\">Over 1,000 Pointless Sites</a></b></p>\r\n"
				+ " </body>\r\n"
				+ "</html>", d.html());
		
	}
	
	@Test
	public void extractAnchorT1() {
		String domain = "www.blankwebsite.com";
		String baseUrl = "http://www.blankwebsite.com/";
		Crawler c = new Crawler(domain);
		Document d = c.downloadWebPage(baseUrl);
		Elements as = c.extractAnchors(d);
		assertArrayEquals(new boolean[] {true, true},
						  new boolean[] {as.size()==1, 
										 as.size()==1 ? as.get(0).attr("abs:href").equals("http://www.pointlesssites.com/") : false});
		
	}
	
	@Test
	public void extractAnchorT2() {
		String domain = "https://github.com/sonata-project/empty-website/blob/main/README.md";
		String baseUrl = "https://github.com/sonata-project/empty-website/blob/main/README.md";
		Crawler c = new Crawler(domain);
		Document d = c.downloadWebPage(baseUrl);
		Elements as = c.extractAnchors(d);
		assertTrue(as.size()==0);
	}
	
	@Test
	public void inspectPageT1() {
		String domain = "www.blankwebsite.com";
		String baseUrl = "http://www.blankwebsite.com/";
		Crawler c = new Crawler(domain);
		List<String> urls = c.inspectPage(baseUrl, new ArrayList<String>());
		assertTrue(urls.size()==0);
		
	}
	
	@Test
	public void inspectPageT2() {
		String domain = "https://github.com/sonata-project/empty-website/blob/main/README.md";
		String baseUrl = "https://github.com/sonata-project/empty-website/blob/main/README.md";
		Crawler c = new Crawler(domain);
		List<String> urls = c.inspectPage(baseUrl, new ArrayList<String>());
		assertTrue(urls.size()==0);
	}
	
	@Test
	public void crawlFullParallelStreamT1() {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.space/";

		// run code based only on parallel streams
		Crawler c = new Crawler(domain);
		List<String> fullParallel = c.crawlFullParallelStream(baseUrl);
		long originalSize = fullParallel.size();
		long postFilterSize = fullParallel.parallelStream().distinct().count();
		boolean allValid = fullParallel.stream().allMatch(u -> c.isValid(u));
		assertArrayEquals(new boolean[] {true, true},
				  		  new boolean[] {originalSize == postFilterSize, allValid});
	}
	
	@Test
	public void crawlFullParallelStreamT2() {
		String domain = "www.blankwebsite.com";
		String baseUrl = "http://www.blankwebsite.com/";
		Crawler c = new Crawler(domain);
		Document d = c.downloadWebPage(baseUrl);
		Elements as = c.extractAnchors(d);
		List<String> fullParallel = c.crawlFullParallelStream(baseUrl);
		assertArrayEquals(new boolean[] {true, true, true},
						  new boolean[] {as.size()==1, 
										 as.size()==1 ? as.get(0).attr("abs:href").equals("http://www.pointlesssites.com/") : false,
										 fullParallel.size()==1});
		
	}
	
	@Test
	public void forkJoinT1() {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.space/";

		// run code based only on parallel streams
		int numberOfProcessors = Math.round(Runtime.getRuntime().availableProcessors() * 1.5f);
		ForkJoinPool pool = new ForkJoinPool(numberOfProcessors);

		List<String> forkUrls = new ArrayList<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(baseUrl);
		Crawler baseCrawler = new Crawler(domain, baseUrl, forkUrls, pool);
		pool.execute(baseCrawler);
		while (pool.getActiveThreadCount() != 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long originalSize = forkUrls.size();
		long postFilterSize = forkUrls.parallelStream().distinct().count();
		boolean allValid = forkUrls.stream().allMatch(u -> baseCrawler.isValid(u));
		assertArrayEquals(new boolean[] {true, true},
				  		  new boolean[] {originalSize == postFilterSize, allValid});
	}
	
	@Test
	public void forkJoinT2() {
		String domain = "www.blankwebsite.com";
		String baseUrl = "http://www.blankwebsite.com/";;
		Crawler c = new Crawler(domain);
		Document d = c.downloadWebPage(baseUrl);
		Elements as = c.extractAnchors(d);
		// run code based only on parallel streams
		int numberOfProcessors = Math.round(Runtime.getRuntime().availableProcessors() * 1.5f);
		ForkJoinPool pool = new ForkJoinPool(numberOfProcessors);

		List<String> forkUrls = new ArrayList<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(baseUrl);
		Crawler baseCrawler = new Crawler(domain, baseUrl, forkUrls, pool);
		pool.execute(baseCrawler);
		while (pool.getActiveThreadCount() != 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertArrayEquals(new boolean[] {true, true, true},
				  new boolean[] {as.size()==1, 
								 as.size()==1 ? as.get(0).attr("abs:href").equals("http://www.pointlesssites.com/") : false,
								 forkUrls.size()==1});
		
	}
	
	@Test
	public void ParallelStreamForkJoinEquivalenceT1() {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.space/";

		// run code based only on parallel streams
		Crawler c = new Crawler(domain);
		List<String> fullParallel = c.crawlFullParallelStream(baseUrl);

		// activate the web crawler based on fork-join and parallel streams
		int numberOfProcessors = Math.round(Runtime.getRuntime().availableProcessors() * 1.5f);
		ForkJoinPool pool = new ForkJoinPool(numberOfProcessors);

		List<String> forkUrls = new ArrayList<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(baseUrl);
		Crawler baseCrawler = new Crawler(domain, baseUrl, forkUrls, pool);
		pool.execute(baseCrawler);
		while (pool.getActiveThreadCount() != 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertTrue(forkUrls.size() == fullParallel.size() && fullParallel.containsAll(forkUrls));
	}
}
