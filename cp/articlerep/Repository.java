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
public class Repository
{

    private Map<String, List<Article>> byAuthor;
    private Map<String, List<Article>> byKeyword;
    private Map<Integer, Article> byArticleId;

    public Repository(int nkeys)
    {
	this.byAuthor = new HashTable<String, List<Article>>(nkeys * 2);
	this.byKeyword = new HashTable<String, List<Article>>(nkeys * 2);
	this.byArticleId = new HashTable<Integer, Article>(nkeys * 2);
    }

    public boolean insertArticle(Article a)
    {
	if (byArticleId.contains(a.getId()))
	    return false;
        try {
            byArticleId.getWriteLockItem(a.getId());

            Iterator<String> authors = a.getAuthors().iterator();
            while (authors.hasNext()) {
                String name = authors.next();

                byAuthor.getWriteLockItem(name);
                try {
                    List<Article> ll = byAuthor.get(name);
                    if (ll == null) {
                        ll = new LinkedList<Article>();
                        byAuthor.put(name, ll);
                    }
                    //((LinkedList<Article>) ll).writeLock.lock();
                    ll.add(a);
                    //((LinkedList<Article>) ll).writeLock.unlock();
                } finally {
                        byAuthor.releaseWriteLockItem(name);
                }

            }

            Iterator<String> keywords = a.getKeywords().iterator();
            while (keywords.hasNext()) {
                String keyword = keywords.next();
                byKeyword.getWriteLockItem(keyword);
                try {
                    List<Article> ll = byKeyword.get(keyword);
                    if (ll == null) {
                        ll = new LinkedList<Article>();
                        byKeyword.put(keyword, ll);
                    }
                    //((LinkedList<Article>) ll).writeLock.lock();
                    ll.add(a);
                    //((LinkedList<Article>) ll).writeLock.unlock();
                } finally {
                    byKeyword.releaseWriteLockItem(keyword);
                }

            }


            byArticleId.put(a.getId(), a);
        } finally {
            byArticleId.releaseWriteLockItem(a.getId());
        }


        return true;

    }

    public void removeArticle(int id)
    {

	Article a = byArticleId.get(id);


	if (a == null)
	    return;

    byArticleId.getWriteLockItem(a.getId());

    try{
	byArticleId.remove(id);

	Iterator<String> keywords = a.getKeywords().iterator();
	while (keywords.hasNext())
	{
	    String keyword = keywords.next();
        byKeyword.getWriteLockItem(keyword);
        try{
	    List<Article> ll = byKeyword.get(keyword);
	    if (ll != null)
	    {
		int pos = 0;
		//((LinkedList<Article>) ll).writeLock.lock();
		Iterator<Article> it = ll.iterator();
		while (it.hasNext())
		{
		    Article toRem = it.next();
		    if (toRem == a)
		    {
			break;
		    }
		    pos++;
		}
		ll.remove(pos);
		//((LinkedList<Article>) ll).writeLock.unlock();
		//((LinkedList<Article>) ll).readLock.lock();
		it = ll.iterator();
		if (!it.hasNext())
		{ // checks if the list is empty
		    byKeyword.remove(keyword);
		}
		//((LinkedList<Article>) ll).readLock.unlock();
	    }
        } finally {
            byKeyword.releaseWriteLockItem(keyword);
        }

	}

	Iterator<String> authors = a.getAuthors().iterator();
	while (authors.hasNext())
	{
	    String name = authors.next();
        byAuthor.getWriteLockItem(name);
        try{
	    List<Article> ll = byAuthor.get(name);
	    if (ll != null)
	    {
		int pos = 0;
		Iterator<Article> it = ll.iterator();
		//((LinkedList<Article>) ll).writeLock.lock();
		while (it.hasNext())
		{
		    Article toRem = it.next();
		    if (toRem == a)
		    {
			break;
		    }
		    pos++;
		}
		ll.remove(pos);
		//((LinkedList<Article>) ll).writeLock.unlock();
		//((LinkedList<Article>) ll).readLock.lock();
		it = ll.iterator();
		if (!it.hasNext())
		{ // checks if the list is empty
		    byAuthor.remove(name);
		}
		//((LinkedList<Article>) ll).readLock.unlock();
	    }
        } finally {
            byAuthor.releaseWriteLockItem(name);
        }

	}
    } finally {
        byArticleId.releaseWriteLockItem(a.getId());
    }

    }

    public List<Article> findArticleByAuthor(List<String> authors)
    {
	List<Article> res = new LinkedList<Article>();

	Iterator<String> it = authors.iterator();
	while (it.hasNext())
	{
		String name = it.next();
	    byAuthor.getReadLockItem(name);
		try{
			
	    List<Article> as = byAuthor.get(name);
	    if (as != null)
	    {
		Iterator<Article> ait = as.iterator();
		
		// reading from hashtable. Must... Lock... List!
		//((LinkedList<Article>) as).readLock.lock();
		try
		{
		    while (ait.hasNext())
		    {
			Article a = ait.next();
			res.add(a);
		    }
		}
		finally { /*((LinkedList<Article>) as).readLock.unlock();*/ }
	    }
		} finally {
			byAuthor.releaseReadLockItem(name);
		}
	}

	return res;
    }

    public List<Article> findArticleByKeyword(List<String> keywords)
    {
	List<Article> res = new LinkedList<Article>();

	Iterator<String> it = keywords.iterator();
	while (it.hasNext())
	{
	    String keyword = it.next();
	    byKeyword.getReadLockItem(keyword);
	    try{
	    List<Article> as = byKeyword.get(keyword);
	    
	    if (as != null)
	    {
		// reading from hashtable. Must... Lock... List!
		//((LinkedList<Article>) as).readLock.lock();
		try
		{
		    Iterator<Article> ait = as.iterator();
		    while (ait.hasNext())
		    {
			Article a = ait.next();
			res.add(a);
		    }
		}
		finally { /*((LinkedList<Article>) as).readLock.unlock(); */}
	    }
	    } finally {
	    	byKeyword.releaseReadLockItem(keyword);
	    }
	}

	return res;
    }

    /**
     * This method is supposed to be executed with no concurrent thread
     * accessing the repository.
     * 
     */
    public boolean validate()
    {

	HashSet<Integer> articleIds = new HashSet<Integer>();
	int articleCount = 0;

	Iterator<Article> aIt = byArticleId.values();
	while (aIt.hasNext())
	{
	    Article a = aIt.next();

	    articleIds.add(a.getId());
	    articleCount++;

	    // check the authors consistency
	    Iterator<String> authIt = a.getAuthors().iterator();
	    while (authIt.hasNext())
	    {
		String name = authIt.next();
		if (!searchAuthorArticle(a, name))
		{
		    return false;
		}
	    }

	    // check the keywords consistency
	    Iterator<String> keyIt = a.getKeywords().iterator();
	    while (keyIt.hasNext())
	    {
		String keyword = keyIt.next();
		if (!searchKeywordArticle(a, keyword))
		{
		    return false;
		}
	    }    
	    
	}
	
	// Check if there aren't any phantom articles left in the keywords
//    Iterator<List<Article>> KeyVals = byKeyword.values();
//    while (KeyVals.hasNext()){
//        List<Article> artList = KeyVals.next();
//        for (int i = 0; i < artList.size(); i++){
//            if (!byArticleId.contains(artList.get(i).getId())){
//                System.out.println("Phantom article detected keyword");
//                return false;
//            }
//        }
//    }

    // Check if there aren't any phantom articles left in the authors
//    Iterator<List<Article>> authVals = byAuthor.values();
//    while (authVals.hasNext()){
//        List<Article> artList = authVals.next();
//        for (int i = 0; i < artList.size(); i++){
//            if (!byArticleId.contains(artList.get(i).getId())){
//                System.out.println("Phantom article detected author");
//                return false;
//            }
//        }
//    }
	

	return articleCount == articleIds.size();
    }

    private boolean searchAuthorArticle(Article a, String author)
    {
	List<Article> ll = byAuthor.get(author);
	if (ll != null)
	{
	    Iterator<Article> it = ll.iterator();
	    
		while (it.hasNext())
		{
		    if (it.next() == a)
		    {
			return true;
		    }
		}
	}
	return false;
    }

    private boolean searchKeywordArticle(Article a, String keyword)
    {
    
	List<Article> ll = byKeyword.get(keyword);
	if (ll != null)
	{
	    Iterator<Article> it = ll.iterator();
	    
		while (it.hasNext())
		{
		    if (it.next() == a)
		    {
			return true;
		    }
		}
	}
	return false;
    }

}
