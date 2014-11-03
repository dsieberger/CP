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

        byArticleId.getLockItem(a.getId(),0);

        Iterator<String> authors = a.getAuthors().iterator();
        while (authors.hasNext())
        {
            String name = authors.next();

            byAuthor.getLockItem(name,0);

            List<Article> ll = byAuthor.get(name);
            if (ll == null)
            {
                ll = new LinkedList<Article>();
                byAuthor.put(name, ll);
            }
            //((LinkedList<Article>) ll).writeLock.lock();
            ll.add(a);
            //((LinkedList<Article>) ll).writeLock.unlock();
            byAuthor.releaseLockItem(name,0);
        }

        Iterator<String> keywords = a.getKeywords().iterator();
        while (keywords.hasNext())
        {
            String keyword = keywords.next();
            byKeyword.getLockItem(keyword,0);
            List<Article> ll = byKeyword.get(keyword);
            if (ll == null)
            {
                ll = new LinkedList<Article>();
                byKeyword.put(keyword, ll);
            }
            //((LinkedList<Article>) ll).writeLock.lock();
            ll.add(a);
            //((LinkedList<Article>) ll).writeLock.unlock();
            byKeyword.releaseLockItem(keyword,0);
        }


        byArticleId.put(a.getId(), a);
        byArticleId.releaseLockItem(a.getId(),0);

        return true;

    }

    public void removeArticle(int id)
    {

    //byArticleId.getReadLock(id);
	Article a = byArticleId.get(id);
    //byArticleId.releaseReadLock(id);


	if (a == null)
	    return;

    byArticleId.getLockItem(a.getId(),0);

	byArticleId.remove(id);

	Iterator<String> keywords = a.getKeywords().iterator();
	while (keywords.hasNext())
	{
	    String keyword = keywords.next();
        byKeyword.getLockItem(keyword,0);
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
        byKeyword.releaseLockItem(keyword,0);
	}

	Iterator<String> authors = a.getAuthors().iterator();
	while (authors.hasNext())
	{
	    String name = authors.next();
        byAuthor.getLockItem(name,0);
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
        byAuthor.releaseLockItem(name,0);
	}
        byArticleId.releaseLockItem(a.getId(),0);
    }

    public List<Article> findArticleByAuthor(List<String> authors)
    {
	List<Article> res = new LinkedList<Article>();

	Iterator<String> it = authors.iterator();
	while (it.hasNext())
	{
	    String name = it.next();
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

	return articleCount == articleIds.size();
    }

    private boolean searchAuthorArticle(Article a, String author)
    {
	List<Article> ll = byAuthor.get(author);
	if (ll != null)
	{
	    Iterator<Article> it = ll.iterator();
	    
	    //((LinkedList<Article>) ll).readLock.lock();
	    try
	    {
		while (it.hasNext())
		{
		    if (it.next() == a)
		    {
			return true;
		    }
		}
	    } finally { /*((LinkedList<Article>) ll).readLock.unlock();*/ }
	}
	return false;
    }

    private boolean searchKeywordArticle(Article a, String keyword)
    {
	List<Article> ll = byKeyword.get(keyword);
	if (ll != null)
	{
	    Iterator<Article> it = ll.iterator();
	    
	    //((LinkedList<Article>) ll).readLock.lock();
	    try
	    {
		while (it.hasNext())
		{
		    if (it.next() == a)
		    {
			return true;
		    }
		}
	    } finally { /*((LinkedList<Article>) ll).readLock.unlock();*/ }
	}
	return false;
    }

}
