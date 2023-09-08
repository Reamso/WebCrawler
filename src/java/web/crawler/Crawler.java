package web.crawler;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private String domain;
	Pattern pattern;
	private List<String> visitedUrls;
	private ForkJoinPool pool;
	private String baseUrl;

	public Crawler(String domain) {
		this.domain = domain;
		String patternDomain = domain.replace(".", "\\.");
		pattern = Pattern.compile("^http(s)?:\\/\\/" + patternDomain + "(\\/$|(\\/[^\\/]*)*$){1}");
		this.visitedUrls = new ArrayList<String>();
		this.pool = null;
		this.baseUrl = "";
	}

	public Crawler(String domain, String baseUrl, List<String> visitedUrls, ForkJoinPool pool) {
		this.domain = domain;
		String patternDomain = domain.replace(".", "\\.");
		pattern = Pattern.compile("^http(s)?:\\/\\/" + patternDomain + "(\\/$|(\\/[^\\/]*)*$){1}");
		this.visitedUrls = visitedUrls;
		this.pool = pool;
		this.baseUrl = baseUrl;
	}

	public boolean isValid(String url) {
		// This method check if the given URL respects the saved pattern, no side effect
		// PRE: url is not null
		// POST: true if respects the patter
		// false otherwise
		if (url == null)
			return false;
		Matcher m = pattern.matcher(url);
		if (!m.matches())
			return false;
		else
			return true;

	}

	public Document downloadWebPage(String url) {
		// This method return the Document of the given URL, no side effect
		// PRE: url is not null and respect the saved pattern
		// POST: if PRE is respected then the Document of the contained web page will be
		// returned
		// else null will be returned
		if (!isValid(url)) {
			return null;
		}
		try {
			Document doc = Jsoup.connect(url).get();
			return doc;
		} catch (IOException e) {
			return null;
		}
	}

	public Elements extractAnchors(Document doc) {
		// This method return the list of all anchors in a given document, no side
		// effect
		// PRE: document != null
		// POST: is returned a list of anchors in the document and if the document is
		// null then the list will be empty
		Elements anchors;
		if (doc == null)
			anchors = new Elements();
		else
			anchors = doc.getElementsByTag("a");
		return anchors;
	}

	public String extractUrlInDomain(Element elem) {
		// This method return the url of the given anchor if and only if the element is
		// anchor,
		// the element is not null, the url is within the specified domain,
		// no side effect
		// PRE: elem != null && elem is an anchor
		// POST: if PRE are verified it is returned the url else is returned null
		String url = null;
		if (elem == null || !elem.tagName().equals("a"))
			return url;
		url = elem.attr("abs:href");
		if (isValid(url))
			return url;
		else
			return "";
	}

	public List<String> inspectPage(String curUrl, List<String> visited) {
		// This method return a new List<String> of all link in domain found in curUrl
		// without repeated elements,
		// there is side effect on visited where is inserted curUrl
		// PRE: isValid(curUrl) && visited != null
		// POST: if isValid(curUrl) && visited != null -> curUrl is inserted in visited
		// and
		// is returned a new List<String> of all link in domain found in curUrl
		// else empty new List<String>
		if (isValid(curUrl) && visited != null) {
			boolean semaphore = false;
			synchronized (visited) {
				semaphore = visited.contains(curUrl);
				if (!semaphore) {
					visited.add(curUrl);
				}
			}
			if (!semaphore) {
				Document d = downloadWebPage(curUrl);
				return extractAnchors(d).parallelStream().map(e -> extractUrlInDomain(e)).filter(url -> {
					synchronized (visited) {
						return url != null && !url.equals("") && !visited.contains(url);
					}
				}).distinct().toList();
			} else
				return new ArrayList<String>();
		} else
			return new ArrayList<String>();
	}

	public List<String> crawlFullParallelStream(String basePage) {
		// This method return a new List<String> of all link in domain found in basePage
		// visiting the entire tree-structure,
		// there is no side effect
		// it make use fully parallelStream to cycle through the web tree-structure
		// PRE: isValid(basePage)
		// POST: if isValid(basePage) -> is returned a new List<String> of all link in
		// domain found in curUrl
		// else empty new List<String>
		List<String> toVisit = new ArrayList<String>();
		List<String> visited = new ArrayList<String>();
		if (isValid(basePage))
			toVisit.add(basePage);
		while (toVisit.size() != 0) {
			toVisit = toVisit.parallelStream().distinct().map(curUrl -> inspectPage(curUrl, visited))
					.reduce(new ArrayList<String>(), (a, b) -> mergeLists(a, b));
		}
		return visited;
	}

	private List<String> mergeLists(List<String> a, List<String> b) {
		// This method return a new List<String> of all distinct elements in a and b, no
		// side effect
		// PRE:
		// POST: new List<String> of all distinct elements in a and b
		List<String> l = new ArrayList<String>();
		l.addAll(a);
		l.addAll(b);
		return l.parallelStream().distinct().toList();
	}

	@Override
	public void compute() {
		// This method implements the RecursiveAction to start the web crawler from a
		// given url inside the given object
		// this method has side effect on this.visitedUrls used to store visited urls
		// from each recursiveAction of each other
		// this method has side effect on the original ForkJoinPool passed as argument
		// in the instantiation
		// instance of Crawler generated by the current one
		// in other words this method inspect a page and for each discovered url create
		// a new instance of a Crawler and add it to the original ForkJoinPool
		// PRE: visitedUrls != null && isValid(baseUrl) && pool != null
		// POST: if (visitedUrls != null && isValid(baseUrl) && pool != null) ->
		// viitedUrls contains all visited urls
		// else viitedUrls is unmodified
		if (visitedUrls != null && isValid(baseUrl) && pool != null) {
			boolean semaphore = false;
			synchronized (visitedUrls) {
				semaphore = visitedUrls.contains(baseUrl);
				if (!semaphore) {
					visitedUrls.add(baseUrl);
				}
			}
			if (!semaphore) {
				Document d = downloadWebPage(baseUrl);
				extractAnchors(d).parallelStream().map(e -> extractUrlInDomain(e)).filter(url -> {
					synchronized (visitedUrls) {
						return url != null && !url.equals("") && !visitedUrls.contains(url);
					}
				}).forEach(url -> {
					synchronized (pool) {
						pool.execute(new Crawler(domain, url, visitedUrls, pool));
					}
				});
			}
		}
	}

	public static void main(String[] args) {
		String domain = "www.dorbit.space";
		String baseUrl = "https://www.dorbit.space/";

		// run code based only on parallel streams
		Crawler c = new Crawler(domain);
		long t = System.currentTimeMillis();
		List<String> fullParallel = c.crawlFullParallelStream(baseUrl);
		System.out.println(
				"FullParallel ended in " + (System.currentTimeMillis() - t) + " with " + fullParallel.size() + " urls");

		// activate the web crawler based on fork-join and parallel streams
		int numberOfProcessors = Math.round(Runtime.getRuntime().availableProcessors() * 1.5f);
		ForkJoinPool pool = new ForkJoinPool(numberOfProcessors);

		List<String> forkUrls = new ArrayList<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(baseUrl);
		Crawler baseCrawler = new Crawler(domain, baseUrl, forkUrls, pool);
		t = System.currentTimeMillis();
		pool.execute(baseCrawler);
		while (pool.getActiveThreadCount() != 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(
				"ForkParallel ended in " + (System.currentTimeMillis() - t) + " with " + forkUrls.size() + " urls");
		System.out
				.println("The discovered urls are " + (fullParallel.containsAll(forkUrls) ? "the same" : "different"));
	}

}
