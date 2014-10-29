package cp.articlerep;

import java.util.HashSet;

import cp.articlerep.ds.Iterator;
import cp.articlerep.ds.LinkedList;
import cp.articlerep.ds.List;
import cp.articlerep.ds.Map;
import cp.articlerep.ds.HashTable;

/**
 * @author Ricardo Dias
 */
public class Repository {

	private Map<String, List<Article>> byAuthor;
	private Map<String, List<Article>> byKeyword;
	private Map<Integer, Article> byArticleId;

	public Repository(int nkeys) {
		this.byAuthor = new HashTable<String, List<Article>>(nkeys*2);
		this.byKeyword = new HashTable<String, List<Article>>(nkeys*2);
		this.byArticleId = new HashTable<Integer, Article>(nkeys*2);
	}

	public boolean insertArticle(Article a) {

		if (byArticleId.contains(a.getId()))
			return false;

		Iterator<String> authors = a.getAuthors().iterator();
		while (authors.hasNext()) {
			String name = authors.next();

			List<Article> ll = byAuthor.get(name);  // lista com poucos elementos
			if (ll == null) {
				ll = new LinkedList<Article>();
				byAuthor.put(name, ll);
			}
			ll.add(a);
		}

		Iterator<String> keywords = a.getKeywords().iterator();
		while (keywords.hasNext()) {
			String keyword = keywords.next();

			List<Article> ll = byKeyword.get(keyword);
			if (ll == null) {
				ll = new LinkedList<Article>();
				byKeyword.put(keyword, ll);
			} 
			ll.add(a);
		}

		byArticleId.put(a.getId(), a);

		return true;
	}

	public void removeArticle(int id) {
		Article a = byArticleId.get(id);

		if (a == null)
			return;
		
		Iterator<String> authors = a.getAuthors().iterator();
		while (authors.hasNext()) {
			String name = authors.next();

			List<Article> ll = byAuthor.get(name);
			if (ll != null) {
				int pos = 0;
				Iterator<Article> it = ll.iterator();
				while (it.hasNext()) {
					Article toRem = it.next();
					if (toRem == a) {
						break;
					}
					pos++;
				}
				ll.remove(pos);
				it = ll.iterator(); 
				if (!it.hasNext()) { // checks if the list is empty
					byAuthor.remove(name);
				}
			}
		}

		Iterator<String> keywords = a.getKeywords().iterator();
		while (keywords.hasNext()) {
			String keyword = keywords.next();

			List<Article> ll = byKeyword.get(keyword);
			if (ll != null) {
				int pos = 0;
				Iterator<Article> it = ll.iterator();
				while (it.hasNext()) {
					Article toRem = it.next();
					if (toRem == a) {
						break;
					}
					pos++;
				}
				ll.remove(pos);
				it = ll.iterator();
				if (!it.hasNext()) { // checks if the list is empty
					byKeyword.remove(keyword);
				}
			}
		}
		
		byArticleId.remove(id);

	}

	public List<Article> findArticleByAuthor(List<String> authors) {
		List<Article> res = new LinkedList<Article>();

		Iterator<String> it = authors.iterator();
		while (it.hasNext()) {
			String name = it.next();
			List<Article> as = byAuthor.get(name);
			if (as != null) {
				Iterator<Article> ait = as.iterator();
				while (ait.hasNext()) {
					Article a = ait.next();
					res.add(a);
				}
			}
		}

		return res;
	}

	public List<Article> findArticleByKeyword(List<String> keywords) {
		List<Article> res = new LinkedList<Article>();

		Iterator<String> it = keywords.iterator();
		while (it.hasNext()) {
			String keyword = it.next();
			List<Article> as = byKeyword.get(keyword);
			if (as != null) {
				Iterator<Article> ait = as.iterator();
				while (ait.hasNext()) {
					Article a = ait.next();
					res.add(a);
				}
			}
		}

		return res;
	}

	
	/**
	 * This method is supposed to be executed with no concurrent thread
	 * accessing the repository.
	 * 
	 */
	public boolean validate() {
		
		HashSet<Integer> articleIds = new HashSet<Integer>();
		int articleCount = 0;

		Iterator<Article> aIt = byArticleId.values();
		while(aIt.hasNext()) {
			Article a = aIt.next();

            // Check if there are duplicate articles (será que é preciso dado que estamos a utilizar hashtables?)
            // if (articleIds.contains(a)) return false;

			articleIds.add(a.getId());
			articleCount++;
			
			// check the authors consistency
			Iterator<String> authIt = a.getAuthors().iterator();
			while(authIt.hasNext()) {
				String name = authIt.next();
				if (!searchAuthorArticle(a, name)) {
					System.out.println("No article author.");
					return false;
				}
			}
			
			// check the keywords consistency
			Iterator<String> keyIt = a.getKeywords().iterator();
			while(keyIt.hasNext()) {
				String keyword = keyIt.next();
				if (!searchKeywordArticle(a, keyword)) {
					System.out.println("No article keyword.");
					return false;
				}
			}

            // Check if there aren't any phantom articles left in the keywords
            Iterator<List<Article>> KeyVals = byKeyword.values();
            while (KeyVals.hasNext()){
                List<Article> artList = KeyVals.next();
                for (int i = 0; i < artList.size(); i++){
                    if (!articleIds.contains(artList.get(i))){
                        System.out.println("Phantom article detected");
                        return false;
                    }
                }
            }

            // Check if there aren't any phantom articles left in the authors
            Iterator<List<Article>> authVals = byAuthor.values();
            while (authVals.hasNext()){
                List<Article> artList = authVals.next();
                for (int i = 0; i < artList.size(); i++){
                    if (!articleIds.contains(artList.get(i))){
                        System.out.println("Phantom article detected");
                        return false;
                    }
                }
            }
		}
		
		return articleCount == articleIds.size(); // numero de artigos correcto
	}
	
	private boolean searchAuthorArticle(Article a, String author) {
		List<Article> ll = byAuthor.get(author);
		if (ll != null) {
			Iterator<Article> it = ll.iterator();
			while (it.hasNext()) {
				if (it.next() == a) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean searchKeywordArticle(Article a, String keyword) {
		List<Article> ll = byKeyword.get(keyword);
		if (ll != null) {
			Iterator<Article> it = ll.iterator();
			while (it.hasNext()) {
				if (it.next() == a) {
					return true;
				}
			}
		}
		return false;
	}

}
