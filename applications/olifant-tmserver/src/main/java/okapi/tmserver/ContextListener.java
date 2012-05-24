package okapi.tmserver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.okapi.lib.tmdb.h2.Repository;

/**
 * Application Lifecycle Listener implementation class RepositoryContextListener
 *
 */
public class ContextListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ContextListener() {
        // TODO Auto-generated constructor stub
    }

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext c = sce.getServletContext();
		Repository repo = (Repository) c.getAttribute("repo");
		repo.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		  ServletContext c = sce.getServletContext();
		  if (c != null) { 
		    if (c.getInitParameter("repoLocation") != null) {       
		      c.setAttribute(          
		       "repoLocation",
		        c.getInitParameter("repoLocation")
		      );     
		    }   
		  }
		  
		  //--CREATING TEST REPO AND TM FOR NOW--
		  Repository repo = new Repository(null, false);
		  repo.createTm("Test TM", "Test TM", "EN");
		  c.setAttribute("repo", repo);		  
	}
}
